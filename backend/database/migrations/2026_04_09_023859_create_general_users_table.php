<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Cuentas base del sistema. `rol` y `estado` quedan en español por
        // convención del dominio (valores visibles al usuario).
        Schema::create('general_users', function (Blueprint $table) {
            $table->id();
            $table->string('nombre');
            $table->string('apellido');
            $table->string('correo')->unique();
            $table->string('password');
            // text: admite data URL (foto subida desde el navegador) o URL externa.
            $table->text('foto_url')->nullable();
            $table->enum('rol', ['aprendiz', 'instructor', 'admin', 'superadmin']);
            $table->boolean('estado')->default(true);
            // Necesario para el flujo de restablecer contraseña (broker de Laravel).
            $table->rememberToken();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('general_users');
    }
};
