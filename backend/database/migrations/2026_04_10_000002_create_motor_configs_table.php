<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// Parámetros del motor de similitudes (umbral + ventana del corpus).
// Tabla de fila única (id 1): el admin los ajusta desde ConfigSimilitud.
return new class extends Migration
{
    public function up(): void
    {
        Schema::create('motor_configs', function (Blueprint $table) {
            $table->id();
            $table->float('umbral')->default(0.2);
            $table->integer('meses')->default(12);
            // Una fila por centro. La fila con NULL es el valor por defecto
            // (global) y la heredan los centros que no han definido el suyo.
            $table->foreignId('training_center_id')->nullable()->unique()->constrained('training_centers')->nullOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('motor_configs');
    }
};
