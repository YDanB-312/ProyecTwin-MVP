<?php

namespace App\Support;

// Reduce los nodos de usuario incrustados en respuestas de propuestas y
// similitudes. `estado` es un dato de gestión que no necesitan esas vistas;
// para un aprendiz, el correo y la foto de otros usuarios tampoco son
// necesarios (el contrato de contacto sigue siendo /general-users/{id}/perfil).
// Se conservan id, nombre, apellido y rol para que la UI siga resolviendo
// autorías y equipos.
final class PerfilEnRespuesta
{
    public static function redactar(array $datos, ?string $rol, bool $quitarContacto = true): array
    {
        foreach ($datos as $clave => $valor) {
            if (!is_array($valor)) continue;

            if (self::esNodoUsuario($clave, $valor)) {
                unset($valor['estado']);

                if ($rol === 'aprendiz' && $quitarContacto) {
                    unset($valor['correo'], $valor['foto_url']);
                }
            }

            $datos[$clave] = self::redactar($valor, $rol, $quitarContacto);
        }

        return $datos;
    }

    private static function esNodoUsuario(string $clave, array $valor): bool
    {
        return in_array($clave, ['creator', 'generalUser', 'user'], true)
            && isset($valor['rol']);
    }
}
