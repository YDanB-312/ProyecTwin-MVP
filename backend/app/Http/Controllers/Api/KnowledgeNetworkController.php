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
        // Admin sin restricciones: borra la red y sus programas en cascada.
        $impacto = ['programas' => $knowledge_network->trainingPrograms()->count()];

        \App\Support\BorradoCascada::red($knowledge_network);

        \App\Support\Auditoria::registrar('eliminar_red', 'knowledge_networks', $knowledge_network->id, [
            'nombre' => $knowledge_network->nombre,
            'impacto' => $impacto,
        ]);

        return response()->json($knowledge_network);
    }
}
