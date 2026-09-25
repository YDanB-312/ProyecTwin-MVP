<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Observaciones (hilo de comentarios) sobre una propuesta.
        Schema::create('comments', function (Blueprint $table) {
            $table->id();
            $table->text('texto');
            $table->foreignId('id_proyecto')->constrained('projects')->onDelete('cascade');
            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');
            // Borrar una observación raíz borra su hilo completo (respuestas).
            $table->foreignId('respuesta_a')->nullable()->constrained('comments')->cascadeOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('comments');
    }
};
