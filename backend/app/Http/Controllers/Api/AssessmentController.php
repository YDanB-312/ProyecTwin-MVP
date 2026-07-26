<?php

namespace App\Http\Controllers\Api;

use App\Models\Assessment;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class AssessmentController extends Controller
{
    public function index()
    {
        $items = Assessment::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'decision' => 'required|max:255',
            'puntaje' => 'nullable|numeric',
            'tipo_revision' => 'nullable|max:255',
            'texto' => 'nullable',
            'fecha' => 'required|date',
            'tiempo_respuesta_dias' => 'required|integer',
            'id_proyecto' => 'required|exists:projects,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $item = Assessment::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Assessment::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Assessment $assessment)
    {
        $request->validate([
            'decision' => 'required|max:255',
            'puntaje' => 'nullable|numeric',
            'tipo_revision' => 'nullable|max:255',
            'texto' => 'nullable',
            'fecha' => 'required|date',
            'tiempo_respuesta_dias' => 'required|integer',
            'id_proyecto' => 'required|exists:projects,id',
            'id_instructor' => 'required|exists:instructors,id',
        ]);

        $assessment->update($request->all());
        return $assessment;
    }

    public function destroy(Assessment $assessment)
    {
        $assessment->delete();
        return $assessment;
    }
}
