<?php

namespace App\Notifications;

use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

// Credenciales iniciales enviadas al correo personal del usuario nuevo. La
// contraseña es temporal: se cambia en el primer inicio de sesión.
class CredencialesUsuario extends Notification
{
    use Queueable;

    public function __construct(
        private string $username,
        private string $temporal
    ) {
    }

    public function via($notifiable): array
    {
        return ['mail'];
    }

    public function toMail($notifiable): MailMessage
    {
        return (new MailMessage)
            ->subject('ProyecTwin: tus credenciales de acceso')
            ->greeting('Hola ' . trim((string) $notifiable->nombre))
            ->line('Tu cuenta de ProyecTwin fue creada. Estas son tus credenciales:')
            ->line('Usuario: ' . $this->username)
            ->line('Contraseña temporal: ' . $this->temporal)
            ->line('Al ingresar por primera vez deberás cambiar la contraseña temporal.')
            ->line('Por seguridad, no compartas estas credenciales con nadie.');
    }
}
