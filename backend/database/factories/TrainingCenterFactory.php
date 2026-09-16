<?php

namespace Database\Factories;

use App\Models\TrainingCenter;
use Illuminate\Database\Eloquent\Factories\Factory;

class TrainingCenterFactory extends Factory
{
    protected $model = TrainingCenter::class;

    public function definition(): array
    {
        return [
            'name' => 'Centro de ' . fake()->unique()->words(3, true),
            'city' => fake()->city(),
        ];
    }
}
