<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Tabla pivote: equipo que participa en una propuesta.
        Schema::create('apprentice_projects', function (Blueprint $table) {
            $table->id();
            $table->foreignId('id_aprendiz')->constrained('apprentices')->onDelete('cascade');
            $table->foreignId('id_proyecto')->constrained('projects')->onDelete('cascade');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('apprentice_projects');
    }
};
