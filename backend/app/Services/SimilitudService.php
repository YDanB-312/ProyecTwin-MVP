<?php

namespace App\Services;

use App\Models\Project;
use App\Similarity\Lsa;
use App\Similarity\Pasajes;

// Motor de similitud de propuestas (mejorado, 100% local).
//
// Combina CUATRO señales sobre el contenido real de dos propuestas:
//   1) PALABRAS  : BM25 + bigramas + sinónimos de dominio (tema por vocabulario).
//   2) CARACTERES: TF-IDF de n-gramas (3-5) — copias parciales / literales.
//   3) TEMA (LSA): semántica aprendida del propio corpus (co-ocurrencia).
//   4) COBERTURA : qué fracción del contenido de una aparece en la otra.
//
// Además devuelve los PASAJES comunes (oraciones que coinciden) para explicar
// *qué* se parece, no solo *cuánto*.
//
// No usa IA, ni librerías, ni archivos externos: solo PHP y el propio corpus.
class SimilitudService
{
    // Pesos de las señales (suman 1). Si no hay LSA (corpus pequeño), el peso del
    // tema se reparte entre las demás.
    private const PESOS = [
        'palabras' => 0.40,
        'caracteres' => 0.20,
        'tema' => 0.25,
        'cobertura' => 0.15,
    ];

    // Parámetros de BM25.
    private const BM25_K1 = 1.4;
    private const BM25_B = 0.75;

    // Campos de la propuesta y su peso (los tokens se repiten según el peso).
    private const PESO_CAMPOS = [
        ['titulo', 3],
        ['palabras_clave', 3],
        ['objetivo_general', 2],
        ['objetivos_especificos', 2],
        ['area_aplicacion', 2],
        ['resumen', 1],
    ];

    private const NGRAM_MIN = 3;
    private const NGRAM_MAX = 5;

    private const STOPWORDS = [
        'de' => true, 'la' => true, 'el' => true, 'los' => true, 'las' => true, 'lo' => true,
        'un' => true, 'una' => true, 'unos' => true, 'unas' => true, 'para' => true, 'con' => true,
        'por' => true, 'del' => true, 'al' => true, 'en' => true, 'entre' => true, 'hasta' => true,
        'desde' => true, 'sobre' => true, 'tras' => true, 'ante' => true, 'bajo' => true, 'hacia' => true,
        'segun' => true, 'que' => true, 'cual' => true, 'cuales' => true, 'quien' => true, 'quienes' => true,
        'cuyo' => true, 'cuya' => true, 'cuyos' => true, 'cuyas' => true,
        'este' => true, 'esta' => true, 'esto' => true, 'estos' => true, 'estas' => true,
        'ese' => true, 'esa' => true, 'eso' => true, 'esos' => true, 'esas' => true,
        'aquel' => true, 'aquella' => true, 'aquello' => true, 'mi' => true, 'mis' => true,
        'tu' => true, 'tus' => true, 'su' => true, 'sus' => true, 'nuestro' => true, 'nuestra' => true,
        'nuestros' => true, 'nuestras' => true, 'como' => true, 'cuando' => true, 'donde' => true,
        'porque' => true, 'pues' => true, 'pero' => true, 'sino' => true, 'aunque' => true,
        'muy' => true, 'mas' => true, 'tan' => true, 'tanto' => true, 'asi' => true, 'tambien' => true,
        'tampoco' => true, 'solo' => true, 'quizas' => true, 'hay' => true, 'han' => true, 'son' => true,
        'es' => true, 'ser' => true, 'sea' => true, 'sean' => true, 'fue' => true, 'fueron' => true,
        'tiene' => true, 'tienen' => true, 'tener' => true, 'hace' => true, 'hacen' => true, 'hacer' => true,
        'permite' => true, 'permiten' => true, 'permitir' => true, 'cada' => true, 'todo' => true,
        'toda' => true, 'todos' => true, 'todas' => true, 'otro' => true, 'otra' => true, 'otros' => true,
        'otras' => true, 'mismo' => true, 'misma' => true, 'mismos' => true, 'mismas' => true,
        'traves' => true, 'les' => true, 'nos' => true, 'ello' => true, 'ellos' => true, 'ellas' => true,
        'fin' => true, 'mediante' => true, 'forma' => true, 'manera' => true, 'caso' => true,
        'casos' => true, 'vez' => true, 'veces' => true, 'usando' => true, 'usar' => true, 'uso' => true,
        'principalmente' => true, 'puede' => true, 'pueden' => true, 'podra' => true, 'debe' => true,
        'deben' => true, 'sera' => true, 'seran' => true, 'esta' => true, 'estan' => true,
    ];

