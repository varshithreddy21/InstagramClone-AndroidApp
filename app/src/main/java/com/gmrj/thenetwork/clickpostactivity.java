package com.gmrj.thenetwork;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class clickpostactivity extends AppCompatActivity {
private ImageView postImage;
private TextView description,description2,title;
private Button edit,delete;
private Toolbar toolbar;
private String PostKey,FullName,currentUserId,databaseUserId,image,Description,Description2,title2;
private DatabaseReference postRef;
private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clickpostactivity);
        postImage=findViewById(R.id.postImage);
        description=findViewById(R.id.postDescription);
        description2=findViewById(R.id.description2_post);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        title=findViewById(R.id.title_post);
        edit=findViewById(R.id.editpost);
        edit.setVisibility(View.INVISIBLE);
        delete=findViewById(R.id.deletepost);
        delete.setVisibility(View.INVISIBLE);
        toolbar=findViewById(R.id.appBar2);
        PostKey=getIntent().getExtras().get("postkey").toString();
        FullName=getIntent().getExtras().get("fullname").toString().concat("'s post");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(FullName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        postRef= FirebaseDatabase.getInstance().getReference().child("posts").child(PostKey);
        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    databaseUserId=dataSnapshot.child("uid").getValue().toString();
                    if(currentUserId.equals(databaseUserId)){
                        edit.setVisibility(View.VISIBLE);
                        delete.setVisibility(View.VISIBLE);
                    }
                    if(TextUtils.isEmpty(dataSnapshot.child("title").getValue().toString())){
                        title.setVisibility(View.GONE);
                        description2.setVisibility(View.GONE);
                        postImage.setVisibility(View.VISIBLE);
                        description.setVisibility(View.VISIBLE);
                        image = dataSnapshot.child("postimage").getValue().toString();
                        Picasso.get().load(image).into(postImage);
                        Description = dataSnapshot.child("description").getValue().toString();
                        description.setText(Description);
                    }else{title.setVisibility(View.VISIBLE);
                        description2.setVisibility(View.VISIBLE);
                        postImage.setVisibility(View.GONE);
                        description.setVisibility(View.GONE);
                         Description2 = dataSnapshot.child("description2").getValue().toString();
                        title2 = dataSnapshot.child("title").getValue().toString();
                        title.setText(title2);
                        description2.setText(Description2);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCurrentPost(Description,Description2,title2);
            }
        });

    }

    private void EditCurrentPost(final String description, final String description2,final String title) {
        LinearLayout linearLayout=new LinearLayout(clickpostactivity.this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
       TextView textView=new TextView(clickpostactivity.this);
       textView.setText("Edit Post");
       textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
       textView.setTextSize(45);
       linearLayout.addView(textView);

        final EditText editText2=new EditText(clickpostactivity.this);
        editText2.setHint("Title");
        final EditText editText=new EditText(clickpostactivity.this);
        editText.setHint("Description");


        if(TextUtils.isEmpty(description)){

            editText.setText(description2);
            editText2.setText(title);
            linearLayout.addView(editText2);
            linearLayout.addView(editText);
        }else {
            linearLayout.addView(editText);
            editText.setText(description);

        }
        Button button =new Button(clickpostactivity.this);
        button.setText("update");
        Button button2 =new Button(clickpostactivity.this);
        button2.setText("cancel");
        button.setBackgroundResource(R.drawable.btn);
        button2.setBackgroundResource(R.drawable.btn);
        linearLayout.addView(button);
        linearLayout.addView(button2);
        linearLayout.setPadding(5,5,5,5);

        final Dialog dialog=new Dialog(clickpostactivity.this);
        dialog.setContentView(linearLayout);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(description))
                { postRef.child("description2").setValue(editText.getText().toString());
                postRef.child("title").setValue(editText2.getText().toString());
                }
                else {
                    postRef.child("description").setValue(editText.getText().toString());
                }
                Toast.makeText(clickpostactivity.this,"Updated Post SuccesFully",Toast.LENGTH_SHORT).show();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.cancel();
                    }
                }
        );

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);
    }

    private void DeleteCurrentPost() {

        postRef.removeValue();
        SendUserToMain();
        Toast.makeText(this,"post deleted",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id== android.R.id.home)
        {
            SendUserToMain();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMain() {
        Intent intent =new Intent(clickpostactivity.this,MainActivity.class);
        startActivity(intent);
    }
}