package com.gmrj.thenetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class TextPostActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private Button updatePost;
    private EditText description,title;
    private FirebaseAuth mAuth;
    Uri imageUri;
    String desc,titleS;
    String downloadUrl;
    private  String saveCurrentDate;
    private  String saveCurrentTime;
    private  String postRandomName;
    private DatabaseReference usersRef,postsRef;
    private StorageReference postImageRef;
    ProgressDialog loadingBar;
    final static int GALLERY=1;
    String currentUserId;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_post);
        postImageRef= FirebaseStorage.getInstance().getReference();
        toolbar=findViewById(R.id.toolbar_post);
        mAuth=FirebaseAuth.getInstance();
        updatePost=findViewById(R.id.post_button);
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        postsRef= FirebaseDatabase.getInstance().getReference().child("posts");
        description =findViewById(R.id.post_describtion);
        title=findViewById(R.id.tittle);
        loadingBar=new ProgressDialog(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");
        currentUserId=mAuth.getCurrentUser().getUid();
        updatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInfo();
            }
        });
    }
    private void validateInfo() {
        desc =description.getText().toString().trim();
        titleS=title.getText().toString().trim();
        if(TextUtils.isEmpty(titleS) ){
            Toast.makeText(TextPostActivity.this, "Please write title for ur post", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(desc)){
            Toast.makeText(TextPostActivity.this, "Please write some description", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Posting ...");
            loadingBar.setMessage("please wait while posting");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            storingImageToStorage();
        }
    }
    private void storingImageToStorage() {
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMMM-dd");
        saveCurrentDate=currentDate.format(calendar.getTime());
        Calendar calendar2=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calendar2.getTime());

       /* postRandomName=saveCurrentDate+saveCurrentTime;

        StorageReference filepath=postImageRef.child("post images").child(imageUri.getLastPathSegment()+postRandomName+".jpg");

        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {


                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();*/
                            saveInfoToDatabase();

                        /*}
                    });
                }
            }


        });*/


    }
    private void saveInfoToDatabase() {

        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullname=dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage=dataSnapshot.child("profileimage").getValue().toString();
                    HashMap postsMap=new HashMap();
                    postsMap.put("uid",currentUserId);
                    postsMap.put("title",titleS);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("sort",saveCurrentTime+saveCurrentDate);
                    postsMap.put("description","");
                    postsMap.put("description2",desc);
                    postsMap.put("postimage","");
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname",userFullname);
                    postsRef.child(currentUserId+postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                SendUserToMain();
                                loadingBar.dismiss();
                                Toast.makeText(TextPostActivity.this, "post updated", Toast.LENGTH_SHORT).show();
                            }else {
                                loadingBar.dismiss();
                                Toast.makeText(TextPostActivity.this, "error occured while posting", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        Intent intent =new Intent(TextPostActivity.this,MainActivity.class);
        startActivity(intent);
    }
}