    // Sinónimos/conceptos de dominio SENA/proyectos (claves ya stemizadas, sin
    // tildes). El valor puede expandir a varios tokens separados por espacio.
    private const SINONIMOS = [
        // Software / sistemas
        'app' => 'sistema', 'apps' => 'sistema', 'aplicativo' => 'sistema', 'aplicacion' => 'sistema',
        'plataforma' => 'sistema', 'software' => 'sistema', 'herramienta' => 'sistema',
        'sistema' => 'sistema', 'programa' => 'sistema', 'modulo' => 'sistema',
        // Web / móvil
        'online' => 'linea web', 'sitio' => 'web', 'pagina' => 'web', 'paginas' => 'web',
        'web' => 'web', 'portal' => 'web sitio',
        'movil' => 'movil', 'mobile' => 'movil', 'celular' => 'movil', 'telefono' => 'movil',
        // Comercio
        'tienda' => 'comercio venta', 'ecommerce' => 'comercio electronico', 'commerce' => 'comercio electronico',
        'comercializ' => 'comercio', 'comercializa' => 'comercio', 'comercial' => 'comercio',
        'vend' => 'venta', 'vendedor' => 'venta', 'expendio' => 'venta', 'venta' => 'venta',
        'compra' => 'venta', 'pago' => 'venta pago', 'pasarela' => 'pago',
        'producto' => 'producto', 'catalogo' => 'catalogo producto',
        'carrito' => 'venta carrito',
        // Inventario / logística
        'stock' => 'inventario', 'existencias' => 'inventario', 'existencia' => 'inventario',
        'inventario' => 'inventario', 'almacen' => 'inventario almacen', 'bodega' => 'inventario',
        'logistica' => 'logistica', 'pedido' => 'logistica pedido', 'trazabilidad' => 'trazabilidad',
        // Monitoreo / IoT
        'seguimiento' => 'monitoreo', 'supervision' => 'monitoreo', 'vigilancia' => 'monitoreo',
        'rastreo' => 'monitoreo', 'monitoreo' => 'monitoreo', 'sensor' => 'sensor iot',
        'sensores' => 'sensor iot', 'iot' => 'iot', 'tiempo' => 'tiempo real',
        // Agro
        'cultivo' => 'agricultura', 'cultivos' => 'agricultura', 'agricola' => 'agricultura',
        'agropecuario' => 'agricultura', 'cosecha' => 'agricultura', 'riego' => 'agricultura riego',
        'agricultura' => 'agricultura', 'campo' => 'agricultura',
        // Salud
        'medico' => 'salud', 'clinica' => 'salud', 'hospital' => 'salud', 'paciente' => 'salud',
        'salud' => 'salud', 'cita' => 'reserva salud',
        // Educación
        'elearning' => 'aprendizaje', 'e-learning' => 'aprendizaje', 'tutoria' => 'aprendizaje',
        'tutor' => 'aprendizaje', 'aprendizaje' => 'aprendizaje', 'educativo' => 'educacion',
        'educacion' => 'educacion', 'curso' => 'educacion curso', 'estudiante' => 'aprendiz',
        'aprendiz' => 'aprendiz',
        // Gestión
        'administracion' => 'gestion', 'administrar' => 'gestion', 'gestion' => 'gestion',
        'registro' => 'gestion registro', 'registrar' => 'gestion registro',
        'reporte' => 'reporte', 'reportes' => 'reporte', 'informe' => 'reporte',
        'control' => 'control', 'organizar' => 'gestion',
        // Turismo / cultura
        'turistico' => 'turismo', 'turismo' => 'turismo', 'cultura' => 'cultura',
        'cultural' => 'cultura', 'ruta' => 'ruta', 'rutas' => 'ruta', 'geolocalizacion' => 'ubicacion',
        // Datos / IA
        'datos' => 'dato', 'dato' => 'dato', 'inteligencia' => 'ia', 'automatizacion' => 'automatizacion',
        'automatico' => 'automatizacion', 'chatbot' => 'ia conversacional',
    ];

