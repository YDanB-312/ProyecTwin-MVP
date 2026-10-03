<?php

use App\Support\Credenciales;
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    // Flujo institucional: el admin crea las cuentas. Se agregan las
    // credenciales: username (login), documento único, y la contraseña temporal
    // encriptada que debe cambiarse en el primer ingreso.
    public function up(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->string('tipo_documento', 20)->nullable()->after('apellido');
            $table->string('numero_documento', 40)->nullable()->unique()->after('tipo_documento');
            $table->string('username')->nullable()->unique()->after('correo');
            $table->boolean('must_change_password')->default(false)->after('password');
            // Contraseña temporal encriptada (Crypt): solo para exportar
            // credenciales; se limpia cuando el usuario define la definitiva.
            $table->text('password_temporal')->nullable()->after('must_change_password');
        });

        // Backfill: los usuarios existentes reciben username generado.
        foreach (DB::table('general_users')->orderBy('id')->get() as $usuario) {
            DB::table('general_users')
                ->where('id', $usuario->id)
                ->update(['username' => Credenciales::username($usuario->nombre, $usuario->apellido)]);
        }
    }

    public function down(): void
    {
        Schema::table('general_users', function (Blueprint $table) {
            $table->dropUnique(['username']);
            $table->dropUnique(['numero_documento']);
            $table->dropColumn(['tipo_documento', 'numero_documento', 'username', 'must_change_password', 'password_temporal']);
        });
    }
};
