package com.example.android.team4p2;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

public class audioService extends Service implements MediaPlayer.OnPreparedListener {

    private static final String ACTION_PLAY = "com.example.android.team4p2";
    MediaPlayer mp = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate()
    {
        Uri uri = Uri.parse("android.resource://com.example.android.team4p2/" + R.raw.cena);
        mp = new MediaPlayer(); // .create(this, R.raw.cena);
        try {
            mp.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mp.setLooping(false);
    }

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
            Log.d("TROY", "This shouldn't happen");
            return 0;
        }
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
        while(mp.isPlaying()){}
        mp.release();
    }
}
