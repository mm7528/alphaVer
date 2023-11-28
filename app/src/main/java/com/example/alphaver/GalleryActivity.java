package com.example.alphaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class GalleryActivity extends AppCompatActivity {

    private ImageView pic;
    Button gallery, download;
    public Uri imageUri;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    StorageReference imageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        pic = (ImageView) findViewById(R.id.pic);
        gallery = (Button) findViewById(R.id.gallery);
        download = (Button) findViewById(R.id.download);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }


        });

        download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                downloadPicture();
            }

        });
    }



    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==RESULT_OK && data!=null&&data.getData()!=null)
        {
            imageUri = data.getData();
            //pic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {
        final String randomKey = UUID.randomUUID().toString();
        imageRef = storageReference.child("images/"+ randomKey);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "image uploaded", Toast.LENGTH_LONG).show();
            }
        })      .addOnFailureListener(new OnFailureListener() {
            @Override
              public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to upload", Toast.LENGTH_LONG).show();
             }
        });

    }

    private void downloadPicture() {
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(GalleryActivity.this).load(uri).into(pic);
                Toast.makeText(getApplicationContext(), "download successful", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to download", Toast.LENGTH_LONG).show();

            }
        });
    }
}