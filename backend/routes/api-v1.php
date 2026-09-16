<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\GeneralUserController;
use App\Http\Controllers\Api\KnowledgeNetworkController;
use App\Http\Controllers\Api\TrainingProgramController;
use App\Http\Controllers\Api\TrainingCenterController;
use App\Http\Controllers\Api\InstructorController;
use App\Http\Controllers\Api\ClassGroupController;
use App\Http\Controllers\Api\ApprenticeController;
use App\Http\Controllers\Api\AdminController;
use App\Http\Controllers\Api\ProjectController;
use App\Http\Controllers\Api\SimilarityController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\BugReportController;
use App\Http\Controllers\Api\ApprenticeProjectController;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CommentController;
use App\Http\Controllers\Api\MotorConfigController;

// ------------------------------------------------------------------ Públicas
// Login, registro de cuenta y restablecimiento de contraseña no exigen token.
Route::post('auth/login', [AuthController::class, 'login']);
Route::post('auth/password-reset', [AuthController::class, 'passwordReset']);
Route::post('general-users', [GeneralUserController::class, 'store']);

// Lectura pública (landing/demo): catálogo institucional y config del motor.
// Solo GET; la escritura sigue restringida a admin dentro del grupo autenticado.
Route::get('knowledge-networks', [KnowledgeNetworkController::class, 'index']);
Route::get('knowledge-networks/{knowledge_network}', [KnowledgeNetworkController::class, 'show']);
Route::get('training-programs', [TrainingProgramController::class, 'index']);
Route::get('training-programs/{training_program}', [TrainingProgramController::class, 'show']);
Route::get('training-centers', [TrainingCenterController::class, 'index']);
Route::get('training-centers/{training_center}', [TrainingCenterController::class, 'show']);
Route::get('config-similitud', [MotorConfigController::class, 'show']);
Route::get('public/resumen', [MotorConfigController::class, 'resumen']);

// Demo pública del motor: compara un texto libre contra el corpus vigente y
// devuelve solo títulos y porcentajes (nunca autores ni resúmenes).
Route::post('public/demo-similitud', [SimilarityController::class, 'demo']);

// ------------------------------------------------------------------ Sesión
Route::middleware('auth:sanctum')->group(function () {
    Route::post('auth/logout', [AuthController::class, 'logout']);
    Route::get('auth/me', [AuthController::class, 'me']);
});

