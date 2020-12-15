package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageRecieverName, messagereceiverImage;
    private TextView username, lastseen;
    private CircleImageView userimage;
    private Toolbar chatToolbar;
    private ImageButton send_message_button, send_files_button;
    private EditText input_message;
    private FirebaseAuth mAuth;
    private String messageSenderID;
    private DatabaseReference RootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView usermessageslist;

    private String saveCurrentTime, saveCurrentDate;
    private String checker="", myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );

        mAuth=FirebaseAuth.getInstance();
        messageSenderID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName=getIntent().getExtras().get("visit_user_name").toString();
        messagereceiverImage=getIntent().getExtras().get("visit_image").toString();

        Initializecontrollers();

        username.setText( messageRecieverName );
        Picasso.get().load( messagereceiverImage ).placeholder( R.drawable.profile_image ).into( userimage );


        send_message_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sendmessage();
            }
        } );

        DisplayLastSeen();

        send_files_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Word Files"
                        };

                AlertDialog.Builder builder = new AlertDialog.Builder( ChatActivity.this );
                builder.setTitle( "Select File" );

                builder.setItems( options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if(which==0)
                       {

                           checker="image";

                           Intent intent=new Intent();
                           intent.setAction( Intent.ACTION_GET_CONTENT );
                           intent.setType( "image/*" );
                           startActivityForResult( Intent.createChooser( intent,"Select Image" ),438 );

                       }
                        if(which==1)
                        {

                            checker="pdf";

                            Intent intent=new Intent();
                            intent.setAction( Intent.ACTION_GET_CONTENT );
                            intent.setType( "application/pdf" );
                            startActivityForResult( Intent.createChooser( intent,"Select PDF File" ),438 );


                        }
                        if(which==2)
                        {

                            checker="docs";

                            Intent intent=new Intent();
                            intent.setAction( Intent.ACTION_GET_CONTENT );
                            intent.setType( "application/word" );
                            startActivityForResult( Intent.createChooser( intent,"Select Word File" ),438 );


                        }
                    }
                } );

                builder.show();

            }
        } );

    }


    private void Initializecontrollers() {

        chatToolbar=findViewById(R.id.chat_toolbar );
        setSupportActionBar( chatToolbar );

        Objects.requireNonNull( getSupportActionBar() ).setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowCustomEnabled( true );

        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService( LAYOUT_INFLATER_SERVICE );
        View actionbarview=layoutInflater.inflate( R.layout.custom_chat_bar ,null);
        getSupportActionBar().setCustomView( actionbarview );

        username=findViewById( R.id.custom_profile_name );
        lastseen=findViewById( R.id.custom_user_last_seen );
        userimage=findViewById( R.id.custom_profile_image );
        send_message_button=findViewById( R.id.send_message_button);
        send_files_button=findViewById( R.id.send_files_button );
        input_message=findViewById( R.id.input_message );
        messageAdapter=new MessageAdapter( messagesList );
        usermessageslist=findViewById( R.id.private_message_list );
        linearLayoutManager=new LinearLayoutManager( this );
        usermessageslist.setLayoutManager( linearLayoutManager );
        usermessageslist.setAdapter( messageAdapter );

        loadingbar=new ProgressDialog( this );

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format( calendar.getTime() );

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format( calendar.getTime() );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingbar.setTitle( "Sending File" );
            loadingbar.setMessage( "Please Wait, We are Sending..." );
            loadingbar.setCanceledOnTouchOutside( false );
            loadingbar.show();

            fileUri=data.getData();

            if(!checker.equals( "image" ))
            {

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child( "Document Files" );

                final String messagesenderRef="Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messagereceiverRef="Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessagekeyReference = RootRef.child( "Messages" ).child( messageSenderID )
                        .child( messageReceiverID ).push();

                final String messagePushID=userMessagekeyReference.getKey();

                final StorageReference filepath=storageReference.child( messagePushID + "." + checker );

                filepath.putFile( fileUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener( new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();

                                Map messageTextBody=new HashMap() ;
                                messageTextBody.put( "message",downloadUrl );
                                messageTextBody.put( "name",fileUri.getLastPathSegment() );
                                messageTextBody.put( "type", checker);
                                messageTextBody.put( "from", messageSenderID );
                                messageTextBody.put( "to",messageReceiverID );
                                messageTextBody.put( "messageID", messagePushID);
                                messageTextBody.put( "time", saveCurrentTime );
                                messageTextBody.put( "date", saveCurrentDate );

                                Map messageBodyDetails=new HashMap();
                                messageBodyDetails.put( messagesenderRef + "/" + messagePushID,messageTextBody );
                                messageBodyDetails.put( messagereceiverRef + "/" + messagePushID,messageTextBody );

                                RootRef.updateChildren( messageBodyDetails );
                                loadingbar.dismiss();


                            }
                        } );
                    }
                } ).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                        loadingbar.dismiss();
                        Toast.makeText( ChatActivity.this,e.getMessage() , Toast.LENGTH_SHORT ).show();

                    }
                } ).addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot)
                    {

                        double p = (100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        loadingbar.setMessage( (int) p + "% uploading..." );

                    }
                } );

            }
            else if (checker.equals( "image" ))
            {

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child( "Image Files" );

                final String messagesenderRef="Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messagereceiverRef="Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessagekeyReference = RootRef.child( "Messages" ).child( messageSenderID )
                        .child( messageReceiverID ).push();

                final String messagePushID=userMessagekeyReference.getKey();

                final StorageReference filepath=storageReference.child( messagePushID + "." + "jpg" );


                uploadTask=filepath.putFile( fileUri );

                uploadTask.continueWithTask( new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                } ).addOnCompleteListener( new OnCompleteListener <Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadurl=task.getResult();
                            myUrl=downloadurl.toString();

                            Map messageTextBody=new HashMap() ;
                            messageTextBody.put( "message",myUrl );
                            messageTextBody.put( "name",fileUri.getLastPathSegment() );
                            messageTextBody.put( "type", checker);
                            messageTextBody.put( "from", messageSenderID );
                            messageTextBody.put( "to",messageReceiverID );
                            messageTextBody.put( "messageID", messagePushID);
                            messageTextBody.put( "time", saveCurrentTime );
                            messageTextBody.put( "date", saveCurrentDate );

                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put( messagesenderRef + "/" + messagePushID,messageTextBody );
                            messageBodyDetails.put( messagereceiverRef + "/" + messagePushID,messageTextBody );

                            RootRef.updateChildren( messageBodyDetails ).addOnCompleteListener( new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        loadingbar.dismiss();

                                        Toast.makeText( ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT ).show();
                                    }

                                    else
                                    {
                                        loadingbar.dismiss();
                                        Toast.makeText( ChatActivity.this, "Error", Toast.LENGTH_SHORT ).show();
                                    }

                                    input_message.setText( " " );
                                }
                            } );

                        }
                    }
                } );


            }
            else
            {
                loadingbar.dismiss();
                Toast.makeText( this, "Nothing Selected", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child( "users" ).child( messageReceiverID )
                .addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if(snapshot.child( "UserState" ).hasChild( "state" ))
                        {
                            String state = snapshot.child( "UserState" ).child( "state" ).getValue().toString();
                            String date = snapshot.child( "UserState" ).child( "date" ).getValue().toString();
                            String time = snapshot.child( "UserState" ).child( "time" ).getValue().toString();

                            if(state.equals( "Online" ))
                            {
                                lastseen.setText( "Online" );
                            }

                            else if(state.equals( "Offline" ))
                            {
                                lastseen.setText( "last seen:" + date + " " + time );
                            }
                        }

                        else
                        {
                            lastseen.setText( "Offline" );
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child( "Messages" ).child( messageSenderID ).child( messageReceiverID )
                .addChildEventListener( new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
                    {
                        Messages messages=snapshot.getValue(Messages.class);
                        messagesList.add( messages );
                        messageAdapter.notifyDataSetChanged();

                        usermessageslist.smoothScrollToPosition( usermessageslist.getAdapter().getItemCount() );

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
    }

    private void Sendmessage() {

        String messagetext=input_message.getText().toString();
        if(TextUtils.isEmpty( messagetext ))
        {
            Toast.makeText( this, "First write your message", Toast.LENGTH_SHORT ).show();
        }
        else
        {
            String messagesenderRef="Messages/" + messageSenderID + "/" + messageReceiverID;
            String messagereceiverRef="Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessagekeyReference = RootRef.child( "Messages" ).child( messageSenderID )
                    .child( messageReceiverID ).push();

            String messagePushID=userMessagekeyReference.getKey();

            Map messageTextBody=new HashMap() ;
            messageTextBody.put( "message",messagetext );
            messageTextBody.put( "type", "text");
            messageTextBody.put( "from", messageSenderID );
            messageTextBody.put( "to",messageReceiverID );
            messageTextBody.put( "messageID", messagePushID);
            messageTextBody.put( "time", saveCurrentTime );
            messageTextBody.put( "date", saveCurrentDate );

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put( messagesenderRef + "/" + messagePushID,messageTextBody );
            messageBodyDetails.put( messagereceiverRef + "/" + messagePushID,messageTextBody );

            RootRef.updateChildren( messageBodyDetails ).addOnCompleteListener( new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                   if(task.isSuccessful())
                   {
                       Toast.makeText( ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT ).show();
                   }

                   else
                   {
                       Toast.makeText( ChatActivity.this, "Error", Toast.LENGTH_SHORT ).show();
                   }

                   input_message.setText( " " );
                }
            } );

        }

    }
}