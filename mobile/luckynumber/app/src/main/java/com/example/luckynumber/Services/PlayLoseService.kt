package com.example.luckynumber.Services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.example.luckynumber.R

class PlayLoseService: Service() {


    private lateinit var media : MediaPlayer
    override fun onCreate() {
        super.onCreate()
        media = MediaPlayer.create(getApplicationContext(), R.raw.laser)

    }
    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        media.start()
        return super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        media.release()
        super.onDestroy()

    }
}
