package com.pacosotelo.coro.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.tools.MenuHelper;

import java.util.Locale;

public class AfinadorFragment extends Fragment {

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 4096;

    private TextView tvNoteName, tvFrequency, tvTuningStatus;
    private View viewTrackBackground, viewNeedlePointer;
    private MaterialButton bStartStopTuner;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startTuner();
                } else {
                    Toast.makeText(getActivity(), "Se requiere permiso de micrófono para afinar", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_afinador, container, false);

        tvNoteName = root.findViewById(R.id.tvNoteName);
        tvFrequency = root.findViewById(R.id.tvFrequency);
        tvTuningStatus = root.findViewById(R.id.tvTuningStatus);
        viewTrackBackground = root.findViewById(R.id.viewTrackBackground);
        viewNeedlePointer = root.findViewById(R.id.viewNeedlePointer);
        bStartStopTuner = root.findViewById(R.id.bStartStopTuner);

        bStartStopTuner.setOnClickListener(v -> toggleTuner());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        verificarYPedirPermiso();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lista, menu);

        MenuItem buscarItem = menu.findItem(R.id.buscar);
        if (buscarItem != null) {
            buscarItem.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.info) {
            MenuHelper.alertaAcercaDe(getActivity());
            return true;
        } else if (itemId == R.id.cerrarSesion) {
            MenuHelper.cerrarSesion(getActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verificarYPedirPermiso() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (!isRecording) {
                startTuner();
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void toggleTuner() {
        if (isRecording) {
            stopTuner();
        } else {
            verificarYPedirPermiso();
        }
    }

    @SuppressLint("MissingPermission")
    private void startTuner() {
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = Math.max(BUFFER_SIZE, minBufferSize);

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Toast.makeText(getActivity(), "No se pudo inicializar el micrófono", Toast.LENGTH_SHORT).show();
                return;
            }

            audioRecord.startRecording();
            isRecording = true;
            bStartStopTuner.setText("Detener Afinador");

            recordingThread = new Thread(this::processAudioData);
            recordingThread.start();
        } catch (Exception e) {
            Log.e("AfinadorFragment", "Error al iniciar AudioRecord", e);
            Toast.makeText(getActivity(), "Error al acceder al micrófono", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTuner() {
        isRecording = false;
        if (recordingThread != null) {
            recordingThread.interrupt();
            recordingThread = null;
        }

        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception e) {
                Log.e("AfinadorFragment", "Error al detener AudioRecord", e);
            }
            audioRecord = null;
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                bStartStopTuner.setText("Iniciar Afinador");
                tvNoteName.setText("--");
                tvNoteName.setTextColor(ContextCompat.getColor(requireContext(), R.color.card_title_color));
                tvFrequency.setText("0.0 Hz");
                tvTuningStatus.setText("Presiona Iniciar");
                tvTuningStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.tag_tiempo_text_color));
                if (viewNeedlePointer != null) {
                    viewNeedlePointer.setTranslationX(0f);
                    viewNeedlePointer.setBackgroundColor(Color.parseColor("#FF9800"));
                }
            });
        }
    }

    private void processAudioData() {
        short[] audioBuffer = new short[BUFFER_SIZE];

        while (isRecording && audioRecord != null) {
            int readSize = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);
            if (readSize > 0) {
                float pitch = detectPitch(audioBuffer, readSize, SAMPLE_RATE);
                if (pitch > 50 && pitch < 1200) {
                    PitchResult result = calculateNote(pitch);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateUI(result));
                    }
                }
            }
        }
    }

    private float detectPitch(short[] buffer, int length, int sampleRate) {
        double sumSq = 0;
        for (int i = 0; i < length; i++) {
            sumSq += buffer[i] * buffer[i];
        }
        double rms = Math.sqrt(sumSq / length);
        if (rms < 250) {
            return -1f;
        }

        int minLag = sampleRate / 1000;
        int maxLag = sampleRate / 65;

        float[] r = new float[maxLag + 1];
        for (int lag = minLag; lag <= maxLag; lag++) {
            float sum = 0;
            for (int i = 0; i < length - lag; i++) {
                sum += (buffer[i] * buffer[i + lag]);
            }
            r[lag] = sum;
        }

        int peakLag = -1;
        float maxVal = -1;
        for (int lag = minLag; lag <= maxLag; lag++) {
            if (r[lag] > maxVal) {
                maxVal = r[lag];
                peakLag = lag;
            }
        }

        if (peakLag <= minLag || peakLag >= maxLag) return -1f;

        float alpha = r[peakLag - 1];
        float beta = r[peakLag];
        float gamma = r[peakLag + 1];

        float denominator = (2 * (2 * beta - alpha - gamma));
        if (denominator == 0) return sampleRate / (float) peakLag;

        float delta = (gamma - alpha) / denominator;
        float trueLag = peakLag + delta;

        return sampleRate / trueLag;
    }

    private static class PitchResult {
        String noteName;
        float frequency;
        float cents;
        boolean isInTune;
    }

    private PitchResult calculateNote(float frequency) {
        double midiNumber = 69 + 12 * (Math.log(frequency / 440.0) / Math.log(2.0));
        int roundedMidi = (int) Math.round(midiNumber);
        double cents = (midiNumber - roundedMidi) * 100;

        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int noteIndex = (roundedMidi % 12 + 12) % 12;
        int octave = (roundedMidi / 12) - 1;

        PitchResult result = new PitchResult();
        result.frequency = frequency;
        result.noteName = noteNames[noteIndex] + octave;
        result.cents = (float) cents;
        result.isInTune = Math.abs(cents) <= 6;

        return result;
    }

    private void updateUI(PitchResult result) {
        if (!isAdded()) return;

        tvNoteName.setText(result.noteName);
        tvFrequency.setText(String.format(Locale.US, "%.1f Hz", result.frequency));

        if (viewTrackBackground != null && viewNeedlePointer != null) {
            int trackWidth = viewTrackBackground.getWidth();
            int pointerWidth = viewNeedlePointer.getWidth();
            if (trackWidth > 0 && pointerWidth > 0) {
                float clampedCents = Math.max(-50f, Math.min(50f, result.cents));
                float fraction = (clampedCents + 50f) / 100f;
                float translationX = (fraction - 0.5f) * (trackWidth - pointerWidth);
                viewNeedlePointer.setTranslationX(translationX);
            }
        }

        if (result.isInTune) {
            tvNoteName.setTextColor(Color.parseColor("#4CAF50"));
            if (viewNeedlePointer != null) viewNeedlePointer.setBackgroundColor(Color.parseColor("#4CAF50"));
            tvTuningStatus.setText("AFINADO ✅");
            tvTuningStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if (result.cents < -6) {
            tvNoteName.setTextColor(Color.parseColor("#FF9800"));
            if (viewNeedlePointer != null) viewNeedlePointer.setBackgroundColor(Color.parseColor("#FF9800"));
            tvTuningStatus.setText("SUBIR ⬆️");
            tvTuningStatus.setTextColor(Color.parseColor("#FF9800"));
        } else {
            tvNoteName.setTextColor(Color.parseColor("#FF5722"));
            if (viewNeedlePointer != null) viewNeedlePointer.setBackgroundColor(Color.parseColor("#FF5722"));
            tvTuningStatus.setText("BAJAR ⬇️");
            tvTuningStatus.setTextColor(Color.parseColor("#FF5722"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTuner();
    }
}
