<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('class_groups', function (Blueprint $table) {
            $table->id();
            $table->string('codigo')->unique();
            $table->foreignId('id_programa')->constrained('training_programs');
            $table->foreignId('id_instructor')->nullable()->constrained('instructors');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('class_groups');
    }
};
