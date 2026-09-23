<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Ficha de formación (grupo). `estado` incluye 'archivado': cierra la
        // ficha conservando historial y bloqueando nuevas uniones.
        Schema::create('class_groups', function (Blueprint $table) {
            $table->id();
            $table->string('codigo')->unique();
            $table->string('numero')->nullable();
            $table->string('nombre');
            $table->enum('estado', ['activo', 'inactivo', 'finalizado', 'archivado'])->default('activo');
            $table->foreignId('id_programa')->constrained('training_programs');
            $table->foreignId('id_instructor')->constrained('instructors');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('class_groups');
    }
};
