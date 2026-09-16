<?php

namespace App\Http\Controllers\Api;

use App\Models\TrainingCenter;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class TrainingCenterController extends Controller
{
    public function index()
    {
        return TrainingCenter::included()->get();
    }

    public function store(Request $request)
    {
        $request->validate([
            'name' => 'required|min:5|max:255|unique:training_centers,name',
            'city' => 'nullable|max:255',
        ]);

        $item = TrainingCenter::create($request->only(['name', 'city']));
        return response()->json($item, 201);
    }

    public function show($id)
    {
        return TrainingCenter::included()->findOrFail($id);
    }

    public function update(Request $request, TrainingCenter $training_center)
    {
        $request->validate([
            'name' => 'required|min:5|max:255|unique:training_centers,name,' . $training_center->id,
            'city' => 'nullable|max:255',
        ]);

        $training_center->update($request->only(['name', 'city']));
        return $training_center;
    }

    public function destroy(TrainingCenter $training_center)
    {
        // No se elimina un centro con fichas asociadas.
        if ($training_center->classGroups()->exists()) {
            return response()->json([
                'message' => 'No se puede eliminar: tiene fichas asociadas.',
            ], 409);
        }
        $training_center->delete();
        return $training_center;
    }
}
