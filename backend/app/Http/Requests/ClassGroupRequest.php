<?php

namespace App\Http\Requests;

use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Support\Facades\Gate;
use Illuminate\Validation\Rule;

// Validación de fichas (alta y edición). El unique del código ignora la ficha
// actual cuando se está editando. La autorización corre ANTES de validar para
// no devolver 422 a quien no tiene permiso.
class ClassGroupRequest extends FormRequest
{
    public function authorize(): bool
    {
        $ficha = $this->route('class_group');

        if ($ficha && !Gate::allows('manage', $ficha)) {
            throw new AuthorizationException('No puedes editar una ficha que no está a tu cargo.');
        }

        return true;
    }

    public function rules(): array
    {
        $ficha = $this->route('class_group');

        return [
            'codigo' => [
                'required', 'max:255',
                Rule::unique('class_groups', 'codigo')->ignore(optional($ficha)->id),
            ],
            'numero' => 'nullable|max:255',
            'nombre' => 'required|max:255',
            'estado' => 'required|in:activo,finalizado',
            'id_programa' => 'required|exists:training_programs,id',
            'id_instructor' => 'required|exists:instructors,id',
        ];
    }
}
