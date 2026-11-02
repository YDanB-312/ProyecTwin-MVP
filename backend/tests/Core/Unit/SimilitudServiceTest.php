<?php

namespace Tests\Core\Unit;

use App\Models\Project;
use App\Services\SimilitudService;
use App\Similarity\Pasajes;
use PHPUnit\Framework\TestCase;

// Pruebas del motor de similitud: verifica que capte tanto copias parciales
// (n-gramas de caracteres) como temas parecidos con otro vocabulario (palabras).
class SimilitudServiceTest extends TestCase
{
    private function proyecto(int $id, string $titulo, string $claves, string $resumen, string $objetivo = ''): Project
    {
        $p = new Project([
            'titulo' => $titulo,
            'palabras_clave' => $claves,
            'resumen' => $resumen,
            'area_aplicacion' => 'Tecnología e Informática',
            'objetivo_general' => $objetivo,
        ]);
        $p->id = $id;
        return $p;
    }

    // Copia casi literal (solo se quitaron algunas palabras): debe puntuar alto.
    public function test_detecta_copia_parcial(): void
    {
        $base = $this->proyecto(1, 'Sistema de Gestión de Inventarios', 'inventarios, stock, control, reportes',
            'Herramienta para controlar el inventario y las ventas del almacén con alertas de stock y reportes de trazabilidad.');
        $copia = $this->proyecto(2, 'Sistema de Gestión de Inventarios', 'inventarios, stock, control',
            'Herramienta para controlar el inventario y las ventas del almacén con alertas y reportes.');

        $pares = SimilitudService::puntaje($base, [$base, $copia]);
        $this->assertNotEmpty($pares);
        $this->assertGreaterThan(0.7, $pares[0]['score'], 'La copia parcial debe superar 0.7');
    }

    // Mismo tema con sinónimos (existencias≈inventario, plataforma≈sistema):
    // la señal de palabras debe aportar similitud.
    public function test_usa_sinonimos_de_dominio(): void
    {
        $a = $this->proyecto(1, 'Sistema de Inventarios', 'inventarios, stock, control',
            'Controlar el inventario del almacén.');
        $b = $this->proyecto(2, 'Plataforma de Existencias', 'existencias, control',
            'Controlar las existencias del almacén.');

        $pares = SimilitudService::puntaje($a, [$a, $b]);
        $this->assertGreaterThan(0.2, $pares[0]['detalles']['palabras'] / 100, 'Los sinónimos deben dar señal de palabras');
    }

    // Textos sin relación: el puntaje debe ser bajo.
    public function test_textos_distintos_puntuan_bajo(): void
    {
        $a = $this->proyecto(1, 'Sistema de Inventarios', 'inventarios, stock',
            'Controlar el inventario del almacén.');
        $b = $this->proyecto(2, 'App de Turismo Local', 'turismo, rutas, cultura',
            'Mostrar sitios turísticos y eventos culturales de la región.');

        $pares = SimilitudService::puntaje($a, [$a, $b]);
        $this->assertLessThan(0.4, $pares[0]['score'], 'Temas distintos deben puntuar bajo');
    }

    // El orden distinto de las palabras no debe hundir la similitud.
    public function test_el_score_es_simetrico_al_invertir_el_orden(): void
    {
        $a = $this->proyecto(1, 'Control de Inventarios del Almacén', 'inventario, almacén',
            'Registrar entradas y salidas de mercancía del almacén.');
        $b = $this->proyecto(2, 'Almacén con Control de Inventarios', 'almacén, inventario',
            'Registrar salidas y entradas de mercancía del almacén.');

        $ab = SimilitudService::puntaje($a, [$a, $b])[0];
        $ba = SimilitudService::puntaje($b, [$b, $a])[0];

        $this->assertSame($ab['porcentaje'], $ba['porcentaje']);
        $this->assertSame($ab['detalles']['palabras'], $ba['detalles']['palabras']);
        $this->assertSame($ab['detalles']['caracteres'], $ba['detalles']['caracteres']);
        $this->assertSame($ab['detalles']['cobertura'], $ba['detalles']['cobertura']);
    }

    private function normalizarPasajes(array $pasajes): array
    {
        $normalizados = array_map(function (array $pasaje) {
            [$a, $b] = strcmp($pasaje['a'], $pasaje['b']) <= 0
                ? [$pasaje['a'], $pasaje['b']]
                : [$pasaje['b'], $pasaje['a']];

            return ['a' => $a, 'b' => $b, 'score' => $pasaje['score']];
        }, $pasajes);

        usort($normalizados, fn (array $x, array $y) => strcmp($x['a']."\0".$x['b'], $y['a']."\0".$y['b']));

        return $normalizados;
    }

    public function test_los_pasajes_son_simetricos(): void
    {
        $a = 'Gestionar inventarios y reportes.';
        $b = 'Implementar una plataforma web para gestionar inventarios, reportes y trazabilidad en el almacén.';

        $ab = Pasajes::comunes($a, $b);
        $ba = Pasajes::comunes($b, $a);

        $this->assertNotEmpty($ab);
        $this->assertSame($this->normalizarPasajes($ab), $this->normalizarPasajes($ba));
    }

    public function test_el_maximo_de_pasajes_se_respeta_tras_unir_ambos_sentidos(): void
    {
        $a = 'Gestionar inventarios y reportes. Controlar el stock del almacén. Registrar las ventas diarias.';
        $b = 'Implementar una plataforma para gestionar inventarios, reportes y trazabilidad. Controlar el stock del almacén. Registrar las ventas diarias.';

        $this->assertLessThanOrEqual(2, count(Pasajes::comunes($a, $b, 2)));
    }
}
