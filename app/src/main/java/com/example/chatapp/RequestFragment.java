package com.example.chatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private  View RequestFragmentview;
    private RecyclerView myrequestlist;
    private DatabaseReference chatRequestsRef , usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentuserID;




    public RequestFragment() {
        // Required empty public constructor


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        chatRequestsRef= FirebaseDatabase.getInstance().getReference().child( "Chat Requests" );

        usersRef=FirebaseDatabase.getInstance().getReference().child( "users" );

        contactsRef=FirebaseDatabase.getInstance().getReference().child( "Contacts" );

        mAuth=FirebaseAuth.getInstance();
        currentuserID=mAuth.getCurrentUser().getUid();
        RequestFragmentview =  inflater.inflate( R.layout.fragment_request, container, false );
        myrequestlist=RequestFragmentview.findViewById( R.id.request_list );
        myrequestlist.setLayoutManager( new LinearLayoutManager( getContext() ) );

        return RequestFragmentview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts>options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestsRef.child( currentuserID ),Contacts.class )
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsviewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestsviewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsviewHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById( R.id.request_accept_btn ).setVisibility( View.VISIBLE );
                holder.itemView.findViewById( R.id.request_cancel).setVisibility( View.VISIBLE );

                final String list_user_id=getRef( position ).getKey();
                DatabaseReference getTypeRef=getRef( position ).child( "request_type" ).getRef();

                getTypeRef.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String type=snapshot.getValue().toString();

                            if(type.equals( "Received" ))
                            {
                                usersRef.child( list_user_id ).addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild( "image" ))
                                        {
                                            final String requestprofileImage=snapshot.child( "image" ).getValue().toString();
                                            Picasso.get().load( requestprofileImage ).into( holder.profileImage );
                                        }

                                        final String requestuserName=snapshot.child( "name" ).getValue().toString();
                                        final String requestuserstatus=snapshot.child( "status" ).getValue().toString();

                                        holder.userName.setText( requestuserName + "  Wants to Connect with You");
                                        holder.userstatus.setText(requestuserstatus );


                                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Accept",
                                                                "Cancel"
                                                        };

                                                AlertDialog.Builder builder=new AlertDialog.Builder( getContext() );

                                                builder.setTitle(requestuserName +  "  Chat Request" );
                                                builder.setItems( options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                       if(which==0)
                                                       {
                                                           contactsRef.child( currentuserID ).child( list_user_id ).child( "Contacts" ).setValue( "saved" )
                                                                   .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if(task.isSuccessful())
                                                                           {
                                                                               contactsRef.child( list_user_id ).child( currentuserID )
                                                                                       .child( "Contacts" ).setValue( "saved" )
                                                                                       .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               if(task.isSuccessful())
                                                                                               {
                                                                                                   chatRequestsRef.child( currentuserID ).child( list_user_id )
                                                                                                           .removeValue()
                                                                                                           .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                                               @Override
                                                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                                                   if(task.isSuccessful())
                                                                                                                   {
                                                                                                                       chatRequestsRef.child( list_user_id ).child( currentuserID )
                                                                                                                               .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                                                           @Override
                                                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                                                               if(task.isSuccessful())
                                                                                                                               {
                                                                                                                                   Toast.makeText( getContext(), "Contact Saved", Toast.LENGTH_SHORT ).show();
                                                                                                                               }
                                                                                                                           }
                                                                                                                       } );
                                                                                                                   }
                                                                                                               }
                                                                                                           } );
                                                                                               }
                                                                                           }
                                                                                       } );
                                                                           }
                                                                       }
                                                                   } );
                                                       }

                                                       if(which==1)
                                                       {
                                                           chatRequestsRef.child( currentuserID ).child( list_user_id )
                                                                   .removeValue()
                                                                   .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if(task.isSuccessful())
                                                                           {
                                                                               chatRequestsRef.child( list_user_id ).child( currentuserID )
                                                                                       .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                   @Override
                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                       if(task.isSuccessful())
                                                                                       {
                                                                                           Toast.makeText( getContext(), "Contact Deleted", Toast.LENGTH_SHORT ).show();
                                                                                       }
                                                                                   }
                                                                               } );
                                                                           }
                                                                       }
                                                                   } );

                                                       }
                                                    }
                                                } );

                                                builder.show();
                                            }
                                        } );
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                } );
                            }

                            else if (type.equals( "Sent" ))
                            {
                                Button request_sent_button = holder.itemView.findViewById( R.id.request_accept_btn );
                                request_sent_button.setText( "Request Sent" );

                                holder.itemView.findViewById( R.id.request_cancel ).setVisibility( View.INVISIBLE );

                                usersRef.child( list_user_id ).addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild( "image" ))
                                        {
                                            final String requestprofileImage=snapshot.child( "image" ).getValue().toString();
                                            Picasso.get().load( requestprofileImage ).into( holder.profileImage );
                                        }

                                        final String requestuserName=snapshot.child( "name" ).getValue().toString();
                                        final String requestuserstatus=snapshot.child( "status" ).getValue().toString();

                                        holder.userName.setText(requestuserName);
                                        holder.userstatus.setText("You have sent request to" + "  " + requestuserName );


                                        holder.itemView.setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[]=new CharSequence[]
                                                        {
                                                                "Cancel Chat Request"
                                                        };

                                                AlertDialog.Builder builder=new AlertDialog.Builder( getContext() );

                                                builder.setTitle("Already Sent Request");
                                                builder.setItems( options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        if(which==0)
                                                        {
                                                            chatRequestsRef.child( currentuserID ).child( list_user_id )
                                                                    .removeValue()
                                                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                chatRequestsRef.child( list_user_id ).child( currentuserID )
                                                                                        .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            Toast.makeText( getContext(), "You have Cancelled the chat request", Toast.LENGTH_SHORT ).show();
                                                                                        }
                                                                                    }
                                                                                } );
                                                                            }
                                                                        }
                                                                    } );

                                                        }
                                                    }
                                                } );

                                                builder.show();
                                            }
                                        } );
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                } );

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
            public RequestsviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from( parent.getContext() ).inflate( R.layout.users_display_layout,parent,false );

                RequestsviewHolder holder=new RequestsviewHolder( view );
                return holder;
            }
        };

        myrequestlist.setAdapter( adapter );
        adapter.startListening();
    }

    public static class RequestsviewHolder extends RecyclerView.ViewHolder
    {

        TextView userName, userstatus;
        CircleImageView profileImage;
        Button Accept,Cancel;
        public RequestsviewHolder(@NonNull View itemView) {
            super( itemView );

            userName=itemView.findViewById( R.id.usernameview );
            userstatus=itemView.findViewById( R.id.statusview );
            profileImage=itemView.findViewById( R.id.users_profile_image );
            Accept=itemView.findViewById( R.id.request_accept_btn );
            Cancel=itemView.findViewById( R.id.request_cancel );
        }
    }
}