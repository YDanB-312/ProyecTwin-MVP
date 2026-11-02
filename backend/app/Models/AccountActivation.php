<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class AccountActivation extends Model
{
    protected $table = 'account_activations';

    protected $fillable = [
        'user_id',
        'token_hash',
        'expires_at',
        'sent_at',
        'used_at',
        'error',
    ];

    protected $casts = [
        'expires_at' => 'datetime',
        'sent_at' => 'datetime',
        'used_at' => 'datetime',
    ];

    protected $hidden = [
        'token_hash',
    ];

    public function user()
    {
        return $this->belongsTo(GeneralUser::class, 'user_id');
    }
}
