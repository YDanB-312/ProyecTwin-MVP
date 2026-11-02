<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// Las bases creadas con versiones anteriores no tienen `email_verified_at`:
// el flujo de activación lo necesita. La migración original ya incluía la
// columna para instalaciones nuevas, así que esta migración es idempotente y
// solo actúa sobre instalaciones existentes.
return new class extends Migration
{
    public function up(): void
    {
        if (Schema::hasColumn('general_users', 'email_verified_at')) {
            return;
        }

        Schema::table('general_users', function (Blueprint $table) {
            $table->timestamp('email_verified_at')->nullable()->after('password');
        });
    }

    public function down(): void
    {
        // No eliminamos la columna: la migración original también la declara
        // para instalaciones nuevas y un rollback selectivo de esta migración
        // no debe romper el flujo de activación.
    }
};
