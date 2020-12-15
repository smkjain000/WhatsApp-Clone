package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView findfriendrecyclerlist;
    private DatabaseReference usersref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_find_friends );

        usersref= FirebaseDatabase.getInstance().getReference().child( "users" );

        findfriendrecyclerlist=findViewById( R.id.findfriendsrecyclerlist );
        findfriendrecyclerlist.setLayoutManager( new LinearLayoutManager( this ) );

        mToolbar=findViewById( R.id.findfriendstoolbar );
        setSupportActionBar( mToolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowHomeEnabled( true );
        getSupportActionBar().setTitle( "Find Friends" );

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery( usersref,Contacts.class )
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                  holder.username.setText( model.getName() );
                  holder.userstatus.setText( model.getStatus() );
                Picasso.get().load( model.getImage() ).placeholder(R.drawable.profile_image  ).into( holder.profileImage );

                holder.itemView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef( position ).getKey();
                        Intent profileintent=new Intent(FindFriendsActivity.this, ProfileActivity.class);
                        profileintent.putExtra( "visit_user_id",visit_user_id );
                        startActivity( profileintent );
                    }
                } );
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate( R.layout.users_display_layout, parent,false );

                FindFriendViewHolder viewHolder=new FindFriendViewHolder( view );
                return viewHolder;

            }
        };

        findfriendrecyclerlist.setAdapter( adapter );
        adapter.startListening();

    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {

        TextView username, userstatus;
        CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull View itemView) {
            super( itemView );

            username=itemView.findViewById( R.id.usernameview );
            userstatus=itemView.findViewById( R.id.statusview );
            profileImage=itemView.findViewById( R.id.users_profile_image );
        }
    }
}