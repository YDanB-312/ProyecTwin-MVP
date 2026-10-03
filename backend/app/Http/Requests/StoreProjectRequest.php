<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

// Validación del alta de propuestas (antes inline en el controlador).
class StoreProjectRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // La autorización por rol vive en el controlador.
    }

    public function rules(): array
    {
        return [
            'titulo' => 'required|max:255',
            'resumen' => 'required',
            'palabras_clave' => 'nullable|max:255',
            'area_aplicacion' => 'required|max:255',
            'objetivo_general' => 'nullable',
            'objetivos_especificos' => 'nullable|array',
            'estado' => 'nullable|in:pendiente,aprobado,rechazado',
            'id_creador' => 'nullable|exists:general_users,id',
            'id_instructor_asignado' => 'nullable|exists:instructors,id',
            'id_class_group' => 'nullable|exists:class_groups,id',
        ];
    }
}
