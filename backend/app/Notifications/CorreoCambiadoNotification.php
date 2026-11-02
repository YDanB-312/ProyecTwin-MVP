<?php

namespace App\Notifications;

use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

// Aviso al correo anterior cuando el identificador de acceso cambia. No
// incluye tokens ni contraseñas; sirve para detectar cambios no autorizados.
class CorreoCambiadoNotification extends Notification
{
    use Queueable;

    public function __construct(
        private string $correoAnterior,
        private string $correoNuevo
    ) {
    }

    public function via($notifiable): array
    {
        return ['mail'];
    }

    public function toMail($notifiable): MailMessage
    {
        return (new MailMessage)
            ->subject('Tu correo de acceso a ProyecTwin cambió')
            ->greeting('Hola')
            ->line('El correo de acceso de tu cuenta cambió de '
                . $this->correoAnterior . ' a ' . $this->correoNuevo . '.')
            ->line('Si no realizaste este cambio, contacta de inmediato al administrador.');
    }
}
