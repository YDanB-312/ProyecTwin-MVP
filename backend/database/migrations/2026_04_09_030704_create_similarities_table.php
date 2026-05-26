<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('similarities', function (Blueprint $table) {
            $table->id();
            $table->float('porcentaje_detectado');
            $table->enum('estado', ['pendiente', 'revisada', 'resuelta'])->default('pendiente');
            $table->json('detalles')->nullable();
            $table->date('fecha_deteccion')->nullable();
            $table->enum('urgencia', ['baja', 'media', 'alta', 'urgente'])->default('media');

            $table->foreignId('id_proyecto_1')->constrained('projects')->onDelete('cascade');
            $table->foreignId('id_proyecto_2')->constrained('projects')->onDelete('cascade');
            $table->foreignId('id_instructor')->nullable()->constrained('instructors')->nullOnDelete();

            $table->unique(['id_proyecto_1', 'id_proyecto_2']);
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('similarities');
    }
};
