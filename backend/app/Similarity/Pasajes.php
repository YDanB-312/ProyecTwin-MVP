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
        foreach ($sa as $i => $oracionA) {
            if (!$ta[$i]) continue;
            $mejor = 0.0;
            $mejorJ = -1;
            foreach ($sb as $j => $oracionB) {
                if (!$tb[$j]) continue;
                $inter = count(array_intersect($ta[$i], $tb[$j]));
                $score = $inter / max(count($ta[$i]), 1);
                if ($score > $mejor) {
                    $mejor = $score;
                    $mejorJ = $j;
                }
            }
            if ($mejor >= $umbral && $mejorJ >= 0) {
                $res[] = [
                    'a' => $oracionA,
                    'b' => $sb[$mejorJ],
                    'score' => (int) round($mejor * 100),
                ];
            }
        }

        usort($res, fn ($x, $y) => $y['score'] <=> $x['score']);
        return array_slice($res, 0, $max);
    }
}
