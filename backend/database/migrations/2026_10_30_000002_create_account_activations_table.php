<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('account_activations', function (Blueprint $table) {
            $table->id();
            // Una sola activación por cuenta: el reenvío reemplaza el token.
            $table->foreignId('user_id')->unique()->constrained('general_users')->cascadeOnDelete();
            $table->string('token_hash', 64)->unique();
            // DATETIME evita el ON UPDATE CURRENT_TIMESTAMP que MySQL añade a
            // TIMESTAMP y que reescribía la expiración al enviar el correo.
            $table->dateTime('expires_at');
            $table->timestamp('sent_at')->nullable();
            $table->timestamp('used_at')->nullable();
            $table->string('error', 500)->nullable();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('account_activations');
    }
};