    // ---------------------------------------------------------------- Normalización

    // minúsculas + sin tildes; deja solo [a-z0-9ñ].
    public static function normalizar(string $texto): string
    {
        $t = mb_strtolower($texto);
        $t = iconv('UTF-8', 'ASCII//TRANSLIT//IGNORE', $t) ?: $t;
        return $t;
    }

    // Stemming ligero de español (consistente para corpus y consulta).
    public static function stemEs(string $w): string
    {
        $len = strlen($w);
        if ($len <= 3) return $w;

        // Sufijos adverbiales/colectivos.
        if ($len > 7 && str_ends_with($w, 'mente')) $w = substr($w, 0, -5);
        if (strlen($w) > 7 && str_ends_with($w, 'ciones')) $w = substr($w, 0, -5);
        if (strlen($w) > 7 && str_ends_with($w, 'siones')) $w = substr($w, 0, -5);
        if (strlen($w) > 6 && str_ends_with($w, 'amiento')) $w = substr($w, 0, -7);
        if (strlen($w) > 5 && str_ends_with($w, 'imiento')) $w = substr($w, 0, -7);

        // Gerundios y participios.
        if (strlen($w) > 6 && (str_ends_with($w, 'ando') || str_ends_with($w, 'iendo'))) {
            $w = substr($w, 0, -4);
        } elseif (strlen($w) > 5 && (str_ends_with($w, 'ado') || str_ends_with($w, 'ido')
            || str_ends_with($w, 'ada') || str_ends_with($w, 'ida'))) {
            $w = substr($w, 0, -3);
        }

        // Infinitivos.
        if (strlen($w) > 5 && (str_ends_with($w, 'ar') || str_ends_with($w, 'er') || str_ends_with($w, 'ir'))) {
            $w = substr($w, 0, -2);
        }

        // Plural.
        if (strlen($w) > 4 && str_ends_with($w, 'es')) $w = substr($w, 0, -2);
        elseif (strlen($w) > 4 && str_ends_with($w, 's')) $w = substr($w, 0, -1);

        return $w;
    }

    // Tokeniza: normaliza, filtra ruido, aplica stemming, expande sinónimos y
    // agrega bigramas de palabras consecutivas (frases).
    public static function tokenizar(?string $texto): array
    {
        $t = self::normalizar($texto ?? '');
        $crudos = preg_split('/[^a-z0-9ñ]+/', $t) ?: [];
        $bases = [];
        $out = [];
        foreach ($crudos as $crudo) {
            if (!$crudo || strlen($crudo) <= 2 || isset(self::STOPWORDS[$crudo])) continue;
            $raiz = self::stemEs($crudo);
            if (!$raiz || strlen($raiz) <= 2 || isset(self::STOPWORDS[$raiz])) continue;
            $bases[] = $raiz;
            if (isset(self::SINONIMOS[$raiz])) {
                foreach (explode(' ', self::SINONIMOS[$raiz]) as $x) {
                    if ($x && strlen($x) > 2 && !isset(self::STOPWORDS[$x])) $out[] = $x;
                }
            } else {
                $out[] = $raiz;
            }
        }
        for ($i = 0; $i + 1 < count($bases); $i++) {
            $out[] = 'b:' . $bases[$i] . '_' . $bases[$i + 1];
        }
        return $out;
    }

    // ---------------------------------------------------------------- Vectores

    // Texto concatenado normalizado (para n-gramas de caracteres).
    private static function textoCompleto(Project $p): string
    {
        $partes = [
            $p->titulo,
            $p->palabras_clave,
            $p->area_aplicacion,
            $p->objetivo_general,
            is_array($p->objetivos_especificos) ? implode(' ', $p->objetivos_especificos) : $p->objetivos_especificos,
            $p->resumen,
        ];
        return self::normalizar(implode(' ', array_filter($partes)));
    }

    // Texto natural (con tildes) para trocear en oraciones.
    private static function textoParaPasajes(Project $p): string
    {
        return implode('. ', array_filter([
            $p->titulo,
            $p->objetivo_general,
            is_array($p->objetivos_especificos) ? implode('. ', $p->objetivos_especificos) : $p->objetivos_especificos,
            $p->resumen,
        ]));
    }

