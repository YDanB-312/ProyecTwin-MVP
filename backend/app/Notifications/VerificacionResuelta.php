<?php

namespace App\Notifications;

use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

// Correo al usuario (aprendiz o instructor) cuando el admin resuelve su
// verificación (aprobar o rechazar). En local se escribe en el log
// (MAIL_MAILER=log).
class VerificacionResuelta extends Notification
{
    use Queueable;

    public function __construct(
        private bool $aprobado,
        private ?string $motivo = null
    ) {
    }

    public function via($notifiable): array
    {
        return ['mail'];
    }

    public function toMail($notifiable): MailMessage
    {
        $mail = (new MailMessage)
            ->subject($this->aprobado
                ? 'ProyecTwin: cuenta verificada'
                : 'ProyecTwin: solicitud rechazada')
            ->greeting('Hola ' . trim((string) $notifiable->nombre));

        if ($this->aprobado) {
            return $mail
                ->line('Tu cuenta fue verificada. Ya puedes ingresar a ProyecTwin.')
                ->line('Gracias por completar el proceso.');
        }

        if ($this->motivo) {
            $mail->line('Motivo: ' . $this->motivo);
        }

        return $mail
            ->line('Tu solicitud de registro en ProyecTwin fue rechazada.')
            ->line('Si crees que es un error, contacta al administrador.');
    }
}
