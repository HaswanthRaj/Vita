package com.haswanth.vita;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class signup extends AppCompatActivity implements View.OnClickListener {

    private TextView banner,registeruser;
    private EditText editname,editemail,editpassword;
    private ProgressBar progressBar;
    private ImageView profilepic;
    StorageReference storageReference;
    public Uri imageUri;
    private String TAG="test";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        banner=(TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);
        profilepic=(ImageView) findViewById(R.id.profile_image);
        profilepic.setOnClickListener(this);
        storageReference = FirebaseStorage.getInstance().getReference();

        registeruser = (Button) findViewById(R.id.registeruser);
        registeruser.setOnClickListener(this);

        editname= (EditText) findViewById(R.id.name);
        editemail= (EditText) findViewById(R.id.email);
        editpassword= (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressbarsplash);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.banner) {
            startActivity(new Intent(this, login.class));
        } else if (v.getId() == R.id.registeruser) {
            registeruser();
        } else if (v.getId() == R.id.profile_image) {
            Intent opengallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(opengallery, 1);
        }

    }

    private void registeruser(){
        String email = editemail.getText().toString().trim();
        String password = editpassword.getText().toString().trim();
        String fullname = editname.getText().toString().trim();

        if(fullname.isEmpty()){
            editname.setError("Full name is required");
            editname.requestFocus();
            return;
        }
        if(email.isEmpty()){
            editemail.setError("Email is required");
            editemail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editemail.setError("Please provide valid email");
            editemail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editpassword.setError("password is required");
            editpassword.requestFocus();
            return;
        }
        if(password.length()<6){
            editpassword.setError("password should have atleast 6 characters");
            editpassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registeruser.setVisibility(View.GONE);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(fullname,email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(signup.this,"Registered Succesfully! Check your email for verfication , check spam mails", Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(signup.this,login.class));
                                                progressBar.setVisibility(View.GONE);
                                                registeruser.setVisibility(View.VISIBLE);
                                                uploadImage(imageUri);
                                            }else{
                                                Toast.makeText(signup.this,"Registeration Failed!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                                registeruser.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
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
            imageUri = data.getData();
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

                }
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    //set image
                    Log.d(TAG, "onSuccess: ");
                    Toast.makeText(signup.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    //db.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profilepic").setValue(downloadUri);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(signup.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }
}