<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Bitácora de acciones sensibles. Inmutable: solo se inserta y se consulta.
    public function up(): void
    {
        Schema::create('audit_logs', function (Blueprint $table) {
            $table->id();
            // Si se borra el actor, la fila se conserva (traza histórica).
            $table->foreignId('id_usuario')->nullable()->constrained('general_users')->nullOnDelete();
            $table->string('accion');
            $table->string('entidad')->nullable();
            $table->unsignedBigInteger('entidad_id')->nullable();
            $table->json('detalle')->nullable();
            $table->string('ip')->nullable();
            $table->timestamps();

            $table->index(['entidad', 'entidad_id']);
            $table->index('accion');
            $table->index('created_at');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('audit_logs');
    }
};
