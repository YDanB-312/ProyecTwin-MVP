<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Propuesta de proyecto. Solo se persiguen los datos que la UI captura
        // y muestra: objetivo general + objetivos específicos (no el json
        // legacy `objetivos`). `estado` refleja el modelo real de 3 estados.
        Schema::create('projects', function (Blueprint $table) {
            $table->id();
            $table->string('titulo');
            $table->text('resumen');
            $table->text('palabras_clave')->nullable();
            $table->string('area_aplicacion');
            $table->text('objetivo_general')->nullable();
            $table->json('objetivos_especificos')->nullable();
            $table->enum('estado', ['pendiente', 'aprobado', 'rechazado'])->default('pendiente');

            $table->foreignId('id_creador')->constrained('general_users')->onDelete('cascade');
            $table->foreignId('id_instructor_asignado')->nullable()->constrained('instructors')->onDelete('set null');
            $table->foreignId('id_class_group')->nullable()->constrained('class_groups')->nullOnDelete();

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('projects');
    }
};
