package com.example.takephoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    Button grabar;
    MediaRecorder recorder;
    public String FILE_PATH ;
    TextView tvResult;
    MediaPlayer mp;
    SwitchCompat mswitch;
    int status;

    private static int MICROPHONE_PERMISSION_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isMicrophonePresent() && isCameraPresent()) getMicrophonePermission();
        FILE_PATH = getRecordingFilePath();
        grabar = findViewById(R.id.Grabar);
        addControls();
        mp = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        mswitch = (SwitchCompat) findViewById(R.id.timerSwitch);

        SharedPreferences prefs = getSharedPreferences("status", MODE_PRIVATE);
        if(prefs!=null)
            status = prefs.getInt("st", 0);

        if (status != 0) {
            mswitch.setChecked(true);
            new RecordAmplitudeTask().execute(new SingleDetectClap(SingleDetectClap.DEFAULT_AMPLITUDE_DIFF));
        }

        grabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grabarAudio();
            }
        });
        mswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // do something, the isChecked will be
            // true if the switch is in the On position
            if (isChecked) {
                // do something when checked is selected
                SharedPreferences.Editor editor = getSharedPreferences("status", MODE_PRIVATE).edit();
                editor.putInt("st", 1);
                editor.apply();
                new RecordAmplitudeTask().execute(new SingleDetectClap(SingleDetectClap.DEFAULT_AMPLITUDE_DIFF));

            } else {
                //do something when unchecked
                SharedPreferences.Editor editor = getSharedPreferences("status", MODE_PRIVATE).edit();
                editor.putInt("st", 0);
                editor.apply();
            }
        });
    }

    private void addControls() {
        tvResult = findViewById(R.id.tvResult);
        File file = new File(getRecordingFilePath());
        if(file.exists()){
            file.delete();
        }
    }

    public void notification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Clap Heard");
        builder.setContentText("You have clapped");
//        builder.setSubText("Tap to view the detail.");
        builder.setOngoing(true);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }
    public void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mp.pause();
    }

    private void grabarAudio(){
        if(recorder == null){
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setOutputFile(getRecordingFilePath());

            try {
                recorder.prepare();
                recorder.start();
            }catch (IOException e)
            {

            }
            grabar.setText("Detener");
            Toast.makeText(getApplicationContext(), "GRABANDO", Toast.LENGTH_LONG).show();
        }else {
            recorder.stop();
            recorder.release();
            recorder = null;
            String asd = "grabar";
            grabar.setText(asd);
            Toast.makeText(getApplicationContext(), "GRABACION FINALIZADA", Toast.LENGTH_LONG).show();
        }
    }
    private void getMicrophonePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},MICROPHONE_PERMISSION_CODE);
        }
    }
    private boolean isMicrophonePresent(){
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }
    private boolean isCameraPresent() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
    public String getRecordingFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDIrectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDIrectory, "testRecording" + ".mp3");
        return  file.getPath();
    }
    public class RecordAmplitudeTask extends AsyncTask<AmplitudeClipListener, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvResult.setText("Detecting clap .....");
//            mp.stop();
        }

        @Override
        protected Boolean doInBackground(AmplitudeClipListener... listeners) {
            if(listeners.length == 0){
                return false;
            }
            AmplitudeClipListener listener = listeners[0];

            MaxAmplitudeRecorder recorder = new MaxAmplitudeRecorder(MaxAmplitudeRecorder.DEFAULT_CLIP_TIME, FILE_PATH, listener, this, getApplicationContext());

            boolean heard = false;
            try{

                heard = recorder.startRecording();

            } catch (IllegalStateException is){
                Log.e("IS", is + "");
                heard = false;
            }catch (RuntimeException re){
                Log.e("RE", re + "");
                heard = false;
            }



            return heard;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.d("result", result+"");
            if(result){
                tvResult.setText("Heard Clap");
                notification();
                mp.start();
                try {
                    sleep(11000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cancelNotification();
                if(status!=0)
                    new RecordAmplitudeTask().execute(new SingleDetectClap(SingleDetectClap.DEFAULT_AMPLITUDE_DIFF));
            }
        }


        @Override
        protected void onCancelled() {
            setDoneMessage();
            super.onCancelled();
        }

        private void setDoneMessage() {
            Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
        }
    }

}