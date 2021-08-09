package com.example.takephoto;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class MaxAmplitudeRecorder {
    public static final long DEFAULT_CLIP_TIME = 500;
    private long clipTime = DEFAULT_CLIP_TIME;

    private AmplitudeClipListener clipListener;

    private boolean continueRecording;
    private MediaRecorder recorder;

    private String tmpAudioFile;
    private AsyncTask task;

    private Context context;

    public MaxAmplitudeRecorder(long clipTime, String tmpAudioFile, AmplitudeClipListener clipListener, AsyncTask task, Context context){
        this.clipTime = clipTime;
        this.clipListener = clipListener;
        this.tmpAudioFile = tmpAudioFile;
        this.task = task;
        this.context = context;
    }

    public boolean startRecording(){

        recorder = prepareRecoder(tmpAudioFile);
        recorder.setOnErrorListener((mr, what, extra) -> stopRecording());
        recorder.start();
        continueRecording = true;
        boolean heard = false;
        recorder.getMaxAmplitude();
        while (continueRecording){
            waitClipTime();
            if((!continueRecording) || (task != null) && task.isCancelled()){
                break;
            }

            int maxAmplitude = recorder.getMaxAmplitude();
            Log.d("max", maxAmplitude+ "");
            heard = clipListener.heard(maxAmplitude);
            if(!heard){
                stopRecording();
            }
        }
        return !heard;
    }

    private void waitClipTime() {
        try {
            Thread.sleep(clipTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void done(){
        if(recorder != null){
            recorder.stop();
            recorder.release();
        }
    }

    private void stopRecording() {
        continueRecording = false;
    }
    public boolean isRecording(){
        return continueRecording;
    }


    public MediaRecorder prepareRecoder(String sdCardPath){
        boolean isSdCardAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        boolean isSdCardSupport = Environment.isExternalStorageRemovable();
        if(isSdCardAvailable && isSdCardSupport){
            Toast.makeText(context, "SD Card is not support", Toast.LENGTH_SHORT).show();
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(sdCardPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recorder;

    }
}
