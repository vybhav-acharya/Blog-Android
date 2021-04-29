package com.example.blogactivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {
    EditText description_blog, title_blog;
    Button upload;
    ImageView blog_image;
    Uri images_uri = null;
    private static final int GALLERY_IMAGE_CODE = 100;
    private static final int CAMERA_IMAGE_CODE = 200;
    ProgressDialog pd;
    FirebaseAuth auth;

    void permission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_CODE) {
                images_uri = data.getData();
                blog_image.setImageURI(images_uri);

            }
            if (requestCode == CAMERA_IMAGE_CODE) {
                blog_image.setImageURI(images_uri);

            }
        }
    }

    private void cameraPick() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp pick");
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp desc");
        images_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, images_uri);
        startActivityForResult(intent, CAMERA_IMAGE_CODE);
    }

    private void galleryPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_CODE);

    }


    private void imagePickDialog() {
        String[] options = {"camera", "gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose one among the following");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    cameraPick();
                }
                if (which == 1) {

                    galleryPick();
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_post);
        permission();
        title_blog = (EditText) findViewById(R.id.title_blog);
        description_blog = (EditText) findViewById(R.id.description_blog);
        upload = (Button) findViewById(R.id.Upload);
        blog_image = findViewById(R.id.post_image_blog);
        pd = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();

        blog_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String title = title_blog.getText().toString();
                String description = description_blog.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    title_blog.setError("Title is required");
                } else if (TextUtils.isEmpty(description)) {
                    description_blog.setError("Description is required");
                } else {
                    uploadData(title, description);
                }
            }
        });
    }


    private void uploadData(final String title, final String description) {

        pd.setMessage("Publishing  the post");
        pd.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String filepath = "Posts/"+"post_"+timeStamp;

        if (blog_image.getDrawable() != null){
            //getImage from Image view ;
            Bitmap bitmap = ((BitmapDrawable)blog_image.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG , 100 , baos);
            byte[] data = baos.toByteArray();


            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filepath);
            reference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){



                                FirebaseUser user = auth.getCurrentUser();

                                HashMap<String , Object> hashMap = new HashMap<>();

                                hashMap.put("uid" , user.getUid());
                                hashMap.put("uEmail" , user.getEmail());
                                hashMap.put("pId" , timeStamp);
                                hashMap.put("pTitle" , title);
                                hashMap.put("pImage" , downloadUri);
                                hashMap.put("pDescription" , description);
                                hashMap.put("pTime" ,  timeStamp);

                                //now we will put the data to firebase database
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();;
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                                title_blog.setText("");
                                                description_blog.setText("");
                                                blog_image.setImageURI(null);
                                                images_uri = null ;

                                                //when post is publised user must go to home activity means main dashboad
                                                startActivity(new Intent(AddPostActivity.this , MainActivity.class));


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
    }

}
