<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Códigos de activación (un solo uso, con vencimiento). Se guardan con
        // hash: el código en claro solo lo conoce quien lo recibe.
        Schema::create('activaciones_cuenta', function (Blueprint $table) {
            $table->id();
            $table->foreignId('id_usuario')->constrained('general_users')->cascadeOnDelete();
            $table->string('codigo_hash');
            $table->timestamp('expira_en');
            $table->timestamp('usado_en')->nullable();
            $table->timestamps();

            $table->index(['id_usuario', 'usado_en']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('activaciones_cuenta');
    }
};
