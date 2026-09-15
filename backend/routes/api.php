<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

// Carga versionada: GET /api/v1/*
Route::prefix('v1')->group(base_path('routes/api-v1.php'));
