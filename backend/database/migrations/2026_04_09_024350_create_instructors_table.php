<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('instructors', function (Blueprint $table) {
            $table->id();
            $table->string('especialidad');
            $table->string('centro_formacion');
            $table->text('biografia_profesional')->nullable();
            $table->string('codigo_instructor')->unique();
            $table->date('fecha_ingreso');
            $table->integer('limite_proyectos')->default(5);
            $table->integer('tiempo_maximo_revision')->default(3);
            $table->text('plantilla_comentarios')->nullable();

            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');
            
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('instructors');
    }
};
