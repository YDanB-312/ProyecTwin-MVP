<?php

namespace Database\Factories;

use App\Models\Centro;
use Illuminate\Database\Eloquent\Factories\Factory;

class CentroFactory extends Factory
{
    protected $model = Centro::class;

    public function definition(): array
    {
        return [
            'nombre' => 'Centro de ' . fake()->unique()->words(3, true),
            'ciudad' => fake()->city(),
        ];
    }
}
