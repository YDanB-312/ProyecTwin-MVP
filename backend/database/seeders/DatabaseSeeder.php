<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;

// Punto de entrada: el escenario demo completo vive en DemoSeeder.
class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        $this->call(DemoSeeder::class);
    }
}
