<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// Similitudes vigentes vs históricas: una fila representa el contenido de una
// propuesta analizado en un momento dado. Al rechazar/reenviar, los pares de la
// versión anterior se conservan como evidencia (vigente = false) y la nueva
// detección crea filas vigentes. Se reemplaza el unique del par por un índice
// normal para permitir varias versiones del mismo par.
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('similarities', function (Blueprint $table) {
            $table->boolean('vigente')->default(true)->index();
            // El índice normal reemplaza al unique: permite varias versiones del
            // mismo par y sigue cubriendo la FK de id_proyecto_1.
            $table->index(['id_proyecto_1', 'id_proyecto_2']);
        });

        Schema::table('similarities', function (Blueprint $table) {
            $table->dropUnique(['id_proyecto_1', 'id_proyecto_2']);
        });
    }

    public function down(): void
    {
        Schema::table('similarities', function (Blueprint $table) {
            $table->unique(['id_proyecto_1', 'id_proyecto_2']);
        });

        Schema::table('similarities', function (Blueprint $table) {
            $table->dropIndex(['id_proyecto_1', 'id_proyecto_2']);
            $table->dropColumn('vigente');
        });
    }
};
