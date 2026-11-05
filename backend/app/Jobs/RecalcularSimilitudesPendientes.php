<?php

namespace App\Jobs;

use App\Models\MotorConfig;
use App\Models\Project;
use App\Similarity\EstadoSimilitud;
use App\Similarity\Recomputador;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Throwable;

/**
 * Recalcula las propuestas pendientes de un mismo programa y red de
 * conocimiento después de una aprobación. No toca aprobadas ni rechazadas.
 *
 * El lock por ámbito evita dos jobs concurrentes sobre el mismo corpus. Si el
 * corpus cambia durante el proceso (otra aprobación), el job deja las
 * propuestas como pendientes y encola una nueva pasada.
 */
class RecalcularSimilitudesPendientes implements ShouldQueue
{
    use Dispatchable;
    use InteractsWithQueue;
    use Queueable;
    use SerializesModels;

    public int $tries = 3;

    /** @var array<int, int> */
    public array $backoff = [10, 60];

    public int $timeout = 300;

    public function __construct(
        public int $programaId,
        public int $redId,
        public string $hashScope,
    ) {
        $this->onQueue('similitudes');
    }

    public function handle(Recomputador $motor): void
    {
        $lock = Cache::lock("similitud:scope:{$this->programaId}:{$this->redId}", 600);
        if (! $lock->get()) {
            return;
        }

        $recalcularDeNuevo = false;

        try {
            $ids = Project::query()
                ->where('estado', 'pendiente')
                ->whereHas('classGroup', function ($q) {
                    $q->where('id_programa', $this->programaId)
                        ->whereHas('program', fn ($p) => $p->where('knowledge_network_id', $this->redId));
                })
                ->orderBy('id')
                ->pluck('id');

            foreach ($ids->chunk(50) as $chunk) {
                Project::whereIn('id', $chunk)
                    ->where('estado', 'pendiente')
                    ->update(['similitud_estado' => EstadoSimilitud::EN_PROCESO]);

                foreach ($chunk as $id) {
                    $propuesta = $this->detectarSiSiguePendiente($motor, (int) $id);
                    if (! $propuesta) {
                        continue;
                    }

                    $hashActual = $motor->hashReferencias($propuesta);
                    if ($hashActual !== $this->hashScope) {
                        $estado = EstadoSimilitud::PENDIENTE;
                        $recalcularDeNuevo = true;
                    } else {
                        $estado = $hashActual === sha1('')
                            ? EstadoSimilitud::SIN_REFERENCIAS
                            : EstadoSimilitud::ACTUALIZADO;
                    }

                    Project::whereKey($id)
                        ->where('estado', 'pendiente')
                        ->update([
                            'similitud_estado' => $estado,
                            'similitud_hash' => $hashActual,
                            'similitud_actualizada_en' => now(),
                        ]);
                }
            }

            if ($motor->hashScope($this->programaId, $this->redId) !== $this->hashScope) {
                $recalcularDeNuevo = true;
            }
        } finally {
            $lock->release();
        }

        if ($recalcularDeNuevo) {
            self::dispatch(
                $this->programaId,
                $this->redId,
                $motor->hashScope($this->programaId, $this->redId),
            );
        }
    }

    /**
     * Serializa con la aprobación usando el mismo orden de locks: motor y
     * luego propuesta. Si otra petición aprobó la propuesta mientras el job
     * esperaba, no se vuelve a analizar (las aprobadas son históricas).
     */
    private function detectarSiSiguePendiente(Recomputador $motor, int $id): ?Project
    {
        return DB::transaction(function () use ($motor, $id) {
            $config = MotorConfig::actual();
            MotorConfig::whereKey($config->id)->lockForUpdate()->first();

            $propuesta = Project::with('classGroup.program')
                ->whereKey($id)
                ->lockForUpdate()
                ->first();

            if (! $propuesta || $propuesta->estado !== 'pendiente') {
                return null;
            }

            $motor->detectar($propuesta, false);

            return $propuesta->fresh();
        });
    }

    public function failed(Throwable $e): void
    {
        Project::query()
            ->where('estado', 'pendiente')
            ->whereHas('classGroup', function ($q) {
                $q->where('id_programa', $this->programaId)
                    ->whereHas('program', fn ($p) => $p->where('knowledge_network_id', $this->redId));
            })
            ->update(['similitud_estado' => EstadoSimilitud::ERROR]);

        report($e);
    }
}
