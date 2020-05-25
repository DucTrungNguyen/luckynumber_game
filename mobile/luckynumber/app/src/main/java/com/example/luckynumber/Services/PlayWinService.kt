package com.example.luckynumber.Services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.media.MediaPlayer
import com.example.luckynumber.R
import kotlinx.android.synthetic.main.activity_main.*


class PlayWinService :Service() {


    private lateinit var media : MediaPlayer
    override fun onCreate() {
        super.onCreate()
        media = MediaPlayer.create(getApplicationContext(), R.raw.win)

    }
    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        media.start()
        return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        media.release()
        super.onDestroy()

    }
}