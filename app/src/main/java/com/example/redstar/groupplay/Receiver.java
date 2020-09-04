package com.example.redstar.groupplay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Receiver extends AppCompatActivity {

    Button dis, seekup, seekback;
    private AudioManager audioManager;
    SeekBar volume;
    static int seek = 0;
    TextView seekcount;

    Receiversocket receiversocket = new Receiversocket(Receiver.this, Receiver.this);

    public static MediaPlayerService mps;
    private Intent playIntent;
    private boolean musicBound = false;

    static void playSongFromReceiverSocket(final String path, long timetoplay) {
        new CountDownTimer(timetoplay, timetoplay) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                System.out.println("timer in receiver.java");
                mps.playSong(path);
            }
        }.start();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder) service;
            //get service
            mps = binder.getService();
            //pass list
            //mps.setList(songList); // this will pass list of songs
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        Toast.makeText(getApplicationContext(), "Receiversocket call", Toast.LENGTH_SHORT).show();
        receiversocket.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("before in resume stop bind start");
        if (musicBound == false) {
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        System.out.println("after in resume stop bind start");

    }

    public void executeAgain() {
        Log.i("executeaAgain : ", "before statement");
        //s = new Receiversocket(getApplicationContext(),Receiver.this);
        seek = 0;
        receiversocket.execute();
        Log.i("executeaAgain : ", "after receiversocket.execute");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);

        seekcount = (TextView) findViewById(R.id.seekcount);
        dis = (Button) findViewById(R.id.dis);
        seekup = (Button) findViewById(R.id.seekup);
        seekback = (Button) findViewById(R.id.seekback);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volume = (SeekBar) findViewById(R.id.volume);
        volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        //startService(new Intent(Receiver.this, Nameservice.class));

        seekup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long max = mps.player.getDuration();
                long current = mps.player.getCurrentPosition();
                if (current + 200 < max) {
                    seek += 200;
                    seekcount.setText((((float)seek / 1000)) + "");
                    mps.player.seekTo((int) current + seek);
                }
            }
        });

        seekback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long current = mps.player.getCurrentPosition();
                if (current - 200 > 0) {
                    seek -= 200;
                    seekcount.setText((((float)seek / 1000)) + "");
                    mps.player.seekTo((int) current - seek);
                }
            }
        });

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                seekBar.setProgress(progress);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mps.player.reset();
                Toast.makeText(getApplicationContext(), "Media player service reset", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        mps = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mps.player.reset();
        WifiManager wifiobj;
        wifiobj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiobj.setWifiEnabled(false);
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        //super.onBackPressed();  // optional depending on your needs
    }
}
