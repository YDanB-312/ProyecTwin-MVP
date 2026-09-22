<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('admins', function (Blueprint $table) {
            $table->id();
            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');
            $table->unique('id_usuario'); // Un perfil por usuario.
            // Centro a cargo del coordinador. Nullable: el superadmin es global.
            // Unique: un solo admin por centro (MySQL admite varios NULL).
            $table->foreignId('training_center_id')->nullable()->unique()->constrained('training_centers')->nullOnDelete();
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('admins');
    }
};
