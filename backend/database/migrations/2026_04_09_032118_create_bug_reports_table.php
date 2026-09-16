<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Reporte de falla enviado por un usuario. La prioridad mostrada en la
        // UI se deriva del tipo; no se persiste.
        Schema::create('bug_reports', function (Blueprint $table) {
            $table->id();
            $table->string('titulo')->nullable();
            $table->text('descripcion');
            $table->enum('tipo', ['sistema', 'proyecto', 'datos', 'bug_ui', 'error_datos', 'rendimiento', 'seguridad', 'otro']);
            $table->enum('estado', ['pendiente', 'en_revision', 'resuelto', 'cerrado', 'rechazado'])->default('pendiente');
            $table->date('fecha');

            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('bug_reports');
    }
};
