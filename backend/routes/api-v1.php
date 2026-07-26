<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\GeneralUserController;
use App\Http\Controllers\Api\TrainingProgramController;
use App\Http\Controllers\Api\InstructorController;
use App\Http\Controllers\Api\ClassGroupController;
use App\Http\Controllers\Api\ApprenticeController;
use App\Http\Controllers\Api\AdminController;
use App\Http\Controllers\Api\ProjectController;
use App\Http\Controllers\Api\SimilarityController;
use App\Http\Controllers\Api\AssessmentController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\BugReportController;
use App\Http\Controllers\Api\ApprenticeProjectController;
use App\Http\Controllers\Api\CommentController;

// General Users
Route::get('general-users', [GeneralUserController::class, 'index']);
Route::post('general-users', [GeneralUserController::class, 'store']);
Route::get('general-users/{general_user}', [GeneralUserController::class, 'show']);
Route::put('general-users/{general_user}', [GeneralUserController::class, 'update']);
Route::delete('general-users/{general_user}', [GeneralUserController::class, 'destroy']);

// Training Programs
Route::get('training-programs', [TrainingProgramController::class, 'index']);
Route::post('training-programs', [TrainingProgramController::class, 'store']);
Route::get('training-programs/{training_program}', [TrainingProgramController::class, 'show']);
Route::put('training-programs/{training_program}', [TrainingProgramController::class, 'update']);
Route::delete('training-programs/{training_program}', [TrainingProgramController::class, 'destroy']);

// Instructors
Route::get('instructors', [InstructorController::class, 'index']);
Route::post('instructors', [InstructorController::class, 'store']);
Route::get('instructors/{instructor}', [InstructorController::class, 'show']);
Route::put('instructors/{instructor}', [InstructorController::class, 'update']);
Route::delete('instructors/{instructor}', [InstructorController::class, 'destroy']);

// Class Groups
Route::get('class-groups', [ClassGroupController::class, 'index']);
Route::post('class-groups', [ClassGroupController::class, 'store']);
Route::get('class-groups/{class_group}', [ClassGroupController::class, 'show']);
Route::put('class-groups/{class_group}', [ClassGroupController::class, 'update']);
Route::delete('class-groups/{class_group}', [ClassGroupController::class, 'destroy']);

// Apprentices
Route::get('apprentices', [ApprenticeController::class, 'index']);
Route::post('apprentices', [ApprenticeController::class, 'store']);
Route::get('apprentices/{apprentice}', [ApprenticeController::class, 'show']);
Route::put('apprentices/{apprentice}', [ApprenticeController::class, 'update']);
Route::delete('apprentices/{apprentice}', [ApprenticeController::class, 'destroy']);

// Admins
Route::get('admins', [AdminController::class, 'index']);
Route::post('admins', [AdminController::class, 'store']);
Route::get('admins/{admin}', [AdminController::class, 'show']);
Route::put('admins/{admin}', [AdminController::class, 'update']);
Route::delete('admins/{admin}', [AdminController::class, 'destroy']);

// Projects
Route::get('projects', [ProjectController::class, 'index']);
Route::post('projects', [ProjectController::class, 'store']);
Route::get('projects/{project}', [ProjectController::class, 'show']);
Route::put('projects/{project}', [ProjectController::class, 'update']);
Route::delete('projects/{project}', [ProjectController::class, 'destroy']);

// Similarities
Route::get('similarities', [SimilarityController::class, 'index']);
Route::post('similarities', [SimilarityController::class, 'store']);
Route::get('similarities/{similarity}', [SimilarityController::class, 'show']);
Route::put('similarities/{similarity}', [SimilarityController::class, 'update']);
Route::delete('similarities/{similarity}', [SimilarityController::class, 'destroy']);

// Assessments
Route::get('assessments', [AssessmentController::class, 'index']);
Route::post('assessments', [AssessmentController::class, 'store']);
Route::get('assessments/{assessment}', [AssessmentController::class, 'show']);
Route::put('assessments/{assessment}', [AssessmentController::class, 'update']);
Route::delete('assessments/{assessment}', [AssessmentController::class, 'destroy']);

// Notifications
Route::get('notifications', [NotificationController::class, 'index']);
Route::post('notifications', [NotificationController::class, 'store']);
Route::get('notifications/{notification}', [NotificationController::class, 'show']);
Route::put('notifications/{notification}', [NotificationController::class, 'update']);
Route::delete('notifications/{notification}', [NotificationController::class, 'destroy']);

// Bug Reports
Route::get('bug-reports', [BugReportController::class, 'index']);
Route::post('bug-reports', [BugReportController::class, 'store']);
Route::get('bug-reports/{bug_report}', [BugReportController::class, 'show']);
Route::put('bug-reports/{bug_report}', [BugReportController::class, 'update']);
Route::delete('bug-reports/{bug_report}', [BugReportController::class, 'destroy']);

// Apprentice Projects
Route::get('apprentice-projects', [ApprenticeProjectController::class, 'index']);
Route::post('apprentice-projects', [ApprenticeProjectController::class, 'store']);
Route::get('apprentice-projects/{apprentice_project}', [ApprenticeProjectController::class, 'show']);
Route::put('apprentice-projects/{apprentice_project}', [ApprenticeProjectController::class, 'update']);
Route::delete('apprentice-projects/{apprentice_project}', [ApprenticeProjectController::class, 'destroy']);

// Comments
Route::get('comments', [CommentController::class, 'index']);
Route::post('comments', [CommentController::class, 'store']);
Route::get('comments/{comment}', [CommentController::class, 'show']);
Route::put('comments/{comment}', [CommentController::class, 'update']);
Route::delete('comments/{comment}', [CommentController::class, 'destroy']);
