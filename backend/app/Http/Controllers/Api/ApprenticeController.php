<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Models\ClassGroup;
use App\Models\GeneralUser;
use App\Models\Instructor;
use App\Models\Notification;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ApprenticeController extends Controller
{
    public function index(Request $request)
    {
        $user = $request->user();

        // Admin ve todos; instructor los de sus fichas; aprendiz los de la suya.
        if (optional($user)->rol === 'admin') {
            return Apprentice::included()->get();
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

        return Apprentice::included()->whereIn('id_class_group', $fichaIds)->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'codigo' => 'required|max:255',
            'id_class_group' => 'nullable|exists:class_groups,id',
            'id_usuario' => 'required|exists:general_users,id',
            'id_programa' => 'required|exists:training_programs,id',
        ]);

        $usuario = GeneralUser::findOrFail($request->id_usuario);
        if ($usuario->rol !== 'aprendiz') {
            return response()->json(['message' => 'El usuario no tiene rol de aprendiz.'], 422);
        }

        $item = Apprentice::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Apprentice::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Apprentice $apprentice)
    {
        $request->validate([
            'codigo' => 'required|max:255',
            'id_class_group' => 'nullable|exists:class_groups,id',
            'id_usuario' => 'required|exists:general_users,id',
            'id_programa' => 'required|exists:training_programs,id',
        ]);

        $usuario = GeneralUser::findOrFail($request->id_usuario);
        if ($usuario->rol !== 'aprendiz') {
            return response()->json(['message' => 'El usuario no tiene rol de aprendiz.'], 422);
        }

        $apprentice->update($request->all());
        return $apprentice;
    }

    public function destroy(Apprentice $apprentice)
    {
        $apprentice->delete();
        return $apprentice;
    }

    // ------------------------------------------------ Mi ficha (aprendiz)

    // Previsualiza una ficha por código antes de unirse. Devuelve el estado
    // para que la interfaz explique si no acepta nuevos integrantes.
    public function fichaPorCodigo(string $codigo)
    {
        $ficha = $this->buscarPorCodigo($codigo);
        if (!$ficha) {
            return response()->json(['message' => 'No encontramos una ficha con ese código.'], 404);
        }
        return $ficha->loadCount('apprentices');
    }

    // Une al aprendiz autenticado a la ficha del código recibido. Antes puede
    // salir de su ficha actual (la interfaz lo hace en dos pasos); si aún así
    // llega con ficha, el cambio también notifica al instructor anterior.
    public function unirmeAFicha(Request $request)
    {
        $request->validate(['codigo' => 'required|string|max:255']);
        $user = $request->user();

        $ficha = $this->buscarPorCodigo($request->codigo);
        if (!$ficha) {
            return response()->json(['message' => 'No encontramos una ficha con ese código.'], 404);
        }
        if ($ficha->estado !== 'activo') {
            return response()->json(['message' => 'Esa ficha no acepta nuevos integrantes.'], 422);
        }

        $aprendiz = $this->miAprendiz($request);
        if ($aprendiz && (int) $aprendiz->id_class_group === (int) $ficha->id) {
            return response()->json(['message' => 'Ya perteneces a esta ficha.'], 422);
        }

        $fichaAnterior = ($aprendiz && $aprendiz->id_class_group)
            ? ClassGroup::with('instructor')->find($aprendiz->id_class_group)
            : null;

        if ($aprendiz) {
            $aprendiz->update([
                'id_class_group' => $ficha->id,
                'id_programa' => $ficha->id_programa,
            ]);
        } else {
            // Un aprendiz recién registrado no tiene fila todavía: se crea aquí.
            $aprendiz = Apprentice::create([
                'codigo' => $this->codigoDisponible(),
                'id_usuario' => $user->id,
                'id_class_group' => $ficha->id,
                'id_programa' => $ficha->id_programa,
            ]);
        }

        $this->notificarInstructor(
            $ficha,
            'El aprendiz ' . $this->nombreDe($user) . ' se unió a la ficha "' . $ficha->nombre . '".',
            'ficha:' . $ficha->id
        );
        if ($fichaAnterior) {
            $this->notificarInstructor(
                $fichaAnterior,
                'El aprendiz ' . $this->nombreDe($user) . ' salió de la ficha "' . $fichaAnterior->nombre . '".',
                'ficha:' . $fichaAnterior->id
            );
        }

        return $aprendiz->fresh()->load('classGroup.program', 'classGroup.instructor.generalUser');
    }

    // Deja al aprendiz autenticado sin ficha. La fila y su código se conservan.
    public function salirDeFicha(Request $request)
    {
        $aprendiz = $this->miAprendiz($request);
        if (!$aprendiz || !$aprendiz->id_class_group) {
            return response()->json(['message' => 'No perteneces a ninguna ficha.'], 422);
        }

        $ficha = ClassGroup::with('instructor')->find($aprendiz->id_class_group);
        $aprendiz->update(['id_class_group' => null]);

        if ($ficha) {
            $this->notificarInstructor(
                $ficha,
                'El aprendiz ' . $this->nombreDe($request->user()) . ' salió de la ficha "' . $ficha->nombre . '".',
                'ficha:' . $ficha->id
            );
        }

        return $aprendiz->fresh();
    }

    // ---------------------------------------------------------------- Internos

    private function miAprendiz(Request $request): ?Apprentice
    {
        return Apprentice::where('id_usuario', $request->user()->id)->first();
    }

    // Busca por código sin importar mayúsculas/espacios.
    private function buscarPorCodigo(?string $codigo): ?ClassGroup
    {
        $codigo = strtolower(trim((string) $codigo));
        if ($codigo === '') return null;

        return ClassGroup::with('program', 'instructor.generalUser')
            ->whereRaw('LOWER(codigo) = ?', [$codigo])
            ->first();
    }

    private function codigoDisponible(): string
    {
        $n = (int) Apprentice::max('id') + 1;
        do {
            $codigo = 'AP-' . str_pad((string) $n, 3, '0', STR_PAD_LEFT);
            $n++;
        } while (Apprentice::where('codigo', $codigo)->exists());

        return $codigo;
    }

    private function nombreDe($user): string
    {
        return trim(($user->nombre ?? '') . ' ' . ($user->apellido ?? '')) ?: ($user->correo ?? 'Un aprendiz');
    }

    // Avisa al instructor de la ficha (si tiene uno) de un movimiento.
    private function notificarInstructor(?ClassGroup $ficha, string $titulo, string $enlace): void
    {
        $idUsuario = optional($ficha?->instructor)->id_usuario;
        if (!$idUsuario) return;

        Notification::create([
            'titulo' => $titulo,
            'tipo' => 'sistema',
            'enlace' => $enlace,
            'leida' => false,
            'fecha' => now()->toDateString(),
            'id_usuario' => $idUsuario,
        ]);
    }
}