// ------------------------------------------------------------------ Autenticadas
Route::middleware(['auth:sanctum', 'cuenta.activa'])->group(function () {

    // Configuración del motor de similitudes (la lectura es pública, arriba)
    Route::middleware('rol:admin')->put('config-similitud', [MotorConfigController::class, 'update']);

    // Usuarios
    Route::get('general-users', [GeneralUserController::class, 'index']);
    Route::get('general-users/{general_user}', [GeneralUserController::class, 'show']);
    Route::put('general-users/{general_user}', [GeneralUserController::class, 'update']);
    Route::middleware('rol:admin')->delete('general-users/{general_user}', [GeneralUserController::class, 'destroy']);

    // Catálogos institucionales: la lectura es pública (arriba); escritura admin
    Route::middleware('rol:admin')->group(function () {
        Route::post('knowledge-networks', [KnowledgeNetworkController::class, 'store']);
        Route::put('knowledge-networks/{knowledge_network}', [KnowledgeNetworkController::class, 'update']);
        Route::delete('knowledge-networks/{knowledge_network}', [KnowledgeNetworkController::class, 'destroy']);
        Route::post('training-programs', [TrainingProgramController::class, 'store']);
        Route::put('training-programs/{training_program}', [TrainingProgramController::class, 'update']);
        Route::delete('training-programs/{training_program}', [TrainingProgramController::class, 'destroy']);
        Route::post('training-centers', [TrainingCenterController::class, 'store']);
        Route::put('training-centers/{training_center}', [TrainingCenterController::class, 'update']);
        Route::delete('training-centers/{training_center}', [TrainingCenterController::class, 'destroy']);
    });

    // Instructores / Aprendices / Admins (altas y ediciones según rol)
    Route::get('instructors', [InstructorController::class, 'index']);
    Route::get('instructors/{instructor}', [InstructorController::class, 'show']);
    Route::get('apprentices', [ApprenticeController::class, 'index']);
    Route::get('apprentices/{apprentice}', [ApprenticeController::class, 'show']);
    Route::get('admins', [AdminController::class, 'index']);
    Route::get('admins/{admin}', [AdminController::class, 'show']);
    Route::middleware('rol:admin,instructor')->group(function () {
        Route::post('instructors', [InstructorController::class, 'store']);
        Route::put('instructors/{instructor}', [InstructorController::class, 'update']);
        Route::post('apprentices', [ApprenticeController::class, 'store']);
        Route::put('apprentices/{apprentice}', [ApprenticeController::class, 'update']);
    });
    Route::middleware('rol:admin')->group(function () {
        Route::delete('instructors/{instructor}', [InstructorController::class, 'destroy']);
        Route::delete('apprentices/{apprentice}', [ApprenticeController::class, 'destroy']);
        Route::post('admins', [AdminController::class, 'store']);
        Route::put('admins/{admin}', [AdminController::class, 'update']);
        Route::delete('admins/{admin}', [AdminController::class, 'destroy']);
    });

    // Fichas: lectura autenticada; escritura admin/instructor
    Route::get('class-groups', [ClassGroupController::class, 'index']);
    Route::get('class-groups/{class_group}', [ClassGroupController::class, 'show']);
    Route::middleware('rol:admin,instructor')->group(function () {
        Route::post('class-groups', [ClassGroupController::class, 'store']);
        Route::put('class-groups/{class_group}', [ClassGroupController::class, 'update']);
        Route::delete('class-groups/{class_group}', [ClassGroupController::class, 'destroy']);
    });

    // Propuestas
    Route::get('projects', [ProjectController::class, 'index']);
    Route::post('projects', [ProjectController::class, 'store']);
    Route::get('projects/{project}', [ProjectController::class, 'show']);
    Route::put('projects/{project}', [ProjectController::class, 'update']);
    Route::delete('projects/{project}', [ProjectController::class, 'destroy']);

    // Similitudes: el motor se ejecuta del lado servidor
    Route::post('similarities/detect', [SimilarityController::class, 'detect']);
    Route::middleware('rol:admin')->post('similarities/recalculate', [SimilarityController::class, 'recalculate']);
    Route::get('similarities', [SimilarityController::class, 'index']);
    Route::post('similarities', [SimilarityController::class, 'store']);
    Route::get('similarities/{similarity}', [SimilarityController::class, 'show']);
    Route::put('similarities/{similarity}', [SimilarityController::class, 'update']);
    Route::delete('similarities/{similarity}', [SimilarityController::class, 'destroy']);

    // Notificaciones
    Route::get('notifications', [NotificationController::class, 'index']);
    Route::post('notifications', [NotificationController::class, 'store']);
    Route::get('notifications/{notification}', [NotificationController::class, 'show']);
    Route::put('notifications/{notification}', [NotificationController::class, 'update']);
    Route::delete('notifications/{notification}', [NotificationController::class, 'destroy']);

    // Reportes de falla
    Route::get('bug-reports', [BugReportController::class, 'index']);
    Route::post('bug-reports', [BugReportController::class, 'store']);
    Route::get('bug-reports/{bug_report}', [BugReportController::class, 'show']);
    Route::put('bug-reports/{bug_report}', [BugReportController::class, 'update']);
    Route::delete('bug-reports/{bug_report}', [BugReportController::class, 'destroy']);

    // Equipo de la propuesta (pivote)
    Route::get('apprentice-projects', [ApprenticeProjectController::class, 'index']);
    Route::post('apprentice-projects', [ApprenticeProjectController::class, 'store']);
    Route::get('apprentice-projects/{apprentice_project}', [ApprenticeProjectController::class, 'show']);
    Route::put('apprentice-projects/{apprentice_project}', [ApprenticeProjectController::class, 'update']);
    Route::delete('apprentice-projects/{apprentice_project}', [ApprenticeProjectController::class, 'destroy']);

    // Observaciones (hilos de comentarios)
    Route::get('comments', [CommentController::class, 'index']);
    Route::post('comments', [CommentController::class, 'store']);
    Route::get('comments/{comment}', [CommentController::class, 'show']);
    Route::put('comments/{comment}', [CommentController::class, 'update']);
    Route::delete('comments/{comment}', [CommentController::class, 'destroy']);
});
