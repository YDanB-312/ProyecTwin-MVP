<?php

namespace App\Notifications;

use Illuminate\Auth\Notifications\ResetPassword as BaseResetPassword;
use Illuminate\Notifications\Messages\MailMessage;

// Notificación de restablecimiento en español; el enlace apunta al frontend.
class ResetPasswordNotification extends BaseResetPassword
{
    protected function resetUrl($notifiable)
    {
        $base = rtrim((string) config('app.frontend_url'), '/');
        $query = http_build_query([
            'token' => $this->token,
            'correo' => $notifiable->getEmailForPasswordReset(),
        ]);

        return "{$base}/restablecer-contrasena?{$query}";
    }

    public function toMail($notifiable)
    {
        $minutos = (int) config('auth.passwords.users.expire', 60);

        return (new MailMessage)
            ->subject('Restablece tu contraseña de ProyecTwin')
            ->greeting('Hola ' . trim((string) $notifiable->nombre))
            ->line('Recibimos una solicitud para restablecer la contraseña de tu cuenta.')
            ->action('Restablecer contraseña', $this->resetUrl($notifiable))
            ->line("El enlace vence en {$minutos} minutos.")
            ->line('Si no solicitaste este cambio, ignora este correo.');
    }
}
