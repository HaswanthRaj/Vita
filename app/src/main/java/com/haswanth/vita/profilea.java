package com.haswanth.vita;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class profilea extends AppCompatActivity {

    private TextView name, email;
    DatabaseReference db;
    private Button logout;
    public ImageView profilepic;
    StorageReference storageReference;
    private RelativeLayout rootLayout;
    private AnimationDrawable animDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilea);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        rootLayout = (RelativeLayout) findViewById(R.id.profile_root);


        name = (TextView) findViewById(R.id.pname);
        email = (TextView) findViewById(R.id.pemail);
        logout = (Button) findViewById(R.id.logout);
        profilepic = (ImageView) findViewById(R.id.userprofile_image);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseDatabase.getInstance().getReference("Users");

        /*StorageReference profileRef = storageReference.child("profilepics/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profilepic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(profilea.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });**/
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(profilea.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (ds.getKey().equals(id)) {
                        name.setText(user.fullname);
                        email.setText(user.email);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent opengallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(opengallery, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            //set image
            profilepic.setImageURI(data.getData());
            //upload image
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri imageuri) {
        //upload image to firebase
        StorageReference filef = storageReference.child("profilepics/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        filef.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //get url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) {
                    ;
                }
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    //set image
                    Toast.makeText(profilea.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    db.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profilepic").setValue(downloadUri);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(profilea.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }


}