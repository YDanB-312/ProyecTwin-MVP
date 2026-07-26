<?php

namespace App\Http\Controllers\Api;

use App\Models\TrainingProgram;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class TrainingProgramController extends Controller
{
    public function index()
    {
        $items = TrainingProgram::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'nivel' => 'required|max:255',
            'num_trimestres' => 'required|integer',
        ]);

        $item = TrainingProgram::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = TrainingProgram::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, TrainingProgram $training_program)
    {
        $request->validate([
            'nombre' => 'required|max:255',
            'nivel' => 'required|max:255',
            'num_trimestres' => 'required|integer',
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
