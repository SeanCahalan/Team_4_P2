package com.example.android.team4p2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

public class audioService extends Service implements MediaPlayer.OnPreparedListener {

    private static final String ACTION_PLAY = "com.example.android.team4p2";
    MediaPlayer mp = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        Uri uri = Uri.parse("android.resource://com.example.android.team4p2/" + R.raw.cena);
        mp = new MediaPlayer();
        try {
            mp.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setLooping(false);
    }

    @Override
    public void onDestroy()
    {
        if (mp != null) {
            mp.stop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (intent.getAction().equals(ACTION_PLAY)) {
            mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mp.setOnPreparedListener(this);
            mp.prepareAsync();
            return 1; // I don't know why I have to return an int...
        } else {
            Log.d("TROY", "This shouldn't happen.");
            return 0;
        }
    }

    public void onPrepared(MediaPlayer player) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            long[] pattern = {125, 125, 0, 72, 0, 73, 72, 533, 73, 125, 0, 72, 0, 73, 72, 585};
            for (int i = 0; i < pattern.length; i++) {
                pattern[i] *= 4;
            }
            vibrator.vibrate(pattern, -1);
            mp.release();
        } else {
            player.start();
            while(mp.isPlaying()){}
            mp.release();
        }
    }
}
