package com.training.myphone;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class DataListenerService extends WearableListenerService {


    GoogleApiClient mGoogleApiClient;
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer){
        for (DataEvent event : dataEventBuffer){
            Uri uri = event.getDataItem().getUri();
            if (event.getType() == DataEvent.TYPE_CHANGED && uri.getPath().equals("/audio_data")){
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
                String nodeId = uri.getHost();
                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
                        uri.toString().getBytes());
                Asset asset = dataMap.getAsset("audio_data");
                try {
                    handleReceivedAudioFile(asset, nodeId);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleReceivedAudioFile(Asset asset, String Id) throws ExecutionException, InterruptedException {
        // Extract the audio data from the asset and store it on the phone
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        // Convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream =
                Tasks.await(Wearable.getDataClient(MainActivity.mContext).getFdForAsset(asset))
                        .getInputStream();

        if (assetInputStream == null) {
            return;
        }
        DataInputStream in = new DataInputStream(assetInputStream);

        try (FileOutputStream outputStream = new FileOutputStream(new File("temp.mp3"), false)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
