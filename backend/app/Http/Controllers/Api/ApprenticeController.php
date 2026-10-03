<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Notification;
use App\Services\NotificacionesService;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ApprenticeController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();

        // Solo cuentan como integrantes quienes tienen rol aprendiz (una cuenta
        // que cambió de rol conserva su fila histórica pero sale del roster).
        $soloAprendices = fn ($q) => $q->whereHas('generalUser', fn ($u) => $u->where('rol', 'aprendiz'));

        // Admin ve todos; instructor los de sus fichas; aprendiz los de la suya.
        if (optional($user)->rol === 'admin') {
            return Apprentice::included()->where($soloAprendices)->get();
        }

        $fichaIds = collect();
        if ($user && $user->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            $fichaIds = $instructorId
                ? ClassGroup::where('id_instructor', $instructorId)->pluck('id')
                : collect();
        } elseif ($user) {
            $fichaIds = Apprentice::where('id_usuario', $user->id)->pluck('id_class_group')->filter();
        }

        return Apprentice::included()->whereIn('id_class_group', $fichaIds)->where($soloAprendices)->get();
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'codigo' => 'required|max:255|unique:apprentices,codigo',
            'id_class_group' => 'nullable|exists:class_groups,id',
            'id_usuario' => 'required|exists:general_users,id',
            // Sin ficha no hay programa (se deriva de la ficha cuando la hay).
            'id_programa' => 'nullable|exists:training_programs,id',
        ]);

        $usuario = GeneralUser::findOrFail($request->id_usuario);
        if ($usuario->rol !== 'aprendiz') {
            return response()->json(['message' => 'El usuario no tiene rol de aprendiz.'], 422);
        }

        // Invariante: el programa del aprendiz es el de su ficha (sin ficha, nulo).
        $data['id_programa'] = null;
        if ($request->filled('id_class_group')) {
            $ficha = ClassGroup::findOrFail($request->id_class_group);
            $data['id_programa'] = $ficha->id_programa;

            // Un instructor solo registra aprendices en sus propias fichas.
            if ($request->user()->rol === 'instructor') {
                $mio = Instructor::where('id_usuario', $request->user()->id)->value('id');
                if (!$mio || (int) $ficha->id_instructor !== (int) $mio) {
                    return response()->json(['message' => 'Solo puedes registrar aprendices en tus propias fichas.'], 403);
                }
            }
        }

        $item = Apprentice::create($data);
        return $item;
    }

    public function show(Request $request, Apprentice $apprentice)
    {
        $this->autorizarLectura($request, $apprentice);
        return Apprentice::included()->findOrFail($apprentice->id);
    }

    public function update(Request $request, Apprentice $apprentice)
    {
        $data = $request->validate([
            'codigo' => 'required|max:255|unique:apprentices,codigo,' . $apprentice->id,
            'id_class_group' => 'nullable|exists:class_groups,id',
            'id_usuario' => 'required|exists:general_users,id',
            // Sin ficha no hay programa (se deriva de la ficha cuando la hay).
            'id_programa' => 'nullable|exists:training_programs,id',
        ]);

        $user = $request->user();

        // Un instructor solo gestiona aprendices de sus fichas (origen y
        // destino) y no puede reasignar la cuenta (eso es del admin).
        if (optional($user)->rol === 'instructor') {
            $mio = Instructor::where('id_usuario', $user->id)->value('id');
            $origen = ($mio && $apprentice->id_class_group)
                ? ClassGroup::where('id', $apprentice->id_class_group)->where('id_instructor', $mio)->exists()
                : false;
            $destino = $request->filled('id_class_group')
                ? ClassGroup::where('id', $request->id_class_group)->where('id_instructor', $mio)->exists()
                : true;

            if (!$origen || !$destino) {
                return response()->json(['message' => 'Solo puedes gestionar aprendices de tus propias fichas.'], 403);
            }
            if ((int) $request->id_usuario !== (int) $apprentice->id_usuario) {
                return response()->json(['message' => 'Solo un administrador puede reasignar la cuenta de un aprendiz.'], 403);
            }
        }

        $usuario = GeneralUser::findOrFail($request->id_usuario);
        if ($usuario->rol !== 'aprendiz') {
            return response()->json(['message' => 'El usuario no tiene rol de aprendiz.'], 422);
        }

        // Invariante: el programa del aprendiz es el de su ficha (sin ficha, nulo).
        $data['id_programa'] = $request->filled('id_class_group')
            ? ClassGroup::findOrFail($request->id_class_group)->id_programa
            : null;

        $fichaAnterior = $apprentice->id_class_group;
        $apprentice->update($data);
        $apprentice->refresh();

        // Si el staff lo sacó o lo movió de ficha, se avisa al aprendiz.
        if ($fichaAnterior && (int) $apprentice->id_class_group !== (int) $fichaAnterior) {
            $this->notificarAprendizDeFicha($apprentice, (int) $fichaAnterior);
        }

        return $apprentice;
    }

    public function destroy(Apprentice $apprentice)
    {
        // Admin sin restricciones: borra el perfil (la FK arrastra sus pivotes de
        // equipo). Las propuestas del usuario se conservan (son de la cuenta).
        $apprentice->delete();
        return $apprentice;
    }

    // ---------------------------------------------------------------- Internos

    // Lectura de un aprendiz: admin cualquiera; instructor los de sus fichas;
    // aprendiz su propia fila o un compañero de su ficha.
    private function autorizarLectura(Request $request, Apprentice $item): void
    {
        $user = $request->user();
        if (optional($user)->rol === 'admin') return;

        if (optional($user)->rol === 'instructor') {
            $instructorId = Instructor::where('id_usuario', $user->id)->value('id');
            $ok = $instructorId && $item->id_class_group
                && ClassGroup::where('id', $item->id_class_group)
                    ->where('id_instructor', $instructorId)->exists();
            abort_if(!$ok, 403, 'No puedes ver un aprendiz de otra ficha.');
            return;
        }

        $fichaId = Apprentice::where('id_usuario', optional($user)->id)->value('id_class_group');
        $ok = $fichaId && (int) $item->id_class_group === (int) $fichaId;
        abort_if(!$ok, 403, 'No puedes ver a un aprendiz de otra ficha.');
    }

    // Avisa al aprendiz de que lo sacaron o movieron de ficha (lo hizo el staff).
    private function notificarAprendizDeFicha(Apprentice $aprendiz, int $fichaAnteriorId): void
    {
        $nombreAnterior = optional(ClassGroup::find($fichaAnteriorId))->nombre ?? 'su ficha';
        $nombreNueva = optional(ClassGroup::find($aprendiz->id_class_group))->nombre;

        $titulo = $nombreNueva
            ? 'Te movieron de la ficha "' . $nombreAnterior . '" a "' . $nombreNueva . '".'
            : 'Te sacaron de la ficha "' . $nombreAnterior . '".';

        app(NotificacionesService::class)->crear($aprendiz->id_usuario, $titulo, 'sistema');
    }
}
