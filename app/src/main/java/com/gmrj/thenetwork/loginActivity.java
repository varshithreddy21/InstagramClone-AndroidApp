package com.gmrj.thenetwork;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class loginActivity extends AppCompatActivity {
TextView textView;
EditText email,password;
Button login;
private FirebaseAuth mAuth;
private DatabaseReference usersRef;
private int STORAGE_PERMISSION_CODE = 1;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(loginActivity.this, "You have already granted this permission!",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }
        mAuth=FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        textView=findViewById(R.id.textView_login);
        email=findViewById(R.id.email_login);
        password=findViewById(R.id.password_login);
        login=findViewById(R.id.login);
        progressDialog=new ProgressDialog(this);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeMeToRegisterActivity();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLOgin();
            }
        });
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(loginActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            sendUserToMain();
        }

    }

    private void AllowUserToLOgin() {
        final String myEmail = email.getText().toString();
        final String myPassword = password.getText().toString();
        if (TextUtils.isEmpty(myEmail) || TextUtils.isEmpty(myPassword)) {
            Toast.makeText(loginActivity.this, "Fill all the feilds", Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.setTitle("Logging In");
            progressDialog.setMessage("Please Wait while Logging In ur Account");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(true);
            mAuth.signInWithEmailAndPassword(myEmail,myPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToMain();
                        Toast.makeText(loginActivity.this,"you are logged in",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }else {
                        String msg=task.getException().getMessage();
                        Toast.makeText(loginActivity.this,"Error : "+msg,Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMain() {
        String CurrentUserId=mAuth.getCurrentUser().getUid();
        String deviceToken= FirebaseInstanceId.getInstance().getToken();
        usersRef.child(CurrentUserId).child("deviceToken").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent =new Intent(loginActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    private void takeMeToRegisterActivity(){
        Intent intent =new Intent(loginActivity.this,register.class);
       // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
