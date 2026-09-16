<?php

namespace App\Services;

use App\Models\Project;

// Motor de similitud de propuestas.
//
// Compara el contenido real de dos propuestas combinando DOS señales:
//   1) Palabras  : TF-IDF + coseno con normalización de español (tildes,
//                  sinónimos de dominio y stemming ligero). Capta temas
//                  parecidos redactados con vocabulario afín.
//   2) Caracteres: TF-IDF + coseno sobre n-gramas (3-5) del texto completo.
//                  Capta copias parciales y cambios de letras u omisiones
//                  (misma propuesta con palabras quitadas).
//
// El puntaje final es una mezcla ponderada de ambas señales. Es 100% local:
// no envía datos de aprendices a terceros y no tiene dependencias externas.
//
// PUNTO DE EXTENSIÓN (futuro, no implementado): si más adelante se dispone de
// un servicio de embeddings, aquí se agregaría una tercera señal semántica y
// se reponderaría la mezcla. La firma pública `puntaje()` es el único punto a tocar.
class SimilitudService
{
    // Reparto de peso entre las dos señales (suman 1).
    private const PESO_PALABRAS = 0.6;
    private const PESO_CARACTERES = 0.4;

    // Campos de la propuesta y su peso en el vector de palabras.
    private const PESO_CAMPOS = [
        ['titulo', 3],
        ['palabras_clave', 3],
        ['objetivo_general', 2],
        ['objetivos_especificos', 2],
        ['area_aplicacion', 2],
        ['resumen', 1],
    ];

    // rango de tamaños de n-grama de caracteres.
    private const NGRAM_MIN = 3;
    private const NGRAM_MAX = 5;

