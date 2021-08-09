package com.example.takephoto;

import android.util.Log;

public class SingleDetectSound implements AmplitudeClipListener {
    public static final int AMPLITUDE_DIFF_LOW = 5000;
    public static final int AMPLITUDE_DIFF_MED = 9000;
    public static final int AMPLITUDE_DIFF_HIGH = 30000;

    public static final int DEFAULT_AMPLITUDE_DIFF = AMPLITUDE_DIFF_LOW;
    public int maxAmp;
    public SingleDetectSound(int maxAmp){
        this.maxAmp = maxAmp;
    }
    @Override
    public boolean heard(int maxAmplitude) {
        boolean result = false;

        if(maxAmp >= maxAmplitude){
            Log.d("Clap", result+ " " +maxAmp+" "+maxAmplitude);
            result = true;
        }
        return result;
    }
}
