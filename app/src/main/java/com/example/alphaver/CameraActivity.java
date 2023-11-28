package com.example.alphaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CameraActivity extends AppCompatActivity {

    ImageView imageView;
    Button takePic,recognizeText;

    TextView recognizedTextTv;

    private final int OPEN_CAMERA_CODE = 1234;
    private final int TAKE_A_PIC_CODE = 2345;

    private FirebaseStorage storage;

    private TextRecognizer textRecognizer;

    private Uri filepath;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        takePic =(Button) findViewById(R.id.takePic);
        recognizeText = (Button) findViewById(R.id.picToText);
        imageView = (ImageView)findViewById(R.id.imageView);
        recognizedTextTv = (TextView) findViewById(R.id.textView);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }

        });

        recognizeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(filepath==null)
                {
                    Toast.makeText(getApplicationContext(), "Take a picture first.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    recognizeTextFromImage();
                }
            }

        });
    }

    private void recognizeTextFromImage()
    {
        try {
            InputImage inputImage = InputImage.fromFilePath(this,filepath);
            Task<Text> textTaskResult = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String recognizedText = text.getText();
                            recognizedTextTv.setText(recognizedText);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Failed recognizing text due to:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed preparing image due to: " + e.getMessage(), Toast.LENGTH_LONG).show();
            //throw new RuntimeException(e);
        }
    }

    private void checkCameraPermission()
    {
        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            } ,OPEN_CAMERA_CODE);
        }
        else {
            takeApic();
        }


    }
    private void takeApic() {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            Toast.makeText(getApplicationContext(), "picture taken", Toast.LENGTH_LONG).show();
            startActivityForResult(takePicIntent,TAKE_A_PIC_CODE);
        }

    }

   @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==OPEN_CAMERA_CODE&&grantResults[1]== PackageManager.PERMISSION_GRANTED)
        {
            takeApic();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==TAKE_A_PIC_CODE&&resultCode==RESULT_OK)
        {

            uploadImage(data);
        }
    }

    private void uploadImage(Intent data)
    {
        Bundle extras = data.getExtras();
        Bitmap bitmap = (Bitmap) extras.get("data");
        imageView.setImageBitmap(bitmap);
        filepath = getImageUri(getApplicationContext(),bitmap);
        if(filepath!=null)
        {
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "image uploaded", Toast.LENGTH_LONG).show();
                }
            }) .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to upload", Toast.LENGTH_LONG).show();
                }
            });

        }
    }
    private Uri getImageUri(Context context, Bitmap bitmap)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"title",null);
        return Uri.parse(path);
    }
}