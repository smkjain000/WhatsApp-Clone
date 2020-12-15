package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class chatsFragment extends Fragment {

    private View Privatechatsview;
    private RecyclerView chatslist;
    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentuserID;



    public chatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Privatechatsview = inflater.inflate( R.layout.fragment_chats, container, false );

        mAuth=FirebaseAuth.getInstance();
        currentuserID=mAuth.getCurrentUser().getUid();
        chatsRef= FirebaseDatabase.getInstance().getReference().child( "Contacts" ).child( currentuserID );
        usersRef=FirebaseDatabase.getInstance().getReference().child( "users" );

        chatslist=(RecyclerView)Privatechatsview.findViewById( R.id.chats_list );
        chatslist.setLayoutManager( new LinearLayoutManager( getContext() ) );

        return Privatechatsview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery( chatsRef,Contacts.class )
                .build();

        FirebaseRecyclerAdapter<Contacts,chatsviewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, chatsviewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsviewHolder holder, int position, @NonNull Contacts model) {

                final String userIDs=getRef( position ).getKey();

                final String[] retimage = {"default_image"};
                usersRef.child( userIDs ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists())
                        {
                            if(snapshot.hasChild( "image" ))
                            {
                                retimage[0] =snapshot.child( "image" ).getValue().toString();
                                Picasso.get().load( retimage[0] ).into( holder.profileImage );
                            }

                            final String retName=snapshot.child( "name" ).getValue().toString();
                            final String retStatus=snapshot.child( "status" ).getValue().toString();

                            holder.name.setText( retName );

                            if(snapshot.child( "UserState" ).hasChild( "state" ))
                            {
                                String state = snapshot.child( "UserState" ).child( "state" ).getValue().toString();
                                String date = snapshot.child( "UserState" ).child( "date" ).getValue().toString();
                                String time = snapshot.child( "UserState" ).child( "time" ).getValue().toString();

                                if(state.equals( "Online" ))
                                {
                                    holder.status.setText( "Online" );
                                }

                                else if(state.equals( "Offline" ))
                                {
                                    holder.status.setText( "last seen:" + date + " " + time );
                                }
                            }

                            else
                                {
                                   holder.status.setText( "Offline" );
                                }


                                holder.itemView.setOnClickListener( new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatintent=new Intent(getContext() ,ChatActivity.class);
                                    chatintent.putExtra( "visit_user_id",userIDs );
                                    chatintent.putExtra( "visit_user_name",retName );
                                    chatintent.putExtra( "visit_image", retimage[0] );
                                    startActivity( chatintent );
                                }
                            } );
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );

            }

            @NonNull
            @Override
            public chatsviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.users_display_layout,parent,false );
                return new chatsviewHolder( view );
            }
        };
        chatslist.setAdapter( adapter );
        adapter.startListening();
    }

    public static class chatsviewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView status, name;

        public chatsviewHolder(@NonNull View itemView) {
            super( itemView );

            profileImage=itemView.findViewById( R.id.users_profile_image );
            status=itemView.findViewById( R.id.statusview );
            name=itemView.findViewById( R.id.usernameview );
        }
    }
}
