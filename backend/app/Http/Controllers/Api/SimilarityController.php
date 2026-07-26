<?php

namespace App\Http\Controllers\Api;

use App\Models\Similarity;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class SimilarityController extends Controller
{
    public function index()
    {
        $items = Similarity::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'porcentaje' => 'required|numeric',
            'estado' => 'required|in:pendiente,revisada,resuelta',
            'detalles' => 'nullable|array',
            'fecha' => 'nullable|date',
            'id_proyecto_1' => 'required|exists:projects,id',
            'id_proyecto_2' => 'required|exists:projects,id',
            'id_instructor' => 'nullable|exists:instructors,id',
        ]);

        $item = Similarity::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Similarity::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Similarity $similarity)
    {
        $request->validate([
            'porcentaje' => 'required|numeric',
            'estado' => 'required|in:pendiente,revisada,resuelta',
            'detalles' => 'nullable|array',
            'fecha' => 'nullable|date',
            'id_proyecto_1' => 'required|exists:projects,id',
            'id_proyecto_2' => 'required|exists:projects,id',
            'id_instructor' => 'nullable|exists:instructors,id',
        ]);

        $similarity->update($request->all());
        return $similarity;
    }

    public function destroy(Similarity $similarity)
    {
        $similarity->delete();
        return $similarity;
    }
}
