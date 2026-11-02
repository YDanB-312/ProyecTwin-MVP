<?php

use App\Http\Controllers\HealthController;
use Illuminate\Support\Facades\Route;

// Rutas de salud sin el grupo `web`: no inician sesión ni generan cookies.
Route::get('/up', [HealthController::class, 'up']);
Route::get('/ready', [HealthController::class, 'ready']);
