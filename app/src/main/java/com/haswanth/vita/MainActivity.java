package com.haswanth.vita;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haswanth.vita.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button create;

    ActivityMainBinding binding;

    RecyclerView recyclerView;
    DatabaseReference db;

    private TextView welcome,welcomeuser;
    Calendar calendar = Calendar.getInstance();
    private RelativeLayout rootLayout;
    private SearchView searchView;
    private AnimationDrawable animDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        rootLayout = (RelativeLayout) findViewById(R.id.root_layout);



        //Bottom navigation code

        binding.bottomnav.setOnNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
            }
            if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(MainActivity.this, profilea.class));
            }
            if (item.getItemId() == R.id.chat) {
                startActivity(new Intent(MainActivity.this, chat.class));
            }
            if(item.getItemId() == R.id.inventory){
                startActivity(new Intent(MainActivity.this, Inventory.class));
            }
            return true;

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, loginsignup.class));
            finish();
        }else{
            getusernam(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),welcomeuser);
        }
    }
    public String getusernam(String id, TextView username) {

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
        final String[] name = new String[1];
        final String[] nam = {""};
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (Objects.equals(ds.getKey(), id)) {
                        assert user != null;
                        nam[0] = user.fullname;

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return name[0];
    }

}