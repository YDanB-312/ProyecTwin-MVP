<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('training_programs', function (Blueprint $table) {
            // FK hacia knowledge_networks; nullable para compatibilidad con datos existentes.
            // Se mantiene la columna `red` (string) por compatibilidad; la FK es la fuente oficial.
            $table->foreignId('knowledge_network_id')
                ->nullable()
                ->after('red')
                ->constrained('knowledge_networks')
                ->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::table('training_programs', function (Blueprint $table) {
            $table->dropConstrainedForeignId('knowledge_network_id');
        });
    }
};
