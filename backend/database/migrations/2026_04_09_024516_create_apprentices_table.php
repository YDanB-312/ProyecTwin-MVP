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
            $table->string('ficha');
            $table->foreignId('id_class_group')->nullable()->constrained('class_groups')->onDelete('cascade');

            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');
            $table->foreignId('id_programa')->constrained('training_programs')->onDelete('cascade');
            $table->string('centro_formacion')->nullable();
            
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('apprentices');
    }
};
