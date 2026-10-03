<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Paginación opt-in del listado de usuarios, con filtros resueltos en servidor.
class PaginacionTest extends TestCase
{
    use DatabaseTransactions;

    private function usuario(string $rol): GeneralUser
    {
        return GeneralUser::create([
            'nombre' => ucfirst($rol),
            'apellido' => 'Pagina',
            'correo' => $rol . '.' . uniqid() . '@test.local',
            'password' => Hash::make('123456'),
            'rol' => $rol,
            'estado' => true,
        ]);
    }

    private function token(GeneralUser $user): string
    {
        return $this->postJson('/v1/auth/login', ['correo' => $user->correo, 'password' => '123456'])
            ->assertOk()->json('token');
    }

    public function test_usuarios_pagina_cuando_se_pide_page(): void
    {
        $admin = $this->usuario('admin');
        for ($i = 0; $i < 3; $i++) $this->usuario('aprendiz');

        $res = $this->withToken($this->token($admin))
            ->getJson('/v1/general-users?page=1&per_page=2')
            ->assertOk()
            ->assertJsonStructure(['data', 'total', 'last_page', 'current_page']);

        $this->assertCount(2, $res->json('data'));
        $this->assertGreaterThanOrEqual(4, $res->json('total'));
    }

    public function test_sin_page_devuelve_la_lista_completa(): void
    {
        $admin = $this->usuario('admin');

        $res = $this->withToken($this->token($admin))->getJson('/v1/general-users')->assertOk();

        $this->assertIsArray($res->json());
        $this->assertArrayNotHasKey('data', $res->json());
    }

    public function test_el_filtro_sin_ficha_encuentra_cuentas_sin_perfil(): void
    {
        $admin = $this->usuario('admin');
        $sinFicha = $this->usuario('aprendiz');

        $res = $this->withToken($this->token($admin))
            ->getJson('/v1/general-users?ficha_id=sin&page=1&per_page=100')
            ->assertOk();

        $ids = collect($res->json('data'))->pluck('id');
        $this->assertTrue($ids->contains($sinFicha->id));
    }
}
