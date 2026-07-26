<?php

namespace App\Http\Controllers\Api;

use App\Models\Apprentice;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

class ApprenticeController extends Controller
{
    public function index()
    {
        $items = Apprentice::included()->get();
        return $items;
    }

    public function store(Request $request)
    {
        $request->validate([
            'codigo' => 'required|max:255',
            'id_class_group' => 'required|exists:class_groups,id',
            'id_usuario' => 'required|exists:general_users,id',
            'id_programa' => 'required|exists:training_programs,id',
        ]);

        $item = Apprentice::create($request->all());
        return $item;
    }

    public function show($id)
    {
        $item = Apprentice::included()->findOrFail($id);
        return $item;
    }

    public function update(Request $request, Apprentice $apprentice)
    {
        $request->validate([
            'codigo' => 'required|max:255',
            'id_class_group' => 'required|exists:class_groups,id',
            'id_usuario' => 'required|exists:general_users,id',
            'id_programa' => 'required|exists:training_programs,id',
        ]);

        $apprentice->update($request->all());
        return $apprentice;
    }

    public function destroy(Apprentice $apprentice)
    {
        $apprentice->delete();
        return $apprentice;
    }
}
