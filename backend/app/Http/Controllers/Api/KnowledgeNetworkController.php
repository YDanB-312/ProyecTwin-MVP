<?php

namespace App\Http\Controllers\Api;

use App\Models\KnowledgeNetwork;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class KnowledgeNetworkController extends Controller
{
    public function index()
    {
        $items = KnowledgeNetwork::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'nombre' => 'required|max:255|unique:knowledge_networks,nombre',
        ]);

        $item = KnowledgeNetwork::create($request->only(['nombre']));
        return response()->json($item, 201);
    }

    public function show($id)
    {
        $item = KnowledgeNetwork::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, KnowledgeNetwork $knowledge_network)
    {
        $request->validate([
            'nombre' => 'required|max:255|unique:knowledge_networks,nombre,' . $knowledge_network->id,
        ]);

        $knowledge_network->update($request->only(['nombre']));
        return $knowledge_network;
    }

    public function destroy(KnowledgeNetwork $knowledge_network)
    {
        // Bloquear si tiene programas asociados
        if ($knowledge_network->trainingPrograms()->exists()) {
            return response()->json([
                'message' => 'No se puede eliminar: hay programas asociados a esta red. Reasigna o elimina esos programas primero.',
            ], 409);
        }

        $knowledge_network->delete();
        return response()->json($knowledge_network);
    }
}
