package com.example.mapexample.Alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class AlarmService extends Service {
    private final static String tag = "alarm_service";
    private final static int VIBRATION_PERIOD = 2000;
    private final static int VIBRATION_TIME = 1000;

    private MediaPlayer mp;
    private Vibrator mv;
    private Handler handler = new Handler();
    private Runnable mVibrationRunnable = new Runnable() {
        @Override
        public void run() {
            mv.vibrate(VIBRATION_TIME);
            handler.postDelayed(mVibrationRunnable,
                    VIBRATION_TIME + VIBRATION_PERIOD);
        }
    };

    private MediaPlayer.OnErrorListener errorlistener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int type, int etc) {
            Log.d(tag, "Error occurred while playing audio");
            Log.d(tag, "Error code: " + type);
            mediaPlayer.stop();
            mediaPlayer.release();

            handler.removeCallbacksAndMessages(null);
            AlarmService.this.stopSelf();
            return true;
        }
    };

    @Override
    public void onCreate() {
        Log.d(tag, "onCreate");
        HandlerThread ht = new HandlerThread(tag);
        ht.start();
        handler = new Handler(ht.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag, "onStartCommand");
        handler.post(new Runnable() {
            @Override
            public void run() {
                startPlayer();
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(tag, "onDestroy");
        if (mp.isPlaying()) {
            Log.d(tag, "stopIsScreenOff player");
            mp.stop();
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void startPlayer() {
        Log.d(tag, "startPlayer");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtone = prefs.getString("ringtone_setting",
                Settings.System.DEFAULT_RINGTONE_URI.toString());
        if (ringtone.equals(Settings.System.DEFAULT_RINGTONE_URI.toString())){
            ringtone = Settings.System.DEFAULT_RINGTONE_URI.toString();
        }
        mp = new MediaPlayer();
        mp.setOnErrorListener(errorlistener);
        float mVolumeLevel= 1;

        try {
            mp.setDataSource(this, Uri.parse(ringtone));
            mp.setLooping(true);
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
            mp.setVolume(mVolumeLevel, mVolumeLevel);
            mp.prepare();
            mp.start();

            if (prefs.getBoolean("vibration_setting", true)) {
                Log.d(tag, "Vibration is set");
                mv = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                handler.post(mVibrationRunnable);
            }
        } catch (Exception e) {
            Log.d(tag, "Failed to start media player\n", e);
            if (mp.isPlaying()) {
                mp.stop();
            }
            stopSelf();
        }
    }
}