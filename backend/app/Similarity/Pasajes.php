<?php

namespace App\Similarity;

use App\Services\SimilitudService;

// Coincidencia por PASAJES (estilo Turnitin): trocea cada propuesta en oraciones
// y busca qué oración de una aparece (casi) en la otra. Devuelve los fragmentos
// comunes para poder explicar *qué* se parece, no solo *cuánto*.
class Pasajes
{
    private const MIN_LARGO = 15;   // oraciones muy cortas se descartan

    // Divide un texto en oraciones razonables.
    public static function oraciones(string $texto): array
    {
        $texto = preg_replace('/\s+/', ' ', (string) $texto);
        $partes = preg_split('/(?<=[\.;:!?])\s+/u', $texto) ?: [];
        $out = [];
        foreach ($partes as $p) {
            $p = trim($p);
            if (mb_strlen($p) >= self::MIN_LARGO) $out[] = $p;
        }
        return $out;
    }

    // Mejores pares de oraciones coincidentes entre $a y $b.
    // Contención = fracción de tokens de la oración de A presentes en la de B.
    public static function comunes(string $a, string $b, int $max = 5, float $umbral = 0.5): array
    {
        $sa = self::oraciones($a);
        $sb = self::oraciones($b);
        if (!$sa || !$sb) return [];

        $tok = fn (string $s) => array_values(array_unique(SimilitudService::tokenizar($s)));
        $ta = array_map($tok, $sa);
        $tb = array_map($tok, $sb);

        $res = [];
        $agregar = function (int $i, int $j, float $score) use (&$res, $sa, $sb, $umbral) {
            if ($score < $umbral) return;

            $clave = $sa[$i] . "\0" . $sb[$j];
            $porcentaje = (int) round($score * 100);

            if (!isset($res[$clave]) || $porcentaje > $res[$clave]['score']) {
                $res[$clave] = [
                    'a' => $sa[$i],
                    'b' => $sb[$j],
                    'score' => $porcentaje,
                ];
            }
        };

        // Evalúa ambos sentidos y conserva la unión de pares con la mejor
        // contención. La cobertura máxima es simétrica: A→B y B→A producen el
        // mismo conjunto de fragmentos, sin importar el orden de los textos.
        foreach ($ta as $i => $tokensA) {
            if (!$tokensA) continue;

            $mejor = 0.0;
            $mejorJ = -1;

            foreach ($tb as $j => $tokensB) {
                if (!$tokensB) continue;

                $inter = count(array_intersect($tokensA, $tokensB));
                if ($inter === 0) continue;

                $score = max(
                    $inter / max(count($tokensA), 1),
                    $inter / max(count($tokensB), 1)
                );

                if ($score > $mejor) {
                    $mejor = $score;
                    $mejorJ = $j;
                }
            }

            if ($mejorJ >= 0) {
                $agregar($i, $mejorJ, $mejor);
            }
        }

        foreach ($tb as $j => $tokensB) {
            if (!$tokensB) continue;

            $mejor = 0.0;
            $mejorI = -1;

            foreach ($ta as $i => $tokensA) {
                if (!$tokensA) continue;

                $inter = count(array_intersect($tokensB, $tokensA));
                if ($inter === 0) continue;

                $score = max(
                    $inter / max(count($tokensB), 1),
                    $inter / max(count($tokensA), 1)
                );

                if ($score > $mejor) {
                    $mejor = $score;
                    $mejorI = $i;
                }
            }

            if ($mejorI >= 0) {
                $agregar($mejorI, $j, $mejor);
            }
        }

        $res = array_values($res);

        usort($res, function (array $x, array $y) {
            $orden = $y['score'] <=> $x['score'];

            return $orden !== 0 ? $orden : strcmp($x['a'] . "\0" . $x['b'], $y['a'] . "\0" . $y['b']);
        });

        return array_slice($res, 0, $max);
    }
}
