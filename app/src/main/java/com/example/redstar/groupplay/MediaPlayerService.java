package com.example.redstar.groupplay;


import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    MediaPlayer player;
    private final IBinder musicBind = new MusicBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playSong(String path,Long id)
    {
        player.reset();
        Uri musicuri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id);

        try {
            player.setDataSource(getApplicationContext(),musicuri);
        } catch (IOException e) {
            Log.e("error in service source",e.toString());
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    public void playSong(String path)
    {
        player.stop();
        player.reset();
        Toast.makeText(getApplicationContext(),"mps player stop reset is called",Toast.LENGTH_SHORT).show();
        try {
            File f = new File(path);
            System.out.println("chechk file is create ");
            FileInputStream ff = new FileInputStream(f);
            System.out.println("chechk fileinputstream is create "+ff.getFD().toString());
            player.setDataSource(ff.getFD());
            System.out.println("chechk file setdata source");
        }catch (IOException e) {
            Log.e("error in service source",e.toString());
            Toast.makeText(getApplicationContext(),"error in change of song : "+e.toString(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    public void onCreate() {
        super.onCreate();
        player=new MediaPlayer();
        initMediaPlayer();
    }

    public  void initMediaPlayer()
    {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public class MusicBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //findmusic.next();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        mp.start();//start playing after voidplaysong called
    }
}