    // Palabras gramaticales sin carga temática (las genéricas tipo "sistema"
    // las castiga el IDF solo).
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
        'casos' => true, 'vez' => true, 'veces' => true,
    ];

    // Sinónimos de dominio SENA/proyectos (claves ya stemizadas, sin tildes).
    // Un valor puede expandir a varios tokens separados por espacio.
    private const SINONIMOS = [
        'app' => 'sistema', 'apps' => 'sistema', 'aplicativo' => 'sistema', 'aplicacion' => 'sistema',
        'plataforma' => 'sistema', 'software' => 'sistema',
        'tienda' => 'comercio', 'ecommerce' => 'comercio electronico', 'commerce' => 'comercio electronico',
        'online' => 'linea', 'sitio' => 'web', 'pagina' => 'web', 'paginas' => 'web',
        'movil' => 'movil', 'mobile' => 'movil', 'celular' => 'movil',
        'artesano' => 'artesania', 'comercializ' => 'comercio', 'comercializa' => 'comercio',
        'comercial' => 'comercio', 'vend' => 'venta', 'vendedor' => 'venta', 'expendio' => 'venta',
        'seguimiento' => 'monitoreo', 'supervision' => 'monitoreo', 'vigilancia' => 'monitoreo',
        'rastreo' => 'monitoreo', 'administracion' => 'gestion', 'administrar' => 'gestion',
        'stock' => 'inventario', 'existencias' => 'inventario',
        'cultivo' => 'agricultura', 'agricola' => 'agricultura', 'agropecuario' => 'agricultura',
        'cosecha' => 'agricultura', 'riego' => 'agricultura',
        'medico' => 'salud', 'clinica' => 'salud', 'hospital' => 'salud', 'paciente' => 'salud',
        'elearning' => 'aprendizaje', 'tutoria' => 'aprendizaje', 'tutor' => 'aprendizaje',
        'reserva' => 'reserva', 'cita' => 'reserva',
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
        if ($len > 7 && str_ends_with($w, 'mente')) $w = substr($w, 0, -5);
        if ($len > 6 && (str_ends_with($w, 'ando') || str_ends_with($w, 'iendo'))) {
            $w = substr($w, 0, -4);
        } elseif ($len > 5 && (str_ends_with($w, 'ado') || str_ends_with($w, 'ido') || str_ends_with($w, 'ada') || str_ends_with($w, 'ida'))) {
            $w = substr($w, 0, -3);
        }
        if (strlen($w) > 6 && (str_ends_with($w, 'ciones') || str_ends_with($w, 'siones'))) $w = substr($w, 0, -5);
        if (strlen($w) > 5 && (str_ends_with($w, 'ar') || str_ends_with($w, 'er') || str_ends_with($w, 'ir'))) $w = substr($w, 0, -2);
        if (strlen($w) > 4 && str_ends_with($w, 'es')) $w = substr($w, 0, -2);
        elseif (strlen($w) > 4 && str_ends_with($w, 's')) $w = substr($w, 0, -1);
        return $w;
    }

    // Tokeniza: normaliza, filtra ruido, aplica stemming y expande sinónimos.
    public static function tokenizar(?string $texto): array
    {
        $t = self::normalizar($texto ?? '');
        $crudos = preg_split('/[^a-z0-9ñ]+/', $t);
        $out = [];
        foreach ($crudos as $crudo) {
            if (!$crudo || strlen($crudo) <= 2 || isset(self::STOPWORDS[$crudo])) continue;
            $raiz = self::stemEs($crudo);
            if (!$raiz || strlen($raiz) <= 2 || isset(self::STOPWORDS[$raiz])) continue;
            if (isset(self::SINONIMOS[$raiz])) {
                foreach (explode(' ', self::SINONIMOS[$raiz]) as $x) {
                    if ($x && strlen($x) > 2 && !isset(self::STOPWORDS[$x])) $out[] = $x;
                }
            } else {
                $out[] = $raiz;
            }
        }
        return $out;
    }

    // ---------------------------------------------------------------- Vectores

    // Texto completo de la propuesta (para n-gramas de caracteres).
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

    // Vector TF ponderado por campo: token => peso acumulado.
    public static function vectorPalabras(Project $p): array
    {
        $vec = [];
        foreach (self::PESO_CAMPOS as [$campo, $peso]) {
            $val = $p->{$campo} ?? '';
            if (is_array($val)) $val = implode(' ', $val);
            foreach (self::tokenizar((string) $val) as $t) {
                $vec[$t] = ($vec[$t] ?? 0) + $peso;
            }
        }
        return $vec;
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
                if (str_contains($gram, ' ')) continue; // n-gramas sin espacios
                $vec[$gram] = ($vec[$gram] ?? 0) + 1;
            }
        }
        return $vec;
    }

    // ---------------------------------------------------------------- IDF + coseno

    // IDF suavizado sobre los vectores del corpus.
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

    // Coseno entre dos vectores TF-IDF. 1 = idénticos, 0 = sin nada en común.
    public static function coseno(array $a, array $b, array $idf): float
    {
        if (!$a || !$b) return 0;
        [$corto, $largo] = count($a) <= count($b) ? [$a, $b] : [$b, $a];
        $normaA = 0;
        foreach ($a as $t => $w) { $idfT = $idf[$t] ?? 1; $normaA += ($w * $idfT) ** 2; }
        $normaB = 0;
        foreach ($b as $t => $w) { $idfT = $idf[$t] ?? 1; $normaB += ($w * $idfT) ** 2; }
        if (!$normaA || !$normaB) return 0;
        $punto = 0;
        foreach ($corto as $t => $w) {
            if (isset($largo[$t])) { $idfT = $idf[$t] ?? 1; $punto += $w * $largo[$t] * $idfT * $idfT; }
        }
        return $punto / (sqrt($normaA) * sqrt($normaB));
    }

    // ---------------------------------------------------------------- Puntaje final

    // Calcula el puntaje (0-1) de una propuesta contra el corpus y devuelve
    // también los términos de palabras compartidos (explicación para la UI).
    // $corpus: Project[] (incluye la propia propuesta).
    public static function puntaje(Project $objetivo, array $corpus): array
    {
        // Vectores de palabras de todo el corpus + IDF propia de esa señal.
        $vecPalabras = [];
        $vecCaracteres = [];
        foreach ($corpus as $p) {
            $vecPalabras[$p->id] = self::vectorPalabras($p);
            $vecCaracteres[$p->id] = self::vectorCaracteres($p);
        }
        $idfPalabras = self::construirIdf(array_values($vecPalabras));
        $idfCaracteres = self::construirIdf(array_values($vecCaracteres));

        $propioPal = $vecPalabras[$objetivo->id] ?? [];
        $propioCar = $vecCaracteres[$objetivo->id] ?? [];

        $resultado = [];
        foreach ($corpus as $otro) {
            if ($otro->id === $objetivo->id) continue;

            $sPalabras = self::coseno($propioPal, $vecPalabras[$otro->id] ?? [], $idfPalabras);
            $sCaracteres = self::coseno($propioCar, $vecCaracteres[$otro->id] ?? [], $idfCaracteres);
            $final = self::PESO_PALABRAS * $sPalabras + self::PESO_CARACTERES * $sCaracteres;

            // Términos de palabra compartidos (los de mayor peso), para explicar.
            $compartidos = [];
            foreach ($propioPal as $t => $w) {
                if (isset($vecPalabras[$otro->id][$t])) $compartidos[$t] = $w;
            }
            arsort($compartidos);
            $terminos = array_slice(array_keys($compartidos), 0, 8);

            $resultado[] = [
                'project_id' => $otro->id,
                'score' => $final,
                'porcentaje' => (int) round($final * 100),
                'detalles' => [
                    'palabras' => (int) round($sPalabras * 100),
                    'caracteres' => (int) round($sCaracteres * 100),
                    'terminos' => $terminos,
                ],
            ];
        }
        return $resultado;
    }
}
