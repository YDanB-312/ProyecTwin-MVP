<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('projects', function (Blueprint $table) {
            $table->id();
            $table->string('titulo');
            $table->string('tipo_proyecto')->nullable();
            $table->text('resumen');
            $table->text('palabras_clave');
            $table->string('area_aplicacion');
            $table->text('tecnologias');
            $table->text('objetivos');
            $table->text('entregables');
            $table->string('url_logo')->nullable();
            $table->enum('estado', ['borrador', 'pendiente', 'en_revision', 'aprobado', 'rechazado', 'requiere_ajustes'])->default('borrador');
            $table->text('observaciones')->nullable();

            $table->foreignId('id_creador')->constrained('general_users')->onDelete('cascade');
            $table->foreignId('id_instructor_asignado')->nullable()->constrained('instructors')->onDelete('set null');

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('projects');
    }
};
