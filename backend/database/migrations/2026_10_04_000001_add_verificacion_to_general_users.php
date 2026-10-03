<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Verificación de cuentas (aprendiz/instructor): viven en general_users
    // porque el perfil de aprendiz nace al unirse a una ficha, no al registrarse.
    // El documento (PDF) que soporta el rol se guarda como archivo local y aquí
    // solo queda su ruta. Datos existentes/seed quedan verificados.
    public function up(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->enum('estado_verificacion', ['pendiente', 'verificado', 'rechazado'])
                ->default('verificado')
                ->after('estado');
            $table->string('motivo_rechazo', 500)->nullable()->after('estado_verificacion');
            $table->string('soporte_path')->nullable()->after('motivo_rechazo');
        });
    }

    public function down(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->dropColumn(['estado_verificacion', 'motivo_rechazo', 'soporte_path']);
        });
    }
};