    // Tokens ponderados por campo (se repiten según el peso del campo).
    private static function tokensPonderados(Project $p): array
    {
        $out = [];
        foreach (self::PESO_CAMPOS as [$campo, $peso]) {
            $val = $p->{$campo} ?? '';
            if (is_array($val)) $val = implode(' ', $val);
            $tk = self::tokenizar((string) $val);
            for ($i = 0; $i < $peso; $i++) {
                foreach ($tk as $t) $out[] = $t;
            }
        }
        return $out;
    }

    // Vector TF de n-gramas de caracteres (3-5) sobre el texto completo.
    public static function vectorCaracteres(Project $p): array
    {
        $texto = preg_replace('/\s+/', ' ', self::textoCompleto($p));
        $vec = [];
        $len = strlen($texto);
        for ($n = self::NGRAM_MIN; $n <= self::NGRAM_MAX; $n++) {
            for ($i = 0; $i + $n <= $len; $i++) {
                $gram = substr($texto, $i, $n);
                if (str_contains($gram, ' ')) continue;
                $vec[$gram] = ($vec[$gram] ?? 0) + 1;
            }
        }
        return $vec;
    }

    // Pesos BM25 por término (satura la frecuencia y normaliza por longitud).
    private static function vectorBM25(array $tokens, array $idf, float $avgLen): array
    {
        if (!$tokens) return [];
        $tf = array_count_values($tokens);
        $len = max(count($tokens), 1);
        $k1 = self::BM25_K1;
        $b = self::BM25_B;
        $vec = [];
        foreach ($tf as $t => $f) {
            $sat = ($f * ($k1 + 1)) / ($f + $k1 * (1 - $b + $b * $len / max($avgLen, 1e-9)));
            $vec[$t] = ($idf[$t] ?? 1.0) * $sat;
        }
        return $vec;
    }

    // ---------------------------------------------------------------- IDF + coseno

    public static function construirIdf(array $vectores): array
    {
        $df = [];
        foreach ($vectores as $vec) {
            foreach ($vec as $t => $_) $df[$t] = ($df[$t] ?? 0) + 1;
        }
        $n = max(count($vectores), 1);
        $idf = [];
        foreach ($df as $t => $f) $idf[$t] = log(($n + 1) / ($f + 1)) + 1;
        return $idf;
    }

    // Coseno entre dos vectores (con IDF opcional aplicado en el momento).
    public static function coseno(array $a, array $b, ?array $idf = null): float
    {
        if (!$a || !$b) return 0.0;
        if ($idf === null) return self::cosenoDirecto($a, $b);

        $wa = [];
        foreach ($a as $t => $w) $wa[$t] = $w * ($idf[$t] ?? 1.0);
        $wb = [];
        foreach ($b as $t => $w) $wb[$t] = $w * ($idf[$t] ?? 1.0);
        return self::cosenoDirecto($wa, $wb);
    }

    private static function cosenoDirecto(array $a, array $b): float
    {
        if (!$a || !$b) return 0.0;
        [$corto, $largo] = count($a) <= count($b) ? [$a, $b] : [$b, $a];
        $punto = 0.0;
        foreach ($corto as $t => $w) {
            if (isset($largo[$t])) $punto += $w * $largo[$t];
        }
        $na = 0.0;
        foreach ($a as $w) $na += $w * $w;
        $nb = 0.0;
        foreach ($b as $w) $nb += $w * $w;
        if ($na <= 0 || $nb <= 0) return 0.0;
        return max(0.0, min(1.0, $punto / (sqrt($na) * sqrt($nb))));
    }

    // Fracción (ponderada) del contenido de A que aparece en B.
    private static function cobertura(array $tokensA, array $tokensB, array $idf): float
    {
        if (!$tokensA || !$tokensB) return 0.0;
        $A = array_count_values($tokensA);
        $B = array_count_values($tokensB);
        $total = 0.0;
        $comun = 0.0;
        foreach ($A as $t => $f) {
            $w = ($idf[$t] ?? 1.0) * $f;
            $total += $w;
            if (isset($B[$t])) $comun += min($w, ($idf[$t] ?? 1.0) * $B[$t]);
        }
        return $total > 0 ? min(1.0, $comun / $total) : 0.0;
    }

