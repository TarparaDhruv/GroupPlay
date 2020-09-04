package com.example.redstar.groupplay;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

public class findmusic extends AppCompatActivity {

    ArrayAdapter madapter;
    ListView mlistview;
    Button next,pre,stop;
    int current=0;
    SeekBar volume;
    public static MediaPlayerService mps;
    private Intent playIntent;
    private boolean musicBound=false;
    private AudioManager audioManager;

    static void playSongFromFind(final String path, final Long passedid, long timetoplay)
    {
        new CountDownTimer(timetoplay,timetoplay)
        {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                mps.playSong(path, passedid);
                System.out.println("after calling mps from findmusic constructor");
            }
        }.start();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MusicBinder binder = (MediaPlayerService.MusicBinder)service;
            //get service
            mps = binder.getService();
            //pass list
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

        if(playIntent==null){
            playIntent = new Intent(this, MediaPlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findmusic);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        next = (Button) findViewById(R.id.next);
        pre = (Button) findViewById(R.id.pre);
        stop= (Button) findViewById(R.id.stop);
        volume=(SeekBar)findViewById(R.id.volume);
        ContentResolver cr = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        volume = (SeekBar) findViewById(R.id.volume);
        volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
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


        Cursor mCursor = cr.query(uri, null, selection, null, sortOrder);
        int count = mCursor.getCount();

        int titleColumn = mCursor.getColumnIndex
                (android.provider.MediaStore.Audio.Media.TITLE);
        int idColumn = mCursor.getColumnIndex
                (android.provider.MediaStore.Audio.Media._ID);
        int loc = mCursor.getColumnIndex
                (MediaStore.Audio.Media.DATA);

        final String songs[] = new String[count];
        final String songsloc[] = new String[count];
        final long songsid[] = new long[count];

        int i=0;
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                songs[i] = mCursor.getString(titleColumn);
                songsid[i] = Long.parseLong(mCursor.getString(idColumn));
                songsloc[i]= mCursor.getString(loc);
                i++;
            } while (mCursor.moveToNext());
        }

        madapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs);
        mlistview = (ListView)findViewById(R.id.lview);
        mlistview.setAdapter(madapter);

        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try
                {
                    current=position;
                    Toast.makeText(getApplicationContext(),songsloc[position]+" : at position : "+position,Toast.LENGTH_SHORT).show();
                    ServerSocket ss = new ServerSocket(getApplicationContext(),findmusic.this,songsloc[position],songsid[position]);
                    ss.execute();//calls socket
                }
                catch (Exception e){
                    Log.e("Error in calling socket : ",e.toString());
                    e.printStackTrace();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mps.player.reset();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                try
                {
                    if(current<songsloc.length-1)
                    {
                        current++;
                        Toast.makeText(getApplicationContext(),songsloc[current]+" : at position : "+current,Toast.LENGTH_SHORT).show();
                        ServerSocket ss = new ServerSocket(getApplicationContext(),findmusic.this,songsloc[current],songsid[current]);
                        ss.execute();//calls socket
                    }
                }
                catch (Exception e){
                    Log.e("Error in calling socket :",e.toString());
                    e.printStackTrace();
                }
            }
        });

        pre.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                try
                {
                    if(current>1)
                    {
                        current--;
                        Toast.makeText(getApplicationContext(),songsloc[current]+" : at position : "+current,Toast.LENGTH_SHORT).show();
                        ServerSocket ss = new ServerSocket(getApplicationContext(),findmusic.this,songsloc[current],songsid[current]);
                        ss.execute();//calls socket
                    }
                }
                catch (Exception e){
                    Log.e("Error in calling socket :",e.toString());
                    e.printStackTrace();
                }

            }
        });

        mCursor.close();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        mps=null;
        super.onDestroy();
    }
}