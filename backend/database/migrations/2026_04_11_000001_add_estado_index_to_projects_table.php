<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // `projects.estado` se filtra en el motor de similitud (corpus = aprobadas),
    // en `resumen` (total propuestas) y en los listados por rol.
    public function up(): void
    {
        Schema::table('projects', function (Blueprint $table) {
            $table->index('estado');
        });
    }

    public function down(): void
    {
        Schema::table('projects', function (Blueprint $table) {
            $table->dropIndex(['estado']);
        });
    }
};
