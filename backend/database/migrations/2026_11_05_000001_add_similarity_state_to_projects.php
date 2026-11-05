<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('projects', function (Blueprint $table) {
            $table->string('similitud_estado', 24)
                ->default('pendiente')
                ->after('huella_envio')
                ->index();
            $table->string('similitud_hash', 64)
                ->nullable()
                ->after('similitud_estado');
            $table->timestamp('similitud_actualizada_en')
                ->nullable()
                ->after('similitud_hash');
        });
    }

    public function down(): void
    {
        Schema::table('projects', function (Blueprint $table) {
            $table->dropIndex(['similitud_estado']);
            $table->dropColumn([
                'similitud_estado',
                'similitud_hash',
                'similitud_actualizada_en',
            ]);
        });
    }
};
