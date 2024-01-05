package com.pacosotelo.coro.tools;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

public class ReproducirAudio {
    private MediaPlayer mediaplayer;

    public ReproducirAudio(Context context, Uri uri) {
        mediaplayer = MediaPlayer.create(context, uri);
    }

    public void reproducir() {
        //Iniciamos el audio
        mediaplayer.start();
    }

    public void pausar() {
        //Pausamos el audio
        mediaplayer.pause();
    }

    public void detener() {
        //Paramos el audio y volvemos a inicializar
        try {
            mediaplayer.stop();
            mediaplayer.prepare();
            mediaplayer.seekTo(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

