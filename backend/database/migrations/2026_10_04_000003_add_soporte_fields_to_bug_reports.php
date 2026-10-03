<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Soporte por conflictos de ficha: número involucrado, motivo y la respuesta
    // con la que el admin cierra la solicitud. Se reutiliza `bug_reports` como
    // bandeja de soporte (no se crean tablas nuevas).
    public function up(): void
    {
        Schema::table('bug_reports', function (Blueprint $table) {
            $table->string('numero_ficha', 30)->nullable()->after('titulo');
            $table->string('motivo', 120)->nullable()->after('numero_ficha');
            $table->text('respuesta')->nullable()->after('estado');
        });
    }

    public function down(): void
    {
        Schema::table('bug_reports', function (Blueprint $table) {
            $table->dropColumn(['numero_ficha', 'motivo', 'respuesta']);
        });
    }
};
