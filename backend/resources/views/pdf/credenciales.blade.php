<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <title>Credenciales ProyecTwin</title>
    <style>
        * { font-family: DejaVu Sans, sans-serif; }
        body { font-size: 11px; color: #1a1a1a; }
        h1 { font-size: 16px; margin: 0 0 4px; }
        .sub { color: #555; margin: 0 0 14px; font-size: 11px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #bbb; padding: 6px 8px; text-align: left; }
        th { background: #f0f0f0; font-size: 10px; text-transform: uppercase; }
        .cred { font-family: DejaVu Sans Mono, monospace; font-weight: bold; }
        .aviso { margin-top: 12px; font-size: 10px; color: #555; }
    </style>
</head>
<body>
    <h1>Credenciales de acceso · ProyecTwin</h1>
    <p class="sub">
        @if (!empty($contexto['ficha']))
            Ficha {{ $contexto['ficha'] }}@if (!empty($contexto['programa'])) · {{ $contexto['programa'] }}@endif ·
        @endif
        Generado el {{ $generado }}
    </p>

    <table>
        <thead>
            <tr>
                <th>Nombre completo</th>
                <th>Usuario</th>
                <th>Contraseña temporal</th>
                <th>Ficha</th>
                <th>Programa</th>
            </tr>
        </thead>
        <tbody>
            @foreach ($filas as $fila)
                <tr>
                    <td>{{ $fila['nombre'] }}</td>
                    <td class="cred">{{ $fila['username'] }}</td>
                    <td class="cred">{{ $fila['password'] }}</td>
                    <td>{{ $fila['ficha'] }}</td>
                    <td>{{ $fila['programa'] }}</td>
                </tr>
            @endforeach
        </tbody>
    </table>

    <p class="aviso">
        La contraseña temporal debe cambiarse en el primer inicio de sesión. Una vez cambiada
        no puede recuperarse; si el usuario la olvida, use “¿Olvidaste tu contraseña?” con su
        correo personal o restablézcala desde el panel de administración.
    </p>
</body>
</html>
