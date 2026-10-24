<?php

namespace Tests\Unit;

use App\Similarity\Pasajes;
use PHPUnit\Framework\TestCase;

class PasajesTest extends TestCase
{
    private function normalizar(array $pasajes): array
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
        $this->assertSame($this->normalizar($ab), $this->normalizar($ba));
    }

    public function test_el_maximo_se_respeta_tras_unir_ambos_sentidos(): void
    {
        $a = 'Gestionar inventarios y reportes. Controlar el stock del almacén. Registrar las ventas diarias.';
        $b = 'Implementar una plataforma para gestionar inventarios, reportes y trazabilidad. Controlar el stock del almacén. Registrar las ventas diarias.';

        $this->assertLessThanOrEqual(2, count(Pasajes::comunes($a, $b, 2)));
    }
}
