<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

/**
 * Compatibilidad con bases creadas antes de la activación por correo.
 *
 * El esquema consolidado declara `email_verified_at` y el índice de guardia
 * en la creación inicial de `general_users`. Las instalaciones que ya tenían
 * esa tabla no reciben esas definiciones si la migración inicial ya estaba
 * registrada. Esta migración es idempotente y repara ambas cosas.
 *
 * Las cuentas existentes se marcan como verificadas: fueron creadas antes de
 * que existiera el flujo de activación y no deben quedar bloqueadas. Las
 * cuentas nuevas seguirán naciendo con `email_verified_at = null` desde
 * `AltaUsuario`.
 */
return new class extends Migration
{
    public function up(): void
    {
        if (! Schema::hasColumn('general_users', 'email_verified_at')) {
            Schema::table('general_users', function (Blueprint $table) {
                $table->timestamp('email_verified_at')->nullable()->after('password');
            });
        }

        // La cuenta existente ya estaba operativa antes de este cambio.
        DB::table('general_users')
            ->whereNull('email_verified_at')
            ->update([
                'email_verified_at' => DB::raw('COALESCE(created_at, NOW())'),
            ]);

        $indices = collect(DB::select('SHOW INDEX FROM general_users'))
            ->pluck('Key_name')
            ->filter()
            ->all();

        if (! in_array('general_users_rol_estado_index', $indices, true)) {
            Schema::table('general_users', function (Blueprint $table) {
                $table->index(['rol', 'estado'], 'general_users_rol_estado_index');
            });
        }
    }

    public function down(): void
    {
        // No se elimina la columna ni el índice: forman parte del esquema
        // consolidado para instalaciones nuevas y otros flujos dependen de ellos.
    }
};
