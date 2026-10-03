<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Fichas: número como identificador único (UNIQUE) y estado 'anulada' para
    // las que tienen historial y no deben borrarse. Al anular se libera el
    // número (queda NULL), por eso el índice único convive con él.
    public function up(): void
    {
        // 1) Tercer estado: 'anulada' (ficha con historial, no acepta uniones).
        if (DB::getDriverName() === 'mysql') {
            DB::statement("ALTER TABLE class_groups MODIFY estado ENUM('activo','finalizado','anulada') NOT NULL DEFAULT 'activo'");
        }

        // 2) Limpieza previa: vacíos y duplicados pasan a NULL (el índice único
        //    no admite repetidos; NULL sí se repite). Se conserva el de menor id.
        DB::table('class_groups')->where('numero', '')->update(['numero' => null]);
        $duplicados = DB::table('class_groups')
            ->select('numero')
            ->whereNotNull('numero')
            ->groupBy('numero')
            ->havingRaw('COUNT(*) > 1')
            ->pluck('numero');
        foreach ($duplicados as $numero) {
            $ids = DB::table('class_groups')->where('numero', $numero)->orderBy('id')->pluck('id');
            DB::table('class_groups')->whereIn('id', $ids->slice(1)->all())->update(['numero' => null]);
        }

        // 3) El número de ficha es único dentro de ProyecTwin.
        Schema::table('class_groups', function (Blueprint $table) {
            $table->unique('numero');
        });
    }

    public function down(): void
    {
        Schema::table('class_groups', function (Blueprint $table) {
            $table->dropUnique(['numero']);
        });

        if (DB::getDriverName() === 'mysql') {
            DB::statement("ALTER TABLE class_groups MODIFY estado ENUM('activo','finalizado') NOT NULL DEFAULT 'activo'");
        }
    }
};
