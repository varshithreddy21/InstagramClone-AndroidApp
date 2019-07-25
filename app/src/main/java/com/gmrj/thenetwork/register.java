package com.gmrj.thenetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class register extends AppCompatActivity {
EditText email,password,passwordConfirm;
Button button;
private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            Toast.makeText(register.this, "logged in",
                    Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this,"alredy in",Toast.LENGTH_SHORT).show();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password_register);
        passwordConfirm=findViewById(R.id.password_confirm);
        progressDialog=new ProgressDialog(this);


    }


    public void onRegister(View view) {
        mAuth = FirebaseAuth.getInstance();
        final String myEmail = email.getText().toString();
        final String myPassword = password.getText().toString();
        final String myPassword2 = passwordConfirm.getText().toString();
        if (TextUtils.isEmpty(myEmail) || TextUtils.isEmpty(myPassword) || TextUtils.isEmpty(myPassword2)) {
            Toast.makeText(register.this, "Fill all the feilds", Toast.LENGTH_SHORT).show();
        }
        if (myPassword.equals(myPassword2)) {

             progressDialog.setTitle("Creating New Account");
             progressDialog.setMessage("Please Wait while creating your Account");
             progressDialog.show();
             progressDialog.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(myEmail, myPassword)
                    .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                String CurrentUserId=mAuth.getCurrentUser().getUid();
                                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                                usersRef.child(CurrentUserId).child("deviceToken").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d("TAG", "createUserWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            SendUserTosetup();
                                            Toast.makeText(register.this, "Succesfully Created User"
                                                    , Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });




                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                String msg=task.getException().getMessage();
                                Toast.makeText(register.this, "error: "+msg
                                        , Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }


                        }


                    });
        }else {
            Toast.makeText(register.this, "Passwords dont match.."
                    , Toast.LENGTH_SHORT).show();
        }
    }
    private void SendUserTosetup() {
        Intent intent =new Intent(register.this,SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
