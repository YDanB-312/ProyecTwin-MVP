<?php

namespace App\Http\Controllers\Api;

use App\Models\Centro;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class CentroController extends Controller
{
    public function index()
    {
        $items = Centro::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|min:5|max:255|unique:centros,nombre',
            'ciudad' => 'nullable|max:255',
        ]);
        $item = Centro::create($request->only(['nombre', 'ciudad']));
        return response()->json($item, 201);
    }

    public function show($id)
    {
        $item = Centro::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Centro $centro)
    {
        $request->validate([
            'nombre' => 'required|min:5|max:255|unique:centros,nombre,' . $centro->id,
            'ciudad' => 'nullable|max:255',
        ]);
        $centro->update($request->only(['nombre', 'ciudad']));
        return $centro;
    }

    public function destroy(Centro $centro)
    {
        if ($centro->classGroups()->exists()) {
            return response()->json([
                'message' => 'No se puede eliminar: tiene fichas asociadas.',
            ], 409);
        }
        $centro->delete();
        return response()->json($centro);
    }
}
