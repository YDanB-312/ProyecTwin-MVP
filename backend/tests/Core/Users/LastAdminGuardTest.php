<?php

namespace Tests\Core\Users;

use App\Models\GeneralUser;
use App\Support\BorradoCascada;
use App\Support\GuardiaAdministradores;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Http\Exceptions\HttpResponseException;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// La guarda del último admin debe ejecutarse dentro de la transacción que
// modifica/elimina la cuenta, no solo en un COUNT previo.
class LastAdminGuardTest extends TestCase
{
    use DatabaseTransactions;

    private function admin(string $correo): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'Guardia',
            'correo' => $correo,
            'password' => Hash::make('123456'),
            'rol' => 'admin',
            'estado' => true,
        ]);
    }

    public function test_guarda_transaccional_rechaza_dejar_un_solo_admin(): void
    {
        GeneralUser::where('rol', 'admin')->update(['estado' => false]);
        $this->admin('guardia.unico.'.uniqid().'@test.local');

        $this->expectException(HttpResponseException::class);

        DB::transaction(fn () => GuardiaAdministradores::bloquearYAfirmar());
    }

    public function test_guarda_permite_operar_con_dos_admins_activos(): void
    {
        GeneralUser::where('rol', 'admin')->update(['estado' => false]);
        $this->admin('guardia.a.'.uniqid().'@test.local');
        $this->admin('guardia.b.'.uniqid().'@test.local');

        DB::transaction(fn () => GuardiaAdministradores::bloquearYAfirmar());

        $this->assertTrue(true);
    }

    public function test_borrado_cascada_de_admin_usa_la_guarda(): void
    {
        GeneralUser::where('rol', 'admin')->update(['estado' => false]);
        $admin = $this->admin('guardia.cascada.'.uniqid().'@test.local');

        $this->expectException(HttpResponseException::class);

        BorradoCascada::usuario($admin, true);
    }

}
