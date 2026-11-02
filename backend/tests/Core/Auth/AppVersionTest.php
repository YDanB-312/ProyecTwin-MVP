<?php

namespace Tests\Core\Auth;

use Tests\TestCase;

// Endpoint público de metadatos Android. El APK histórico no se anuncia ni se
// sirve: download_url permanece vacío hasta autorización de Oscar.
class AppVersionTest extends TestCase
{
    public function test_expone_la_version_configurada_sin_descarga(): void
    {
        config([
            'app_version.android.version' => '1.10.0',
            'app_version.android.min_version' => '1.0.0',
            'app_version.android.enforce_minimum' => false,
            'app_version.android.download_url' => 'https://example.test/apk/proyecttwin.apk',
            'app_version.android.release_notes' => 'Mejoras de estabilidad',
        ]);

        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJson([
                'latest_version' => '1.10.0',
                'minimum_supported_version' => '1.0.0',
                'enforce_minimum' => false,
                'download_url' => '',
                'release_notes' => 'Mejoras de estabilidad',
            ]);
    }

    public function test_valores_por_defecto_con_contrato_completo(): void
    {
        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJsonStructure([
                'latest_version',
                'minimum_supported_version',
                'enforce_minimum',
                'download_url',
                'release_notes',
            ])
            ->assertJson(['download_url' => '']);
    }

    public function test_no_anuncia_descarga_aunque_la_configuracion_la_defina(): void
    {
        config([
            'app.url' => 'https://proyectwin.test',
            'app_version.android.download_url' => '/apk/ProyecTWin.apk',
        ]);

        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJson(['download_url' => '']);
    }

    public function test_no_publica_el_apk_historico_por_ruta_publica(): void
    {
        $this->get('/apk/ProyecTWin.apk')->assertNotFound();
        $this->get('/apk/')->assertNotFound();
        $this->get('/apk/otro.apk')->assertNotFound();
    }

    public function test_obligatoriedad_explicita(): void
    {
        config([
            'app_version.android.enforce_minimum' => true,
            'app_version.android.version' => '2.0.0',
            'app_version.android.min_version' => '1.5.0',
        ]);

        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJson([
                'latest_version' => '2.0.0',
                'minimum_supported_version' => '1.5.0',
                'enforce_minimum' => true,
            ]);
    }
}
