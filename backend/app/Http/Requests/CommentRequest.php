<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

// Validación de observaciones (alta y edición comparten reglas).
class CommentRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // La autorización vive en la Policy.
    }

    public function rules(): array
    {
        return [
            'texto' => 'required',
            'id_proyecto' => 'required|exists:projects,id',
            'id_usuario' => 'nullable|exists:general_users,id',
            'respuesta_a' => 'nullable|exists:comments,id',
        ];
    }
}
