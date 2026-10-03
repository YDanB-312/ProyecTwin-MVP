<?php

namespace App\Http\Controllers\Api;

use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

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

    // Guarda la red completa (nombre + programas: crear/renombrar/eliminar) en
    // una sola transacción. Evita el loop secuencial con estados parciales.
    public function sincronizar(Request $request, KnowledgeNetwork $knowledge_network)
    {
        $datos = $request->validate([
            'nombre' => 'required|max:255|unique:knowledge_networks,nombre,' . $knowledge_network->id,
            'programas' => 'required|array|min:1',
            'programas.*.id' => 'nullable|integer',
            'programas.*.nombre' => 'required|max:255',
        ]);

        $programas = DB::transaction(function () use ($datos, $knowledge_network) {
            $knowledge_network->update(['nombre' => $datos['nombre']]);

            $idsEnviados = collect($datos['programas'])->pluck('id')->filter()->map(fn ($id) => (int) $id);
            $existentes = TrainingProgram::where('knowledge_network_id', $knowledge_network->id)->get();

            foreach ($datos['programas'] as $item) {
                $nombre = trim($item['nombre']);
                $previo = !empty($item['id']) ? $existentes->firstWhere('id', (int) $item['id']) : null;
                if ($previo) {
                    if ($previo->nombre !== $nombre) $previo->update(['nombre' => $nombre]);
                } else {
                    TrainingProgram::create([
                        'nombre' => $nombre,
                        'nivel' => 'Tecnologo',
                        'num_trimestres' => 6,
                        'knowledge_network_id' => $knowledge_network->id,
                    ]);
                }
            }

            foreach ($existentes as $previo) {
                if (!$idsEnviados->contains((int) $previo->id)) $previo->delete();
            }

            return TrainingProgram::where('knowledge_network_id', $knowledge_network->id)->orderBy('id')->get();
        });

        \App\Support\Auditoria::registrar('actualizar_red', 'knowledge_networks', $knowledge_network->id, [
            'nombre' => $knowledge_network->nombre,
            'programas' => $programas->count(),
        ]);

        return response()->json([
            'red' => $knowledge_network->fresh(),
            'programas' => $programas,
        ]);
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
