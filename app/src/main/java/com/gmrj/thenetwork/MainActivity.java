package com.gmrj.thenetwork;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private DatabaseReference userRef,postsRef,likesRef;
    private DrawerLayout drawerLayout;
    private NavigationView nav;
    private RecyclerView postList;
    private TextView usernamee;
    private Toolbar toolbar;
    private ImageButton postButton;
    private Dialog dialog;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private ProgressBar progressBar;
    private NotificationManagerCompat notificationManagerCompat;

    boolean visible=false;
    Boolean likeChecker=false;
    String yes="false";
    int dialogman=0;
    String currentUserId,fullname,image;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CircleImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth=FirebaseAuth.getInstance();
        userRef=FirebaseDatabase.getInstance().getReference().child("users");
        postsRef=FirebaseDatabase.getInstance().getReference().child("posts");
        likesRef=FirebaseDatabase.getInstance().getReference().child("likes");
        drawerLayout=findViewById(R.id.drawable_layout);
        nav=findViewById(R.id.nav_nav);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dialog=new Dialog(this);
        postButton=findViewById(R.id.add_post);
         progressBar=new ProgressBar(this);
        currentUserId=mAuth.getCurrentUser().getUid();
        notificationManagerCompat =NotificationManagerCompat.from(this);
        Notification notification=new NotificationCompat.Builder(this,App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("hello")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        notificationManagerCompat.notify(1,notification);
        getSupportActionBar().setTitle("Semaphores");
        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Intent intent =new Intent(MainActivity.this,register.class);
       // startActivity(intent);
        View navView=nav.inflateHeaderView(R.layout.nav_header);
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                UserMenuSelect(menuItem);

                return false;
            }
        });
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.hasChild("admin")){
                    yes=dataSnapshot.child("admin").getValue().toString();
                    if(yes.equals("true")){
                        postButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        postList=findViewById(R.id.users_posts);
        postList.setHasFixedSize(true);
        postList.setItemViewCacheSize(20);
        postList.setDrawingCacheEnabled(true);
        postList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        imageView=navView.findViewById(R.id.profile_image);
        usernamee=navView.findViewById(R.id.textview);


        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")&&dataSnapshot.hasChild("fullname")) {
                        fullname=dataSnapshot.child("fullname").getValue().toString();
                        usernamee.setText(fullname);
                        image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.kk).into(imageView);
                    }else {
                        Toast.makeText(MainActivity.this, "please select profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag();


            }
        });

    }

    private void frag() {
        Button submit;
        dialog.setContentView(R.layout.post_dailog);
        Toolbar toolbar2=dialog.findViewById(R.id.appBar);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setTitle("Post Type");
        radioGroup=dialog.findViewById(R.id.radioGroup);


        submit=dialog.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioid=radioGroup.getCheckedRadioButtonId();
                radioButton=dialog.findViewById(radioid);
                switch (radioButton.getText().toString()) {
                    case "Image":
                        sendPostActivity();
                        break;
                    case "Text":
                        sendTextActivity();
                        break;

                    default:
                        Toast.makeText(MainActivity.this, "please select any radio button", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
       dialog.show();

    }

    private void DisplayAllUsersPost() {
        FirebaseRecyclerOptions<Posts> options=new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postsRef.orderByChild("date"),Posts.class)
                .build();
        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (
                        options
                )
        {
            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, final int position, @NonNull final Posts model) {


               final String PostKey=getRef(position).getKey();
                   holder.postImage.setVisibility(View.VISIBLE);
                   holder.description2.setVisibility(View.VISIBLE);
                   Picasso.get().load(model.getProfileimage()).into(holder.profileimage);
                   if (TextUtils.isEmpty(model.getPostimage())) {

                       holder.postImage.setVisibility(View.GONE);
                       holder.description.setVisibility(View.GONE);
                       holder.tittle.setVisibility(View.VISIBLE);
                       holder.description2.setVisibility(View.VISIBLE);
                       holder.tittle.setText(model.getTitle());
                       holder.description2.setText(model.getDescription2());
                   } else {
                       holder.tittle.setVisibility(View.GONE);
                       holder.description2.setVisibility(View.GONE);
                       holder.postImage.setVisibility(View.VISIBLE);
                       holder.description.setVisibility(View.VISIBLE);
                       Picasso.get().load(model.getPostimage()).into(holder.postImage);
                       holder.description.setText(model.getDescription());
                   }
                   holder.fullname.setText(model.getFullname());

                   final String date1 = model.getDate() + " at " + model.time;
                   holder.dateTime.setText(date1);

                   holder.setLikeButtonStatus(PostKey);
                   holder.commentButton.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent intent = new Intent(MainActivity.this, comments.class);
                           intent.putExtra("postkey", PostKey);
                           startActivity(intent);

                       }
                   });
                holder.noOfAgrees.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, LikesActivity.class);
                        intent.putExtra("postkey", PostKey);
                        startActivity(intent);
                    }
                });

                holder.profileimage.setOnClickListener(new View.OnClickListener() {
                    String uid;

                    @Override
                    public void onClick(View v) {
                        postsRef.child(PostKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    uid = dataSnapshot.child("uid").getValue().toString();
                                    Intent intent = new Intent(MainActivity.this, profileActivity.class);
                                    intent.putExtra("userId", uid);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, clickpostactivity.class);
                        intent.putExtra("postkey", PostKey);
                        intent.putExtra("fullname", model.getFullname());
                        startActivity(intent);
                    }
                });
                holder.agreeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (likeChecker.equals(true)) {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                                        likesRef.child(PostKey).child(currentUserId).removeValue();
                                        likeChecker = false;
                                    } else {
                                        likesRef.child(PostKey).child(currentUserId).setValue(true);
                                        likesRef.child(PostKey).child(currentUserId).child("fullname").setValue(fullname);
                                        likesRef.child(PostKey).child(currentUserId).child("profileimage").setValue(image);
                                        likeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });


            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.allpostlayout,viewGroup,false);
                PostsViewHolder viewHolder=new PostsViewHolder(view);

                return viewHolder;
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();



    }
    public static class PostsViewHolder extends  RecyclerView.ViewHolder{
        View view;
        CardView mView;
        TextView fullname,dateTime,description,tittle,description2,noOfAgrees;
        CircleImageView profileimage;
        ImageView postImage;
        ProgressBar progressBar;
        ImageButton agreeButton,commentButton,sendComment;
        int countLikes;
        String currentUserId;
        DatabaseReference likesRef;

        public PostsViewHolder(View itemView){
            super(itemView);
            mView=itemView.findViewById(R.id.cardView_pro);
            fullname=itemView.findViewById(R.id.username_card);
            dateTime=itemView.findViewById(R.id.date_time);
            description=itemView.findViewById(R.id.description_card);
            profileimage=itemView.findViewById(R.id.dp);
            postImage=itemView.findViewById(R.id.postimage);
            tittle=itemView.findViewById(R.id.tittle);
            progressBar=itemView.findViewById(R.id.progressBar);
            description2=itemView.findViewById(R.id.description2);
            likesRef=FirebaseDatabase.getInstance().getReference().child("likes");
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
            agreeButton=itemView.findViewById(R.id.agree_button);
            commentButton=itemView.findViewById(R.id.commment_button);
            noOfAgrees=itemView.findViewById(R.id.no_of_agrees);

        }

        public void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLikes=(int)dataSnapshot.child(postKey).getChildrenCount();
                        agreeButton.setImageResource(R.drawable.aggre_colored);
                        noOfAgrees.setText((Integer.toString(countLikes)+(" Agreed")));
                    }else {
                        countLikes=(int)dataSnapshot.child(postKey).getChildrenCount();
                        agreeButton.setImageResource(R.drawable.agree_black);
                        noOfAgrees.setText((Integer.toString(countLikes)+(" Agreed")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendPDFActivity() {
        Intent intent =new Intent(MainActivity.this,PdfActivity.class);
        startActivity(intent);
    }
    private void sendTextActivity() {
        Intent intent =new Intent(MainActivity.this,TextPostActivity.class);
        startActivity(intent);
    }
    private void sendPostActivity() {
        Intent intent =new Intent(MainActivity.this,PostActivity.class);
        startActivity(intent);
    }

    public void proPic(View view){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelect(MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home:
                Toast.makeText(MainActivity.this, "home",
                        Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                Toast.makeText(MainActivity.this, "profile",
                        Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_settings:
                Toast.makeText(MainActivity.this, "settings",
                        Toast.LENGTH_SHORT).show(); break;
            case R.id.nav_post:
                if(yes.equals("true")) {
                    frag();
                }
                else {
                    Toast.makeText(MainActivity.this, "Only Admin is Allowed to post",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                UserToLoginActivity();
                Toast.makeText(MainActivity.this, "logout",
                        Toast.LENGTH_SHORT).show(); break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            UserToLoginActivity();
        }
        else {
            checkUserExistence();
        }
        DisplayAllUsersPost();
    }

    private void checkUserExistence() {
        final String user_id=mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(!dataSnapshot.hasChild(user_id))
               {
                   SendUserToSetupActivity();
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {
        Intent intent =new Intent(MainActivity.this,SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void SendUserToProfileActivity() {
        Intent intent =new Intent(MainActivity.this,profileActivity.class);
        intent.putExtra("userId",currentUserId);
        startActivity(intent);

    }

    private void UserToLoginActivity() {
        Intent intent =new Intent(MainActivity.this,loginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();



    }

}
