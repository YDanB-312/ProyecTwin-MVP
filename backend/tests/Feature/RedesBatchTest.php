<?php

namespace Tests\Feature;

use App\Models\GeneralUser;
use App\Models\KnowledgeNetwork;
use App\Models\TrainingProgram;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

// Guardado por lote de una red + sus programas en una sola transacción.
class RedesBatchTest extends TestCase
{
    use DatabaseTransactions;

    public function test_guardado_por_lote_crea_renombra_y_elimina(): void
    {
        $admin = GeneralUser::create([
            'nombre' => 'Admin',
            'apellido' => 'Redes',
            'correo' => 'admin.redes.' . uniqid() . '@test.local',
            'password' => Hash::make('clave12345'),
            'rol' => 'admin',
            'estado' => true,
        ]);
        $token = $this->postJson('/v1/auth/login', [
            'correo' => $admin->correo,
            'password' => 'clave12345',
        ])->assertOk()->json('token');

        $red = KnowledgeNetwork::create(['nombre' => 'Red ' . uniqid()]);
        $p1 = TrainingProgram::create([
            'nombre' => 'Programa A',
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);
        $p2 = TrainingProgram::create([
            'nombre' => 'Programa B',
            'nivel' => 'Tecnologo',
            'num_trimestres' => 6,
            'knowledge_network_id' => $red->id,
        ]);

        $res = $this->withToken($token)
            ->putJson("/v1/knowledge-networks/{$red->id}/programas", [
                'nombre' => $red->nombre . ' X',
                'programas' => [
                    ['id' => $p1->id, 'nombre' => 'Programa A renombrado'],
                    ['nombre' => 'Programa nuevo'],
                ],
            ])
            ->assertOk();

        $this->assertSame($red->nombre . ' X', $res->json('red.nombre'));
        $this->assertDatabaseHas('training_programs', ['id' => $p1->id, 'nombre' => 'Programa A renombrado']);
        $this->assertDatabaseMissing('training_programs', ['id' => $p2->id]);
        $this->assertDatabaseHas('training_programs', [
            'nombre' => 'Programa nuevo',
            'knowledge_network_id' => $red->id,
        ]);
    }
}
