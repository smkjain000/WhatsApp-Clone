package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiveruserID;
    private CircleImageView userprofileimage;
    private TextView userProfilename, userProfilestatus;
    private Button sendmessage, declinemessage;
    private DatabaseReference UserRef,ChatRequestRef, ContactsRef;
    private String current_state,senderuserID;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_profile );

        mAuth=FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child( "users" );
        ChatRequestRef=FirebaseDatabase.getInstance().getReference().child( "Chat Requests" );
        ContactsRef=FirebaseDatabase.getInstance().getReference().child( "Contacts" );

        receiveruserID = getIntent().getExtras().get( "visit_user_id" ).toString();

        userprofileimage = findViewById( R.id.visit_profile_image );
        userProfilename = findViewById( R.id.visit_username );
        userProfilestatus = findViewById( R.id.visit_status );
        sendmessage = findViewById( R.id.send_message );
        declinemessage=findViewById( R.id.decline_message_request );

        current_state="new";
        senderuserID=mAuth.getCurrentUser().getUid();



        Retrieveuserinfo();


    }

    private void Retrieveuserinfo() {

        UserRef.child( receiveruserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if ((snapshot.exists()) && (snapshot.hasChild( "image" )))
                {

                    String userImage = snapshot.child( "image" ).getValue().toString();
                    String userStatus = snapshot.child( "status" ).getValue().toString();
                    String userName = snapshot.child( "name" ).getValue().toString();

                    Picasso.get().load( userImage ).placeholder( R.drawable.profile_image ).into( userprofileimage );

                    userProfilename.setText( userName );
                    userProfilestatus.setText( userStatus );

                    ManagechatRequests();

                }
                else
                {
                    String userStatus = snapshot.child( "status" ).getValue().toString();
                    String userName = snapshot.child( "name" ).getValue().toString();

                    userProfilename.setText( userName );
                    userProfilestatus.setText( userStatus );

                    ManagechatRequests();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    private void ManagechatRequests() {

        ChatRequestRef.child( senderuserID ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild( receiveruserID ))
                {
                    String request_type=snapshot.child( receiveruserID )
                            .child( "request_type" ).getValue().toString();

                    if(request_type.equals( "Sent" ))
                    {
                        current_state="request_sent";
                        sendmessage.setText( "Cancel Chat Request" );
                    }

                    else if(request_type.equals( "Received" ))
                    {
                        current_state="request_received";
                        sendmessage.setText( "Accept Chat Request" );

                        declinemessage.setVisibility( View.VISIBLE );
                        declinemessage.setEnabled( true );

                        declinemessage.setOnClickListener( new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        } );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        if(!senderuserID.equals( (receiveruserID) ))
        {
            sendmessage.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendmessage.setEnabled( false );

                    if(current_state.equals( "new" ))
                    {
                        SendchatRequest();
                    }

                    if(current_state.equals( "request_sent" ))
                    {
                        CancelChatRequest();
                    }

                    if(current_state.equals( "request_received" ))
                    {
                        AcceptChatRequest();
                    }

                    if(current_state.equals( "friends" ))
                    {
                        RemoveSpecificContact();
                    }
                }
            } );
        }
        else
        {
            ContactsRef.child( senderuserID ).addListenerForSingleValueEvent( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild( receiveruserID ))
                    {
                        current_state="friends";
                        sendmessage.setText( "Remove this Contact" );
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            } );
            sendmessage.setVisibility( View.INVISIBLE );
        }
    }

    private void SendchatRequest() {

        ChatRequestRef.child( senderuserID ).child( receiveruserID ).child( "request_type" ).setValue( "Sent" ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ChatRequestRef.child( receiveruserID ).child( senderuserID ).child( "request_type" ).setValue( "Received" ).addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendmessage.setEnabled( true );
                                current_state="request_sent";
                                sendmessage.setText( "Cancel Chat Request" );
                            }
                        }
                    } );
                }
            }
        } );
    }

    private void CancelChatRequest() {

        ChatRequestRef.child( senderuserID ).child( receiveruserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ChatRequestRef.child( receiveruserID ).child( senderuserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendmessage.setEnabled( true );
                                current_state="new";
                                sendmessage.setText( "Send Message" );
                            }
                        }
                    } );
                }
            }
        } );

        declinemessage.setVisibility( View.INVISIBLE );
        declinemessage.setEnabled( false );
    }

    private void AcceptChatRequest() {

        ContactsRef.child( senderuserID).child( receiveruserID ).child( "Contacts" ).setValue( "saved" )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            ContactsRef.child( receiveruserID ).child( senderuserID ).child( "Contacts" ).setValue( "saved" )
                                    .addOnCompleteListener( new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                ChatRequestRef.child( senderuserID ).child( receiveruserID ).removeValue()
                                                        .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    ChatRequestRef.child( receiveruserID ).child( senderuserID ).removeValue()
                                                                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendmessage.setEnabled( true );
                                                                                    current_state="friends";
                                                                                    sendmessage.setText( "Remove this Contact" );
                                                                                    declinemessage.setVisibility( View.INVISIBLE );
                                                                                    declinemessage.setEnabled( false );

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

    private void RemoveSpecificContact() {

        ContactsRef.child( senderuserID ).child( receiveruserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactsRef.child( receiveruserID ).child( senderuserID ).removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendmessage.setEnabled( true );
                                current_state="new";
                                sendmessage.setText( "Send Message" );
                            }
                        }
                    } );
                }
            }
        } );

        declinemessage.setVisibility( View.INVISIBLE );
        declinemessage.setEnabled( false );


    }

}