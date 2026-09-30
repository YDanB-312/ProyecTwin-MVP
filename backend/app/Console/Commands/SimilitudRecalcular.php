<?php

namespace App\Console\Commands;

use App\Similarity\Recomputador;
use Illuminate\Console\Command;

// Recompone la tabla `similarities` con el motor actual.
class SimilitudRecalcular extends Command
{
    protected $signature = 'similitud:recalcular {--sin-notificar : No genera notificaciones}';

    protected $description = 'Recalcula todas las similitudes con el motor actual.';

    public function handle(Recomputador $motor): int
    {
        $r = $motor->recalcular(!$this->option('sin-notificar'));
        $this->info("Eliminadas: {$r['eliminadas']} · creadas: {$r['creadas']} · total: {$r['total']}");
        return self::SUCCESS;
    }
}
