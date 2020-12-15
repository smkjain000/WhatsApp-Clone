package com.example.chatapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView mycontactlist;
    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentuserID;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate( R.layout.fragment_contacts, container, false );
        mycontactlist=ContactsView.findViewById( R.id.contacts_list );
        mycontactlist.setLayoutManager( new LinearLayoutManager( getContext() ) );

        mAuth=FirebaseAuth.getInstance();
        currentuserID=mAuth.getCurrentUser().getUid();

        contactsRef= FirebaseDatabase.getInstance().getReference().child( "Contacts" ).child( currentuserID );
        usersRef=FirebaseDatabase.getInstance().getReference().child( "users" );

        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class  )
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {

                String UserIDs=getRef( position ).getKey();
                usersRef.child( UserIDs ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                       if(snapshot.exists())
                       {

                           if(snapshot.child( "UserState" ).hasChild( "state" ))
                           {
                               String state = snapshot.child( "UserState" ).child( "state" ).getValue().toString();
                               String date = snapshot.child( "UserState" ).child( "date" ).getValue().toString();
                               String time = snapshot.child( "UserState" ).child( "time" ).getValue().toString();

                               if(state.equals( "Online" ))
                               {
                                   holder.onlineicon.setVisibility( View.VISIBLE );
                               }

                               else if(state.equals( "Offline" ))
                               {
                                   holder.onlineicon.setVisibility( View.INVISIBLE );
                               }
                           }

                           else {
                               holder.onlineicon.setVisibility( View.INVISIBLE );
                           }

                           if(snapshot.hasChild( "image" ))
                           {
                               String UserImage=snapshot.child( "image" ).getValue().toString();
                               String profileName=snapshot.child( "name" ).getValue().toString();
                               String profilestatus=snapshot.child( "status" ).getValue().toString();

                               holder.name.setText( profileName );
                               holder.status.setText( profilestatus );
                               Picasso.get().load( UserImage ).placeholder( R.drawable.profile_image ).into( holder.profileImage );
                           }

                           else {

                               String profileName=snapshot.child( "name" ).getValue().toString();
                               String profilestatus=snapshot.child( "status" ).getValue().toString();

                               holder.name.setText( profileName );
                               holder.status.setText( profilestatus );

                           }
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from( parent.getContext() ).inflate( R.layout.users_display_layout,parent,false );
                ContactsViewHolder viewHolder=new ContactsViewHolder( view );

                return viewHolder;
            }
        };

        mycontactlist.setAdapter( adapter );
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {

        TextView name,status;
        CircleImageView profileImage;
        ImageView onlineicon;
        public ContactsViewHolder(@NonNull View itemView) {
            super( itemView );

            name=itemView.findViewById( R.id.usernameview );
            status=itemView.findViewById( R.id.statusview );
            profileImage=itemView.findViewById( R.id.users_profile_image );
            onlineicon=itemView.findViewById( R.id.user_online_status );
        }
    }
}
