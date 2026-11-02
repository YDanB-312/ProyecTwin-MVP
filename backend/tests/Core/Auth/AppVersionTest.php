<?php

namespace Tests\Core\Auth;

use Tests\TestCase;

// Endpoint público de metadatos Android: versión, hash y URL de descarga.
class AppVersionTest extends TestCase
{
    public function test_expone_la_version_configurada_y_la_descarga_https(): void
    {
        config([
            'app.url' => 'https://proyectwin.test',
            'app_version.android.version' => '1.4.0',
            'app_version.android.version_code' => 5,
            'app_version.android.min_version' => '1.3.0',
            'app_version.android.min_version_code' => 4,
            'app_version.android.enforce_minimum' => false,
            'app_version.android.download_url' => '/apk/ProyecTWin.apk',
            'app_version.android.sha256' => str_repeat('a', 64),
            'app_version.android.size' => 14866767,
            'app_version.android.published_at' => '2026-11-02T19:00:00Z',
            'app_version.android.release_notes' => 'Mejoras de estabilidad',
        ]);

        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJson([
                'latest_version' => '1.4.0',
                'latest_version_code' => 5,
                'minimum_supported_version' => '1.3.0',
                'minimum_supported_version_code' => 4,
                'enforce_minimum' => false,
                'download_url' => 'https://proyectwin.test/apk/ProyecTWin.apk',
                'apk_sha256' => str_repeat('a', 64),
                'apk_size' => 14866767,
                'published_at' => '2026-11-02T19:00:00Z',
                'release_notes' => 'Mejoras de estabilidad',
            ]);
    }

    public function test_valores_por_defecto_con_contrato_completo(): void
    {
        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJsonStructure([
                'latest_version',
                'latest_version_code',
                'minimum_supported_version',
                'minimum_supported_version_code',
                'enforce_minimum',
                'download_url',
                'apk_sha256',
                'apk_size',
                'published_at',
                'release_notes',
            ]);
    }

    public function test_no_anuncia_una_url_insegura(): void
    {
        config([
            'app.url' => 'http://localhost:8000',
            'app_version.android.download_url' => '/apk/ProyecTWin.apk',
        ]);

        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJson(['download_url' => '']);
    }

    public function test_el_apk_autorizado_existe_en_el_repositorio(): void
    {
        $this->assertFileExists(public_path('apk/ProyecTWin.apk'));
    }

    public function test_obligatoriedad_explicita_por_version_code(): void
    {
        config([
            'app_version.android.enforce_minimum' => true,
            'app_version.android.version' => '2.0.0',
            'app_version.android.version_code' => 8,
            'app_version.android.min_version' => '1.5.0',
            'app_version.android.min_version_code' => 6,
        ]);

        $this->getJson('/v1/app-version/android')
            ->assertOk()
            ->assertJson([
                'latest_version' => '2.0.0',
                'latest_version_code' => 8,
                'minimum_supported_version' => '1.5.0',
                'minimum_supported_version_code' => 6,
                'enforce_minimum' => true,
            ]);
    }
}
