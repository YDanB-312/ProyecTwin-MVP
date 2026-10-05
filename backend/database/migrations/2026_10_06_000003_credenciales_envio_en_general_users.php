<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Estado del envío de credenciales al correo personal (permite reintentar
    // sin perder la cuenta si el correo falla).
    public function up(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->timestamp('credenciales_enviadas_en')->nullable()->after('password_temporal');
            $table->string('credenciales_error', 500)->nullable()->after('credenciales_enviadas_en');
        });
    }

    public function down(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->dropColumn(['credenciales_enviadas_en', 'credenciales_error']);
        });
    }
};
