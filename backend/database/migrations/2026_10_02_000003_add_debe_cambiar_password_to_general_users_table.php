<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Cuentas con clave temporal (creadas/reiniciadas por admin) deben
        // cambiarla en el primer ingreso.
        Schema::table('general_users', function (Blueprint $table) {
            $table->boolean('debe_cambiar_password')->default(false)->after('estado');
        });
    }

    public function down(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->dropColumn('debe_cambiar_password');
        });
    }
};
