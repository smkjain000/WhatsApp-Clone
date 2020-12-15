package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SimpleTimeZone;
import java.util.regex.Pattern;

public class GroupchatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ImageButton sendbtn;
    private EditText usermessageinput;
    private ScrollView myscrollview;
    private TextView groupmessage;
    private String currentGroupname , currentuserId, currentusername, currentdate, currenttime;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, GroupNameRef, GroupMessagekeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_groupchat );

        currentGroupname=getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupchatActivity.this,currentGroupname, Toast.LENGTH_SHORT ).show();

        mAuth=FirebaseAuth.getInstance();
        currentuserId=mAuth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child( "users" );
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child( "Groups" ).child( currentGroupname );


        Initializefields();

        getuserInfo();

        sendbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfotodatabase();
                usermessageinput.setText( " " );
                myscrollview.fullScroll( ScrollView.FOCUS_DOWN );

            }
        } );
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener( new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatdate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();

            groupmessage.append( chatName + " :\n\n"+ chatMessage + "\n\n" + chatTime + "    " + chatdate + "\n\n\n" );
            myscrollview.fullScroll( ScrollView.FOCUS_DOWN );
        }
    }

    private void SaveMessageInfotodatabase() {
        String message= usermessageinput.getText().toString();
        String messagekey=GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText( this, "Write Something !!! ", Toast.LENGTH_SHORT ).show();
        }
        else
        {
            Calendar ccalForDate=Calendar.getInstance();
            SimpleDateFormat CurrentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
            currentdate=CurrentDateFormat.format( ccalForDate.getTime() );

            Calendar ccalFortime=Calendar.getInstance();
            SimpleDateFormat CurrentTimeFormat=new SimpleDateFormat("hh:mm a");
            currenttime=CurrentTimeFormat.format( ccalFortime.getTime() );

            HashMap<String,Object> groupMessageKey=new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);
            GroupMessagekeyRef=GroupNameRef.child(messagekey) ;

            HashMap<String, Object>messageInfoMap =new HashMap<>();
            messageInfoMap.put( "name",currentusername );
            messageInfoMap.put( "message",message );
            messageInfoMap.put( "date",currentdate );
            messageInfoMap.put( "time",currenttime );

            GroupMessagekeyRef.updateChildren( messageInfoMap );
        }
    }

    private void getuserInfo() {
        UserRef.child( currentuserId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentusername=dataSnapshot.child( "name" ).getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    private void Initializefields() {
        mtoolbar=findViewById( R.id.group_chat_bar_layout );
        setSupportActionBar( mtoolbar );
        getSupportActionBar().setTitle( currentGroupname );

        sendbtn=(ImageButton)findViewById( R.id.sendbtn );
        groupmessage=findViewById( R.id.group_message );
        usermessageinput=findViewById( R.id.usermessageinput );
        myscrollview=findViewById( R.id.my_scroll_view );



    }
}