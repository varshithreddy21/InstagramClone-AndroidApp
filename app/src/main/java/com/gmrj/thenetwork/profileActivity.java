package com.gmrj.thenetwork;

import android.app.Dialog;
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
import android.widget.TextView;
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

public class profileActivity extends AppCompatActivity {
private CircleImageView profileimage;
private TextView username,fullname,phno;
private FirebaseAuth mAuth;
private DatabaseReference usersRef;
private StorageReference UserProfileImageRef;
private String currentUserId,UserId;
private Toolbar toolbar;
private String Username,Fullname,Phoneno;
private ImageButton editB;
private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileimage=findViewById(R.id.profileimage2);
        username=findViewById(R.id.username);
        fullname=findViewById(R.id.fullname);
        phno=findViewById(R.id.phnno);
        mAuth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.appBar5);
            editB=findViewById(R.id.editB);
            loadingBar=new ProgressDialog(this);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile images");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");
        currentUserId= getIntent().getExtras().get("userId").toString();
        UserId=mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        if(currentUserId.equals(UserId)){
            editB.setVisibility(View.VISIBLE);
        }
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Username=dataSnapshot.child("username").getValue().toString();
                    username.setText(Username);
                     Fullname=dataSnapshot.child("fullname").getValue().toString();
                    fullname.setText(Fullname);
                    Phoneno=dataSnapshot.child("phoneno").getValue().toString();
                    phno.setText(Phoneno);
                    String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(profileImage).into(profileimage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentUserId.equals(UserId)) {
                    changeProfileImage();
                }
            }
        });
    }

    private void changeProfileImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(profileActivity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



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

                                    usersRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        // startActivity(selfIntent);
                                                        //   Picasso.get().load(downloadUrl).into(profileImage);
                                                        setimage();
                                                        loadingBar.dismiss();
                                                        Toast.makeText(profileActivity.this, "..uploaded image succesfully", Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(profileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(profileActivity.this, "Error:unable to upload image", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }
    private void setimage() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.hii).into(profileimage);
                    }else {
                        Toast.makeText(profileActivity.this, "please select profile image", Toast.LENGTH_SHORT).show();
                    }
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
        Intent intent =new Intent(profileActivity.this,MainActivity.class);
        startActivity(intent);
    }
    public void editButton(View view){
        final EditText username,fullname,phonenumber;
        Button button;
        final Dialog dialog=new Dialog(this);
        dialog.setContentView(R.layout.editdialog);
            username= dialog.findViewById(R.id.user);
            fullname= dialog.findViewById(R.id.full);
            phonenumber= dialog.findViewById(R.id.phn);
            button=dialog.findViewById(R.id.update);
            username.setText(Username);
            fullname.setText(Fullname);
            phonenumber.setText(Phoneno);
            dialog.show();
           button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(TextUtils.isEmpty(username.getText())||TextUtils.isEmpty(fullname.getText())||TextUtils.isEmpty(phonenumber.getText())){
                       Toast.makeText(profileActivity.this,"enter all the feilds",Toast.LENGTH_SHORT).show();
                   }else {
                       updateDetails(username.getText().toString(), fullname.getText().toString(), phonenumber.getText().toString());
                       dialog.dismiss();
                   }
               }
           });

    }

    private void updateDetails(String username,String fullname,String phoneno) {

        usersRef.child("username").setValue(username);
        usersRef.child("fullname").setValue(fullname);
        usersRef.child("phoneno").setValue(phoneno).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(profileActivity.this,"updated succesfully",Toast.LENGTH_SHORT).show();
                }else{
                    String msg=task.getException().getMessage();
                    Toast.makeText(profileActivity.this,"error: "+msg,Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
