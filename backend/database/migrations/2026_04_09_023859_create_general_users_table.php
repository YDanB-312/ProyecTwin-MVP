<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('general_users', function (Blueprint $table) {
            $table->id();
            $table->string('nombre');
            $table->string('apellido');
            $table->string('documento')->nullable();
            $table->string('correo')->unique();
            $table->string('password');
            $table->string('telefono')->nullable();
            $table->date('fecha_nacimiento')->nullable();
            $table->string('foto_url')->nullable();
            $table->enum('rol', ['aprendiz', 'instructor', 'admin']);
            $table->boolean('estado')->default(true);
            $table->boolean('notif_similitud')->default(true);
            $table->boolean('notif_comentarios_instructor')->default(true);
            $table->boolean('notif_recordatorio_avances')->default(true);
            $table->boolean('notif_nuevos_proyectos')->default(true);
            $table->boolean('notif_revisiones_pendientes')->default(true);
            $table->boolean('notif_mensajes_aprendices')->default(true);
            $table->boolean('notif_noticias_sistema')->default(true);
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('general_users');
    }
};
