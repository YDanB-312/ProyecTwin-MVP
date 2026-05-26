<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('assessments', function (Blueprint $table) {
            $table->id();
            $table->string('decision');
            $table->decimal('puntaje', 3, 1)->nullable();
            $table->string('tipo_revision')->nullable();
            $table->text('observacion')->nullable();
            $table->date('fecha_revision');
            $table->integer('tiempo_respuesta_dias');

            $table->foreignId('id_proyecto')->constrained('projects')->onDelete('cascade');
            $table->foreignId('id_instructor')->constrained('instructors')->onDelete('cascade');

            $table->unique(['id_proyecto', 'tipo_revision']);

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('assessments');
    }
};
