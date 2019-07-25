package com.gmrj.thenetwork;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class comments extends AppCompatActivity {
Toolbar toolbar;
private ImageButton commentSend;
private String Post_key,currentUserId;
private EditText commentInput;
private RecyclerView CommentsList;
private DatabaseReference usersRef,postsRef;
private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        toolbar=findViewById(R.id.app_bar3);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Comments");
        mAuth=FirebaseAuth.getInstance();
        CommentsList=findViewById(R.id.comments_lists);
        Post_key=getIntent().getExtras().get("postkey").toString();
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        currentUserId=mAuth.getCurrentUser().getUid();
        postsRef=FirebaseDatabase.getInstance().getReference().child("posts").child(Post_key).child("comments");
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);
        commentInput=findViewById(R.id.comment_edit);
        commentSend=findViewById(R.id.sendcomment);
        commentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName=dataSnapshot.child("username").getValue().toString();
                            String profileimage=dataSnapshot.child("profileimage").getValue().toString();
                            validateComment(userName,profileimage);
                            commentInput.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<CommentsClass> options=new FirebaseRecyclerOptions.Builder<CommentsClass>()
                .setQuery(postsRef.orderByChild("date"),CommentsClass.class)
                .build();
        FirebaseRecyclerAdapter<CommentsClass,CommentsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<CommentsClass, CommentsViewHolder>
                (
                  options
                ) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull CommentsClass model) {
               holder.setComment(model.getComment());
               holder.setDate(model.getDate());
               holder.setTime(model.getTime());
               holder.setProfileimage(model.getProfileimage());
               holder.setUsername(model.getUsername());
            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout,viewGroup,false);
                comments.CommentsViewHolder viewHolder=new comments.CommentsViewHolder(view);
                return viewHolder;
            }
        };
        CommentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHolder(View view){
            super(view);
            mView=view;
        }
        public void setComment(String comment) {
            TextView myComment=mView.findViewById(R.id.comment_text);
            myComment.setText(("@")+comment+("  "));
        }
        public void setDate(String date) {
            TextView myDate=mView.findViewById(R.id.comment_date);
            myDate.setText(("")+date);
        }
        public void setTime(String time) {
            TextView myTime=mView.findViewById(R.id.comment_time);
            myTime.setText(("")+time);
        }
        public void setUsername(String username) {
            TextView myUsername=mView.findViewById(R.id.commment_username);
            myUsername.setText(username);
        }
        public void setProfileimage(String profileimage) {
            CircleImageView imageView=mView.findViewById(R.id.comment_photo);
            Picasso.get().load(profileimage).into(imageView);
        }


    }
    private void validateComment(String userName,String profileimage) {

        String commentText=commentInput.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(comments.this, "Write comment",
                    Toast.LENGTH_SHORT).show();
        }else {
            Calendar calendar=Calendar.getInstance();
            SimpleDateFormat currentDate=new SimpleDateFormat("MMMM-dd");
           final String saveCurrentDate=currentDate.format(calendar.getTime());
            Calendar calendar2=Calendar.getInstance();
            SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
           final String saveCurrentTime=currentTime.format(calendar2.getTime());
           final String Randomkey=currentUserId+saveCurrentDate
                   +saveCurrentTime;
            HashMap hashMap=new HashMap();
            hashMap.put("uid",currentUserId);
            hashMap.put("comment",commentText);
            hashMap.put("profileimage",profileimage);
            hashMap.put("date",saveCurrentDate);
            hashMap.put("time",saveCurrentTime);
            hashMap.put("username",userName);
           postsRef.child(Randomkey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if(task.isSuccessful()){
                       Toast.makeText(comments.this, "commented succesfully",
                               Toast.LENGTH_SHORT).show();
                   }
                   else {
                       Toast.makeText(comments.this, "error occured try again",
                               Toast.LENGTH_SHORT).show();
                   }
               }
           });
        }
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
        Intent intent =new Intent(comments.this,MainActivity.class);
        startActivity(intent);
    }
}
