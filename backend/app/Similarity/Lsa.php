<?php

namespace App\Similarity;

// Latent Semantic Analysis (LSA) — semántica aprendida SOLO del propio corpus.
//
// A partir de la matriz término-documento (ponderada con IDF) calculamos los
// primeros componentes latentes mediante iteración de potencias sobre la matriz
// de Gram documento-documento. Los documentos quedan representados por vectores
// en ese espacio latente: dos propuestas que comparten vocabulario asociado
// ("riego" ~ "cultivo") quedan cerca, aunque no compartan palabras.
//
// Todo es álgebra local (PHP puro): sin IA, sin librerías, sin archivos externos.
class Lsa
{
    // Número máximo de componentes latentes.
    private const MAX_COMPONENTES = 12;
    private const ITERACIONES = 60;

    // Corpus muy pequeño: LSA no es significativo (matriz degenerada).
    private const MIN_DOCS = 4;

    // Tope de seguridad: por encima, el coste O(n²) por comparación deja de ser
    // razonable; se omite el tema y el resto de señales sigue funcionando.
    private const MAX_DOCS = 200;

    // $docs: list<array<string>> (tokens por documento).
    // Devuelve list<array<float>> (vector latente por documento) o [] si aplica.
    public static function docVectors(array $docs): array
    {
        $n = count($docs);
        if ($n < self::MIN_DOCS || $n > self::MAX_DOCS) return [];

        // Frecuencias e IDF.
        $docTf = [];
        $df = [];
        foreach ($docs as $i => $tokens) {
            $tf = array_count_values($tokens);
            $docTf[$i] = $tf;
            foreach ($tf as $t => $_) $df[$t] = ($df[$t] ?? 0) + 1;
        }
        $idf = [];
        foreach ($df as $t => $f) $idf[$t] = log(($n + 1) / ($f + 1)) + 1;

        // Documentos como mapas término => peso (idf * tf).
        $M = [];
        foreach ($docTf as $i => $tf) {
            $v = [];
            foreach ($tf as $t => $f) $v[$t] = $idf[$t] * $f;
            $M[$i] = $v;
        }

        // Matriz de Gram documento-documento.
        $G = array_fill(0, $n, array_fill(0, $n, 0.0));
        for ($i = 0; $i < $n; $i++) {
            for ($j = $i; $j < $n; $j++) {
                $dot = 0.0;
                foreach ($M[$i] as $t => $w) {
                    if (isset($M[$j][$t])) $dot += $w * $M[$j][$t];
                }
                $G[$i][$j] = $dot;
                $G[$j][$i] = $dot;
            }
        }

        // Iteración de potencias con deflación para los primeros componentes.
        $k = min(self::MAX_COMPONENTES, $n - 1);
        $defl = $G;
        $componentes = [];

        for ($c = 0; $c < $k; $c++) {
            $v = array_fill(0, $n, 1.0 / sqrt($n));
            for ($it = 0; $it < self::ITERACIONES; $it++) {
                $w = self::multiplicar($defl, $v);
                $norma = sqrt(array_sum(array_map(fn ($x) => $x * $x, $w)));
                if ($norma < 1e-9) break;
                foreach ($w as $i => $x) $w[$i] = $x / $norma;
                $v = $w;
            }

            $Av = self::multiplicar($defl, $v);
            $lambda = 0.0;
            for ($i = 0; $i < $n; $i++) $lambda += $v[$i] * $Av[$i];
            if ($lambda <= 1e-9) break;

            $escala = sqrt($lambda);
            $componentes[$c] = array_map(fn ($x) => $x * $escala, $v);

            // Deflación: A <- A - lambda * v v^T
            for ($i = 0; $i < $n; $i++) {
                for ($j = 0; $j < $n; $j++) {
                    $defl[$i][$j] -= $lambda * $v[$i] * $v[$j];
                }
            }
        }

        if (!$componentes) return [];

        $out = [];
        for ($i = 0; $i < $n; $i++) {
            $vec = [];
            foreach ($componentes as $vc) $vec[] = $vc[$i];
            $out[$i] = $vec;
        }
        return $out;
    }

    private static function multiplicar(array $matriz, array $vector): array
    {
        $n = count($vector);
        $out = array_fill(0, $n, 0.0);
        for ($i = 0; $i < $n; $i++) {
            $s = 0.0;
            $fila = $matriz[$i];
            for ($j = 0; $j < $n; $j++) $s += $fila[$j] * $vector[$j];
            $out[$i] = $s;
        }
        return $out;
    }

    public static function coseno(array $a, array $b): float
    {
        $n = min(count($a), count($b));
        if ($n === 0) return 0.0;
        $punto = 0.0;
        for ($i = 0; $i < $n; $i++) $punto += $a[$i] * $b[$i];
        $na = 0.0;
        foreach ($a as $x) $na += $x * $x;
        $nb = 0.0;
        foreach ($b as $x) $nb += $x * $x;
        if ($na <= 0 || $nb <= 0) return 0.0;
        return max(0.0, min(1.0, $punto / (sqrt($na) * sqrt($nb))));
    }
}
