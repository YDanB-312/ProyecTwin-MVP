<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// La guarda del último administrador bloquea las filas activas por rol/estado.
// El índice reduce el rango bloqueado y evita que el SELECT ... FOR UPDATE
// tenga que recorrer/bloquear toda la tabla.
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->index(['rol', 'estado'], 'general_users_rol_estado_index');
        });
    }

    public function down(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->dropIndex('general_users_rol_estado_index');
        });
    }
};