    private static function pesos(bool $conTema): array
    {
        $base = self::PESOS;
        if ($conTema) return $base;
        $suma = $base['palabras'] + $base['caracteres'] + $base['cobertura'];
        return [
            'palabras' => $base['palabras'] / $suma,
            'caracteres' => $base['caracteres'] / $suma,
            'tema' => 0.0,
            'cobertura' => $base['cobertura'] / $suma,
        ];
    }

    // ---------------------------------------------------------------- Puntaje final

    // $corpus: Project[] (incluye la propia propuesta).
    public static function puntaje(Project $objetivo, array $corpus): array
    {
        $tokens = [];
        $vecCar = [];
        $tokensPlanos = [];
        foreach ($corpus as $p) {
            $tk = self::tokensPonderados($p);
            $tokens[$p->id] = $tk;
            $tokensPlanos[$p->id] = self::tokenizar(self::textoCompleto($p));
            $vecCar[$p->id] = self::vectorCaracteres($p);
        }

        $idfPal = self::construirIdf(array_map(fn ($tk) => array_count_values($tk), $tokens));
        $idfCar = self::construirIdf($vecCar);

        $avgLen = 0.0;
        foreach ($tokens as $tk) $avgLen += count($tk);
        $avgLen = count($tokens) ? $avgLen / count($tokens) : 1.0;

        $bm25 = [];
        foreach ($tokens as $id => $tk) $bm25[$id] = self::vectorBM25($tk, $idfPal, $avgLen);

        // LSA: vectores latentes por documento (según el orden del corpus).
        $latentes = Lsa::docVectors(array_values($tokensPlanos));
        $lsa = [];
        if ($latentes) {
            $i = 0;
            foreach ($corpus as $p) {
                $lsa[$p->id] = $latentes[$i] ?? null;
                $i++;
            }
        }
        $pesos = self::pesos($lsa !== []);

        $resultado = [];
        foreach ($corpus as $otro) {
            if ($otro->id === $objetivo->id) continue;

            $sPal = self::cosenoDirecto($bm25[$objetivo->id] ?? [], $bm25[$otro->id] ?? []);
            $sCar = self::coseno($vecCar[$objetivo->id] ?? [], $vecCar[$otro->id] ?? [], $idfCar);
            $sTema = (isset($lsa[$objetivo->id]) && isset($lsa[$otro->id]))
                ? Lsa::coseno($lsa[$objetivo->id], $lsa[$otro->id]) : null;
            $sCob = max(
                self::cobertura($tokens[$objetivo->id] ?? [], $tokens[$otro->id] ?? [], $idfPal),
                self::cobertura($tokens[$otro->id] ?? [], $tokens[$objetivo->id] ?? [], $idfPal)
            );

            $score = $pesos['palabras'] * $sPal
                + $pesos['caracteres'] * $sCar
                + $pesos['tema'] * ($sTema ?? 0.0)
                + $pesos['cobertura'] * $sCob;

            // Términos compartidos de mayor peso (los más explicativos).
            $compartidos = [];
            foreach ($bm25[$objetivo->id] ?? [] as $t => $peso) {
                if (isset($bm25[$otro->id][$t])) $compartidos[$t] = min($peso, $bm25[$otro->id][$t]);
            }
            arsort($compartidos);
            $terminos = array_slice(array_keys($compartidos), 0, 10);
            $terminos = array_map(
                fn ($t) => str_starts_with($t, 'b:') ? str_replace(['b:', '_'], ['', ' '], $t) : $t,
                $terminos
            );

            $resultado[] = [
                'project_id' => $otro->id,
                'score' => $score,
                'porcentaje' => (int) round($score * 100),
                'detalles' => [
                    'palabras' => (int) round($sPal * 100),
                    'caracteres' => (int) round($sCar * 100),
                    'tema' => $sTema === null ? null : (int) round($sTema * 100),
                    'cobertura' => (int) round($sCob * 100),
                    'terminos' => array_values($terminos),
                    'pasajes' => Pasajes::comunes(
                        self::textoParaPasajes($objetivo),
                        self::textoParaPasajes($otro)
                    ),
                    'pesos' => $pesos,
                    'engine' => 'improved-1',
                ],
            ];
        }

        return $resultado;
    }
}
