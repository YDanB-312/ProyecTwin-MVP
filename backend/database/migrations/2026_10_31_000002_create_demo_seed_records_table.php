<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// Registro técnico de propiedad de los datos de demostración. Permite que los
// seeders sean idempotentes sin añadir columnas `demo` a todas las tablas de
// negocio ni tocar registros creados por usuarios reales.
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('demo_seed_records', function (Blueprint $table) {
            $table->id();
            $table->string('tipo', 60);
            $table->string('clave', 120);
            $table->unsignedBigInteger('entidad_id')->nullable();
            $table->timestamps();

            $table->unique(['tipo', 'clave']);
            $table->index(['tipo', 'entidad_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('demo_seed_records');
    }
};
