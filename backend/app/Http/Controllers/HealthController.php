<?php

namespace App\Http\Controllers;

use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;
use Throwable;

// Healthchecks sin middleware web: no abren sesión, no escriben cookies y no
// tocan la base en el caso de liveness. `/up` es liveness para Railway;
// `/ready` comprueba la dependencia de base de datos para diagnóstico.
class HealthController extends Controller
{
    public function up(): JsonResponse
    {
        return response()->json(['status' => 'ok']);
    }

    public function ready(): JsonResponse
    {
        try {
            DB::select('select 1');
        } catch (Throwable) {
            return response()->json(['status' => 'unavailable'], 503);
        }

        return response()->json(['status' => 'ready']);
    }
}
