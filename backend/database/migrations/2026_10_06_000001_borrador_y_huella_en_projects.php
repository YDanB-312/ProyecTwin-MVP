<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Flujo de propuestas: nace en borrador (aún no enviada) y guarda la huella
    // del último envío para bloquear reenvíos sin cambios reales.
    public function up(): void
    {
        if (DB::getDriverName() === 'mysql') {
            DB::statement("ALTER TABLE projects MODIFY estado ENUM('borrador','pendiente','aprobado','rechazado') NOT NULL DEFAULT 'pendiente'");
            // El borrador admite campos incompletos: se validan al enviar.
            DB::statement("ALTER TABLE projects MODIFY titulo VARCHAR(255) NULL, MODIFY resumen TEXT NULL, MODIFY area_aplicacion VARCHAR(255) NULL");
        }

        Schema::table('projects', function (Blueprint $table) {
            $table->string('huella_envio', 40)->nullable()->after('estado');
        });
    }

    public function down(): void
    {
        Schema::table('projects', function (Blueprint $table) {
            $table->dropColumn('huella_envio');
        });

        if (DB::getDriverName() === 'mysql') {
            DB::statement("UPDATE projects SET titulo = COALESCE(titulo, ''), resumen = COALESCE(resumen, ''), area_aplicacion = COALESCE(area_aplicacion, '')");
            DB::statement("ALTER TABLE projects MODIFY titulo VARCHAR(255) NOT NULL, MODIFY resumen TEXT NOT NULL, MODIFY area_aplicacion VARCHAR(255) NOT NULL");
            DB::statement("ALTER TABLE projects MODIFY estado ENUM('pendiente','aprobado','rechazado') NOT NULL DEFAULT 'pendiente'");
        }
    }
};
