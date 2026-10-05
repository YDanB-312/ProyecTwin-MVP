<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

// ProyecTwin no utiliza el concepto de número de trimestres: se retira el campo
// heredado. La migración original (create_training_programs_table) no se toca.
return new class extends Migration
{
    public function up(): void
    {
        Schema::table('training_programs', function (Blueprint $table) {
            $table->dropColumn('num_trimestres');
        });
    }

    public function down(): void
    {
        // La columna original era NOT NULL sin default; se restaura nullable
        // para no inventar valores (el dato ya no forma parte del modelo).
        Schema::table('training_programs', function (Blueprint $table) {
            $table->integer('num_trimestres')->nullable();
        });
    }
};
