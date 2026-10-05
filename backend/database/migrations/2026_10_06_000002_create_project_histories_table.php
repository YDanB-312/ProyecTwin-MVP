<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Evolución del proyecto (visible para participantes e instructor), separada
    // de audit_logs (seguridad/administración).
    public function up(): void
    {
        Schema::create('project_histories', function (Blueprint $table) {
            $table->id();
            $table->foreignId('id_proyecto')->constrained('projects')->cascadeOnDelete();
            $table->foreignId('id_usuario')->nullable()->constrained('general_users')->nullOnDelete();
            $table->string('accion');
            $table->json('detalle')->nullable();
            $table->timestamps();
            $table->index(['id_proyecto', 'created_at']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('project_histories');
    }
};
