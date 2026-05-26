<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('bug_reports', function (Blueprint $table) {
            $table->id();
            $table->text('descripcion');
            $table->string('tipo')->nullable();
            $table->string('url_evidencia')->nullable();
            $table->string('estado');
            $table->date('fecha');

            $table->foreignId('id_usuario')->constrained('general_users')->onDelete('cascade');
            $table->foreignId('id_admin')->nullable()->constrained('admins')->onDelete('set null');

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('bug_reports');
    }
};
