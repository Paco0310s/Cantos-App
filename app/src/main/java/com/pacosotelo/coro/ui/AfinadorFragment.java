package com.pacosotelo.coro.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.pacosotelo.coro.R;
//import org.jtransforms.fft.DoubleFFT_1D;
import java.text.DecimalFormat;

public class AfinadorFragment extends Fragment {

    private boolean stopped = true;
    boolean buffer_ready = false;
    private float f_fundamental = 0.0f;
    private final int POW_FREC_SHOW = 11;
    private final int POW_FFT_BUFFER = 16;
    private final int BUFFER_SIZE_SHOW_FREQ = (int) Math.pow(2, POW_FREC_SHOW);
    private final int BUFFER_SIZE_MICRO = (int) Math.pow(2, POW_FFT_BUFFER);
    private final float[] Frecuencias = new float[BUFFER_SIZE_SHOW_FREQ];
    private final float[] hanning = new float[BUFFER_SIZE_MICRO];
    //private final DoubleFFT_1D fft = new DoubleFFT_1D(BUFFER_SIZE_MICRO);
    double[] buffer_double = new double[BUFFER_SIZE_MICRO];
    AudioRecord recorder = null;
    short[] buffer = new short[BUFFER_SIZE_MICRO];
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private Activity activity;
    private TextView mostrarFrec;
    View root;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted) requireActivity().finish();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_afinador, container, false);

        mostrarFrec = root.findViewById(R.id.editText_frec);

        mostrarFrec.setOnClickListener(v -> accion_boton_ini_fin());

        activity = getActivity();

        accion_boton_ini_fin();

        ActivityCompat.requestPermissions(activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            int SampleRate = Integer.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
            int BufferSizeMin = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SampleRate,
                    AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BufferSizeMin);

            //Hanning - Ventana
            for (int i = 0; i < BUFFER_SIZE_MICRO; i++) {
                //Funcion sacada de MATLAB
                hanning[i] = (float) (0.5 - (0.5 * Math.cos((2 * Math.PI * i) / (BUFFER_SIZE_MICRO - 1))));
            }

            //Relleno del buffer mapeando las frecuencias 2^11 MUESTRAS
            Frecuencias[0]=0.0f; //Hz
            float PASOS = (float) SampleRate/((float) BUFFER_SIZE_MICRO -1);
            //for(int i=1;i<BUFFER_SIZE_SHOW_FREQ;i++)
            for(int i=1;i<BUFFER_SIZE_SHOW_FREQ;i++) {
                // El valor 2,353 se saco de manera expiremental midiendo distintas
                // frecuencias y ajustando hasta tener un valor aceptable.
                Frecuencias[i] = Frecuencias[i-1] + PASOS;
            }

            new Thread(() -> {
                while (true)
                {
                    if (!stopped) {
                        activity.runOnUiThread(this::calcFFT);
                    }

                    // sleep
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignored) {

                    }


                }
            }).start();
            //------------------------------------------------------------------------------------------
        }

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------ Boton de iniciar / parar --------------------------------------
    //----------------------------------------------------------------------------------------------
    private void accion_boton_ini_fin () {
        // Si estaba encendido, solo apago
        if (!stopped) {
            stopped = true;
            f_fundamental=0;
            // Actualizo Frecuencia
            String frec = f_fundamental+"Hz";
            mostrarFrec.setText(frec);
            mostrarFrec.setTextColor(Color.RED);
            return;
        } else {
            stopped = false;
            mostrarFrec.setTextColor(Color.GREEN);
        }


        //---------------------------------- EJECUTO EL THREAD DE AUDIO RECORDER -------------------

        // Este thread va a estar siempre grabando audio
        new Thread(this::getDataAudio).start();


    }

    //----------------------------------------------------------------------------------------------
    //------------------------------ CALC FFT - JAVA -----------------------------------------------
    //----------------------------------------------------------------------------------------------
    // Este método hace la FFT
    private void calcFFT () {
        // Solo si hay nuevos datos en el buffer...
        if (buffer_ready) {

            // Pasamos a double como quiere la clase FFT
            for (int i = 0; i < BUFFER_SIZE_MICRO; i++)
            {
                buffer_double[i] = buffer[i]*hanning[i]; //Ventaneo
            }

            // HAcemos la FFT. La salida va a estar en el mismo buffer. Solo saca la parte
            // real (izquierda) de la FFT, intercalando la salida real y la imaginaria.
            //fft.realForward(buffer_double);

            updateFFT_values();

            DecimalFormat df = new DecimalFormat("#.00");
            String resultado = df.format(f_fundamental);

            // Actualizo Frecuencia
            String frec = resultado + "Hz";
            mostrarFrec.setText(frec);

            //(); //Comparo las notas medidas con las patron

            // Terminamos de procesar el buffer, reseteamos el flag
            buffer_ready = false;
        }
    }

    //Actualización graficos FFT
    private final float[] buffer_aux = new float[BUFFER_SIZE_SHOW_FREQ];

    //Funcion que trabaja con los datos transformados
    private void updateFFT_values() {
        //En esta funcion se obtiene el modulo

        for (int i=0;i<BUFFER_SIZE_SHOW_FREQ;i++)
        {
            //calculo el modulo
            double aux_mod = Math.sqrt(buffer_double[i]*buffer_double[i] + buffer_double[i+1]*buffer_double[i+1]);

            aux_mod = 20*Math.log(aux_mod);
            buffer_aux[i] = (float) aux_mod;
        }

        findPeaks(); //busco puntos y se calcula la frecuencia fundamental

    }


    //Busco los picos y saco la frecuencia fundamental
    private void findPeaks() {
        int len_freq = 0;
        float[] buff_freq =new float [(int) BUFFER_SIZE_SHOW_FREQ/2];
        //float[] Amplitud_muestras = new float [(int) BUFFER_SIZE_SHOW_FREQ/2];

        for(int i=1;i<buffer_aux.length-1;i++)
        {
            if( (buffer_aux[i]-buffer_aux[i-1])>0 && (buffer_aux[i+1]-buffer_aux[i])<=0 )
            {
                int NIVELMINIMO = 275;
                if(buffer_aux[i]> NIVELMINIMO -10)
                {
                    //Amplitud_muestras[len_freq]=buffer_aux[i];

                    float FREC_ELEGIDA = 80;
                    if(Frecuencias[i]> FREC_ELEGIDA -20&& Frecuencias[i]< FREC_ELEGIDA +20){
                        buff_freq[len_freq]= Frecuencias[i];
                        len_freq++;
                    }

                }
            }
        }

        // Busco el maximo de picos obtenidos (f fundamental)
        // luego busco a que frecuencia pertenece y es la que aparece por pantalla

        if(len_freq!=0)
        {

            f_fundamental =  findmax(buff_freq);//buff_freq[findmax(Amplitud_muestras)-1];
        }

        if (len_freq==0) f_fundamental=0;
    }

    //Funcion que sirve para encontrar el pico más alto despues de transformar
    //private int findmax(float aux[]){
    private float findmax(float[] aux){
        int arg=0;
        float suma=0;
        for(int i=0;i<aux.length-1;i++){
            if(aux[i]==0){
                arg=i;
                i= aux.length;
            }
            else suma=suma+aux[i];
        }
        suma=suma/arg;
        //return arg;
        return suma;
    }

    //----------------------------------------------------------------------------------------------
    //------------------------------ CAPTURAR AUDIO ------------------------------------------------
    //----------------------------------------------------------------------------------------------
    private void getDataAudio() {

        // Seteamos la prioridad
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        // intentamos crear el grqabador de audio y grabar...

        try {

            // Empezamos a grabar
            recorder.startRecording();

            // Mientras no me digan que pare...
            while(!stopped) {
                //Log.d("PRUEBA", "Estoy grabando");

                // Leo las muestras de audio
                recorder.read(buffer,0, BUFFER_SIZE_MICRO);

                // Si llego aca es que hay nueva info, seteo el flag para la FFT
                buffer_ready = true;

            }
        } catch(Throwable x) {
            Log.w("Error Audio: ","Error reading voice audio",x);
        }
    }

}

