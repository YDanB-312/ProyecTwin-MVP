<?php

namespace App\Notifications;

use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

// Enlace de activación de una cuenta nueva. Nunca incluye contraseña ni
// credenciales temporales; el token viaja solo en la URL.
class ActivacionCuentaNotification extends Notification
{
    use Queueable;

    public function __construct(private string $token)
    {
    }

    public function via($notifiable): array
    {
        return ['mail'];
    }

    public function toMail($notifiable): MailMessage
    {
        $minutos = (int) config('auth.activation_expire', 1440);
        // El token viaja en el fragmento: el navegador no lo envía al servidor
        // ni aparece en logs, proxies o cabeceras Referer.
        $url = rtrim((string) config('app.frontend_url'), '/')
            . '/activar-cuenta#' . http_build_query(['token' => $this->token]);

        return (new MailMessage)
            ->subject('Activa tu cuenta de ProyecTwin')
            ->greeting('Hola ' . trim((string) $notifiable->nombre))
            ->line('Tu cuenta de ProyecTwin fue creada. Para activarla, define tu contraseña.')
            ->action('Activar mi cuenta', $url)
            ->line("El enlace vence en {$minutos} minutos y solo puede usarse una vez.")
            ->line('Si no esperabas este correo, ignóralo o contacta al administrador.');
    }
}
