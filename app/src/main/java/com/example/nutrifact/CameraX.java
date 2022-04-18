package com.example.nutrifact;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class CameraX extends AppCompatActivity implements View.OnClickListener, ImageAnalysis.Analyzer {

    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    private boolean isCameraPermissionGranted = false;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;

    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;

    TextView bCamera;
    PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageAnalysis imageAnalysis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerax);

        bCamera = findViewById(R.id.captureButton);

        previewView = findViewById(R.id.previewView);

        bCamera.setOnClickListener(this);

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if(result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null){
                    isReadPermissionGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null){
                    isReadPermissionGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

                if(result.get(Manifest.permission.CAMERA) != null){
                    isReadPermissionGranted = result.get(Manifest.permission.CAMERA);
                }
            }
        });
        requestPermission();

        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(()->{
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        },getExecutor());
    }

    private void requestPermission(){
        boolean minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = isWritePermissionGranted || minSDK;

        List<String> permissionRequest = new ArrayList<String>();

        if(!isWritePermissionGranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!isReadPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if(!isCameraPermissionGranted){
            permissionRequest.add(Manifest.permission.CAMERA);
        }

        if(!permissionRequest.isEmpty()){
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    private Executor getExecutor(){
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider){
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        //image capture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture, imageAnalysis);


    }

    @Override
    public void onClick(View v) {
        if(v==bCamera){
            capturePhoto();
        }
    }

    private void capturePhoto(){

        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        }else {
            File photoDir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(), "fruits/");

            if (!photoDir.exists()) {
                photoDir.mkdir();
            }

            Date date = new Date();
            String timeStamp = String.valueOf(date.getTime());
            String photoFilePath = photoDir.getAbsolutePath() + "/" + timeStamp + ".jpg";

            File photoFile = new File(photoFilePath);
        }*/

        long timeStamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");


        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(getApplicationContext(), "Processing...", Toast.LENGTH_SHORT).show();
                        Intent classifier = new Intent(getApplicationContext(), Classifier.class);
                        classifier.putExtra("FILE_URI", outputFileResults.getSavedUri());
                        startActivity(classifier);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getApplicationContext(), "Capture Failed: " + exception.getMessage() , Toast.LENGTH_SHORT).show();

                    }
                }
        );
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        Bitmap bitmap = convertImageProxyToBitmap(image);
        image.close();
    }

    private Bitmap convertImageProxyToBitmap(ImageProxy image) {
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length);
    }


}
