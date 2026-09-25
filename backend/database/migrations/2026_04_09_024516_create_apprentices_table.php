<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('apprentices', function (Blueprint $table) {
            $table->id();
            $table->string('codigo')->unique(); // Código de aprendiz único.
            $table->foreignId('id_class_group')->nullable()->constrained('class_groups')->onDelete('cascade');

            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');
            // El programa se deriva de la ficha: sin ficha puede ser nulo. La FK
            // es restrict para no arrastrar aprendices al borrar un programa.
            $table->foreignId('id_programa')->nullable()->constrained('training_programs')->restrictOnDelete();
            $table->unique('id_usuario'); // Un perfil por usuario.

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('apprentices');
    }
};
