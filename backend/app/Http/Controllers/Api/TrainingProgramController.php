<?php

namespace App\Http\Controllers\Api;

use App\Models\TrainingProgram;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class TrainingProgramController extends Controller
{
    public function index()
    {
        return TrainingProgram::included()->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'nivel' => 'required|max:255',
            'num_trimestres' => 'required|integer',
            'knowledge_network_id' => 'required|exists:knowledge_networks,id',
        ]);

        $item = TrainingProgram::create($request->all());
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return TrainingProgram::included()->findOrFail($id);
    }

    public function update(Request $request, TrainingProgram $training_program)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'nivel' => 'required|max:255',
            'num_trimestres' => 'required|integer',
            'knowledge_network_id' => 'required|exists:knowledge_networks,id',
        ]);

        $training_program->update($request->all());
        return $training_program;
    }

    public function destroy(TrainingProgram $training_program)
    {
        $training_program->delete();
        return $training_program;
    }
}
