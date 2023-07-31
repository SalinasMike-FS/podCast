package com.training.mywatch;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.media.MediaRecorder;
import java.io.IOException;
import android.content.Context;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import com.training.mywatch.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;
    private Button btnRecord, btnSend;
    private MediaRecorder mediaRecorder;
    private static String outfile = "out.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        btnRecord = binding.recordButton;
        btnSend = binding.sendPhoneButton;
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording(outfile);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                stopRecording();
                try {
                    sendAudioFileToPhone(MainActivity.this, new File(outfile));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void startRecording(String outputFile){

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendAudioFileToPhone(Context context, File file) throws ExecutionException, InterruptedException {
        Asset asset;
        try {
            byte[] audioBytes = Files.readAllBytes(file.toPath());
            asset = Asset.createFromBytes(audioBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putAsset("audio_data", asset);
        PutDataRequest putReq = PutDataRequest.create("/audio_data");
        putReq.setData(dataMap.toByteArray());
        putReq.setUrgent();
        //putReq = putReq.asPutDataRequest().setUrgent();
        new SendThread(context, putReq).start();

//        Task<DataItem> putDataTask = Wearable.getDataClient(context).putDataItem(putReq);
//        Tasks.await(putDataTask);
    }

    class SendThread extends Thread {
        PutDataRequest mMap;
        Context mContext;
        //constructor
        SendThread(Context context, PutDataRequest map) {
            mMap = map;
            mContext = context;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            Task<DataItem> putDataTask = Wearable.getDataClient(mContext).putDataItem(mMap);
            try {
                Tasks.await(putDataTask);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}