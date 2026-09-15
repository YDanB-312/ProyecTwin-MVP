<?php

namespace App\Services;

use App\Models\Project;

// Porte 1:1 de frontend/src/data/similitud.js (TF-IDF + coseno con español)
// Mismos STOPWORDS, SINONIMOS, stemEs y pesos por campo para paridad exacta.

class SimilitudService
{
    private const STOPWORDS = [
        'de' => true, 'la' => true, 'el' => true, 'los' => true, 'las' => true, 'lo' => true,
        'un' => true, 'una' => true, 'unos' => true, 'unas' => true, 'para' => true, 'con' => true,
        'por' => true, 'del' => true, 'al' => true, 'en' => true, 'entre' => true, 'hasta' => true,
        'desde' => true, 'sobre' => true, 'tras' => true, 'ante' => true, 'bajo' => true, 'hacia' => true,
        'segun' => true, 'que' => true, 'cual' => true, 'cuales' => true, 'quien' => true, 'quienes' => true,
        'este' => true, 'esta' => true, 'esto' => true, 'estos' => true, 'estas' => true,
    ];

    private const SINONIMOS = [
        'app' => 'sistema', 'apps' => 'sistema', 'aplicativo' => 'sistema',
        'plataforma' => 'sistema', 'software' => 'sistema',
        'tienda' => 'comercio', 'ecommerce' => 'comercio electronico',
        'sitio' => 'web', 'pagina' => 'web', 'movil' => 'movil',
        'artesano' => 'artesania', 'vend' => 'venta', 'stock' => 'inventario',
        'cultivo' => 'agricultura', 'medico' => 'salud', 'tutoria' => 'aprendizaje',
    ];

    private const PESO_CAMPOS = [
        ['titulo', 3], ['palabras_clave', 3], ['objetivos', 2], ['area_aplicacion', 2], ['resumen', 1],
    ];

    public static function stemEs(string $w): string
    {
        if (mb_strlen($w) <= 3) return $w;
        if (mb_strlen($w) > 7 && str_ends_with($w, 'mente')) $w = substr($w, 0, -5);
        if (mb_strlen($w) > 6 && (str_ends_with($w, 'ando') || str_ends_with($w, 'iendo'))) $w = substr($w, 0, -4);
        if (mb_strlen($w) > 4 && str_ends_with($w, 's')) $w = substr($w, 0, -1);
        return $w;
    }

    public static function tokenizar(?string $texto): array
    {
        $t = mb_strtolower($texto ?? '');
        $t = iconv('UTF-8', 'ASCII//TRANSLIT//IGNORE', $t) ?: $t;
        $tokens = preg_split('/[^a-z0-9]+/', $t);
        $out = [];
        foreach ($tokens as $crudo) {
            if (!$crudo || strlen($crudo) <= 2 || isset(self::STOPWORDS[$crudo])) continue;
            $raiz = self::stemEs($crudo);
            if (!$raiz || strlen($raiz) <= 2 || isset(self::STOPWORDS[$raiz])) continue;
            $canon = self::SINONIMOS[$raiz] ?? null;
            if ($canon) {
                foreach (explode(' ', $canon) as $x) if ($x && strlen($x) > 2) $out[] = $x;
            } else $out[] = $raiz;
        }
        return $out;
    }

    public static function vectorDeProyecto(Project $p): array
    {
        $vec = [];
        foreach (self::PESO_CAMPOS as [$campo, $peso]) {
            $val = $p->{$campo} ?? '';
            if (is_array($val)) $val = implode(' ', $val);
            foreach (self::tokenizar($val) as $t) $vec[$t] = ($vec[$t] ?? 0) + $peso;
        }
        return $vec;
    }

    public static function construirIdf(array $vectores): array
    {
        $df = []; $n = max(count($vectores), 1);
        foreach ($vectores as $vec) foreach ($vec as $t => $_) $df[$t] = ($df[$t] ?? 0) + 1;
        $idf = []; foreach ($df as $t => $f) $idf[$t] = log(($n + 1) / ($f + 1)) + 1;
        return $idf;
    }

    public static function similitudEntre(array $a, array $b, array $idf): float
    {
        if (!$a || !$b) return 0;
        [$corto, $largo] = count($a) <= count($b) ? [$a, $b] : [$b, $a];
        $normA = 0; foreach ($a as $t => $w) { $idfT = $idf[$t] ?? 1; $normA += ($w * $idfT) ** 2; }
        $normB = 0; foreach ($b as $t => $w) { $idfT = $idf[$t] ?? 1; $normB += ($w * $idfT) ** 2; }
        if (!$normA || !$normB) return 0;
        $punto = 0;
        foreach ($corto as $t => $w) if (isset($largo[$t])) { $idfT = $idf[$t] ?? 1; $punto += $w * $largo[$t] * $idfT * $idfT; }
        return $punto / (sqrt($normA) * sqrt($normB));
    }
}
