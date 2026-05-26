<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('notifications', function (Blueprint $table) {
            $table->id();
            $table->text('mensaje');
            $table->enum('tipo', ['similitud', 'revision', 'mensaje', 'sistema']);
            $table->enum('prioridad', ['urgente', 'alta', 'media', 'baja', 'informativa'])->default('media');
            $table->string('url_enlace')->nullable();
            $table->unsignedBigInteger('id_origen')->nullable();
            $table->string('tipo_origen')->nullable();
            $table->boolean('leida')->default(false);
            $table->date('fecha');

            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('notifications');
    }
};
