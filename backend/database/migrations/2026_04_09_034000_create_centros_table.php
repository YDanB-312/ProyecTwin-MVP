<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('centros', function (Blueprint $table) {
            $table->id();
            $table->string('nombre')->unique();
            $table->string('ciudad')->nullable();
            $table->timestamps();
        });

        Schema::table('class_groups', function (Blueprint $table) {
            $table->foreignId('centro_id')->nullable()->after('id_instructor')->constrained('centros')->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::table('class_groups', function (Blueprint $table) {
            $table->dropForeign(['centro_id']);
            $table->dropColumn('centro_id');
        });
        Schema::dropIfExists('centros');
    }
};
