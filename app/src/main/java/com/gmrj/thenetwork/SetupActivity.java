package com.gmrj.thenetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {
private EditText userName,fullName,phoneNumber;
private Button save;
private CircleImageView profileImage;
private FirebaseAuth mAuth;
private DatabaseReference UsersRef;
private StorageReference UserProfileImageRef;
ProgressDialog loadingBar;
final static int GALLERY=1;
String currentUserId;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        userName=findViewById(R.id.textview);
        fullName=findViewById(R.id.fullname);
        phoneNumber=findViewById(R.id.phoneno);
        profileImage=findViewById(R.id.profile_image);
        save=findViewById(R.id.save);
      loadingBar=new ProgressDialog(this);
        progressDialog=new ProgressDialog(this);
        mAuth= FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile images");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountinfo();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SetupActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();


        }

        // Cuando se pulsa en el crop button
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Updating Profile Image");
                loadingBar.setMessage("Please wait while updating image..!");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                File file=new File(resultUri.getPath());

                try{
                    file=new Compressor(this).compressToFile(file);
                }
                catch (Exception e){

                }
                resultUri=Uri.fromFile(file);
                String currentUserID=mAuth.getCurrentUser().getUid();
                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {

                           // Toast.makeText(SetupActivity.this, "", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    UsersRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                       // startActivity(selfIntent);
                                                     //   Picasso.get().load(downloadUrl).into(profileImage);
                                                        setimage();
                                                        loadingBar.dismiss();
                                                        Toast.makeText(SetupActivity.this, "..uploaded image succesfully", Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(SetupActivity.this, "Error:unable to upload image", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void setimage() {
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.hii).into(profileImage);
                    }else {
                        Toast.makeText(SetupActivity.this, "please select profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveAccountinfo() {
        String username=userName.getText().toString();
        String fullname=fullName.getText().toString();
        String phoneno=phoneNumber.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(SetupActivity.this, "Enter username feild", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname)){
            Toast.makeText(SetupActivity.this, "Enter fullname feild", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(phoneno)){
            Toast.makeText(SetupActivity.this, "Enter phone no feild", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Saving information");
            progressDialog.setMessage("Please Wait ,while saving ur account");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            HashMap userMap=new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("phoneno",phoneno);
           UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if(task.isSuccessful()){
                       check();
                       Toast.makeText(SetupActivity.this, "Your Account is updated", Toast.LENGTH_SHORT).show();
                       progressDialog.dismiss();
                   }else {
                       String msg=task.getException().getMessage();
                       Toast.makeText(SetupActivity.this, "error:", Toast.LENGTH_SHORT).show();
                       progressDialog.dismiss();
                   }
               }
           });
        }


    }

    private void check() {
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("profileimage")){
                    if(dataSnapshot.hasChild("username")&&(dataSnapshot.hasChild("fullname"))){
                        sendUserToMainActivity();
                    }else {
                        Toast.makeText(SetupActivity.this, "enter all the fields", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(SetupActivity.this, "Select profile image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
       
    }

    private void sendUserToMainActivity() {
        Intent intent =new Intent(SetupActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
