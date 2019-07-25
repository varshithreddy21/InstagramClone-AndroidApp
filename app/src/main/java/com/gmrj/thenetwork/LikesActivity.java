package com.gmrj.thenetwork;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class LikesActivity extends AppCompatActivity {
private Toolbar toolbar;
private FirebaseAuth mAuth;
private RecyclerView LikesList;
private String Post_key,currentUserId;
private DatabaseReference usersRef,postsRef,LikesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        toolbar=findViewById(R.id.appBar4);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Agreed");
        mAuth= FirebaseAuth.getInstance();
        LikesList=findViewById(R.id.likesList);
        Post_key=getIntent().getExtras().get("postkey").toString();
        usersRef= FirebaseDatabase.getInstance().getReference().child("users");
        currentUserId=mAuth.getCurrentUser().getUid();
        postsRef=FirebaseDatabase.getInstance().getReference().child("posts").child(Post_key).child("comments");
        LikesRef=FirebaseDatabase.getInstance().getReference().child("likes").child(Post_key);
        LikesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        LikesList.setLayoutManager(linearLayoutManager);







    }
    @Override
    protected void onStart() {
        super.onStart();
       FirebaseRecyclerOptions<LikesClass> options=new FirebaseRecyclerOptions.Builder<LikesClass>()
               .setQuery(LikesRef,LikesClass.class).build();
    FirebaseRecyclerAdapter<LikesClass,LikesView> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<LikesClass, LikesView>(options) {
        @Override
        protected void onBindViewHolder(@NonNull LikesView holder, int position, @NonNull LikesClass model) {
            holder.setProfileimage(model.getProfileimage());
            holder.setFullname(model.getFullname());
        }

        @NonNull
        @Override
        public LikesView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_likes_layout,viewGroup,false);
            LikesActivity.LikesView viewHolder=new LikesActivity.LikesView(view);
            return viewHolder;
        }
    } ;
       LikesList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    private static class LikesView extends RecyclerView.ViewHolder{
        View mView;
        LikesView(View view){
            super(view);
            mView=view;
        }
        public void setFullname(String fullname) {
            TextView textView=mView.findViewById(R.id.likes_fullname);
            textView.setText(fullname);
        }

        public void setProfileimage(String profileimage) {
            CircleImageView imageView=mView.findViewById(R.id.likes_profileimage);
            Picasso.get().load(profileimage).into(imageView);
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
        Intent intent =new Intent(LikesActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
