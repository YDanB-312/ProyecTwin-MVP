<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        // Padrón institucional: única fuente legítima de identidad. El registro
        // solo puede crear cuentas para personas previamente cargadas aquí.
        Schema::create('padron_usuarios', function (Blueprint $table) {
            $table->id();
            $table->string('tipo_documento', 5); // CC, TI, CE, PA
            $table->string('numero_documento', 11);
            $table->string('nombre');
            $table->string('apellido');
            $table->string('correo')->unique();
            $table->enum('rol', ['aprendiz', 'instructor', 'admin']);
            // Programa/ficha a los que pertenece según matrícula (rol aprendiz).
            $table->foreignId('id_programa')->nullable()->constrained('training_programs')->nullOnDelete();
            $table->foreignId('id_class_group')->nullable()->constrained('class_groups')->nullOnDelete();
            // Cuenta reclamada por esta persona (si ya se registró).
            $table->foreignId('id_usuario')->nullable()->constrained('general_users')->nullOnDelete();
            $table->boolean('activo')->default(true);
            $table->timestamps();

            $table->unique(['tipo_documento', 'numero_documento']);
            $table->unique('id_usuario');
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('padron_usuarios');
    }
};
