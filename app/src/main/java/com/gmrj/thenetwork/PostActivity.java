package com.gmrj.thenetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {
private Toolbar toolbar;
private ImageButton SelectImage;
private Button updatePost;
private EditText description;
private FirebaseAuth mAuth;
    Uri imageUri;
    String desc;
    String downloadUrl;
    private  String saveCurrentDate;
    private  String saveCurrentTime;
    private  String postRandomName;
private DatabaseReference usersRef,postsRef,notifyRef;
private StorageReference postImageRef;
    ProgressDialog loadingBar;
    final static int GALLERY=1;
    String currentUserId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postImageRef= FirebaseStorage.getInstance().getReference();
        toolbar=findViewById(R.id.toolbar_post);
        mAuth=FirebaseAuth.getInstance();
        SelectImage =findViewById(R.id.post_image);
        updatePost=findViewById(R.id.post_button);
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        postsRef= FirebaseDatabase.getInstance().getReference().child("posts");
        notifyRef= FirebaseDatabase.getInstance().getReference().child("notifications");
        description =findViewById(R.id.post_describtion);
        loadingBar=new ProgressDialog(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");
        currentUserId=mAuth.getCurrentUser().getUid();
        SelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        updatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInfo();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
          imageUri   = result.getUri();
            SelectImage.setImageURI(imageUri);

        }

    }
    private void openGallery() {
        /*Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY);*/
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(PostActivity.this);
    }
    private void validateInfo() {
       desc =description.getText().toString();
        if(imageUri==null ){
            Toast.makeText(PostActivity.this, "Please select post image", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(desc)){
            Toast.makeText(PostActivity.this, "Please write some description", Toast.LENGTH_SHORT).show();
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

        postRandomName=saveCurrentDate+saveCurrentTime;

        StorageReference filepath=postImageRef.child("post images").child(imageUri.getLastPathSegment()+postRandomName+".jpg");
        File file=new File(imageUri.getPath());
        File newFile;
        try{
         file=new  Compressor(this).compressToFile(file);
        }
         catch (Exception e){

         }
         imageUri=Uri.fromFile(file);
        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {


                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                           saveInfoToDatabase();

                        }
                    });
                }
            }


        });


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
                    postsMap.put("title","");
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("sort",saveCurrentTime+saveCurrentDate);
                    postsMap.put("description",desc);
                    postsMap.put("description2","");
                    postsMap.put("postimage",downloadUrl);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("fullname",userFullname);
                    HashMap<String,String> chatNotification=new HashMap<>();
                    chatNotification.put("from",currentUserId);
                    chatNotification.put("type"," new post");
                    postsRef.child(currentUserId+postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                SendUserToMain();
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "post updated", Toast.LENGTH_SHORT).show();
                            }else {
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "error boccured while posting", Toast.LENGTH_SHORT).show();
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
        Intent intent =new Intent(PostActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
