package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update;
    private EditText username, status;
    private CircleImageView profileimage;
    private Toolbar settingstoolbar;
    private String CurrentuserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPick = 1;
    private StorageReference userprofileImageRef;

    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_settings );

        mAuth = FirebaseAuth.getInstance();
        CurrentuserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        userprofileImageRef = FirebaseStorage.getInstance().getReference().child( "Profile Image" );

        update = findViewById( R.id.update );
        username = findViewById( R.id.username );
        status = findViewById( R.id.status );
        profileimage = findViewById( R.id.profile_image );
        loadingbar=new ProgressDialog( this );
        settingstoolbar=findViewById( R.id.settings_toolbar );
        setSupportActionBar( settingstoolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setDisplayShowCustomEnabled( true );
        getSupportActionBar().setTitle( "Account Settings" );


        update.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        } );

        RetrieveuserInfo();

        profileimage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction( Intent.ACTION_GET_CONTENT );
                galleryIntent.setType( "image/*" );
                startActivityForResult( galleryIntent, GalleryPick );
            }
        } );
    }


    private void updateSettings() {
        String setusername = username.getText().toString();
        String setstatus = status.getText().toString();

        if (TextUtils.isEmpty( setusername )) {
            Toast.makeText( SettingsActivity.this, "Enter your name", Toast.LENGTH_SHORT ).show();
        }
        if (TextUtils.isEmpty( setstatus )) {
            Toast.makeText( SettingsActivity.this, "Enter Status", Toast.LENGTH_SHORT ).show();
        } else {
            HashMap<String, Object> profilemap = new HashMap<>();
            profilemap.put( "Uid", CurrentuserId );
            profilemap.put( "name", setusername );
            profilemap.put( "status", setstatus );

            RootRef.child( "users" ).child( CurrentuserId ).updateChildren( profilemap ).addOnCompleteListener( new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SendusertoMainActivity();
                        Toast.makeText( SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT ).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText( SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();
                    }
                }
            } );

        }
    }


    private void RetrieveuserInfo() {
        RootRef.child( "users" ).child( CurrentuserId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild( "name" ) && (dataSnapshot.hasChild( "image" )))) {
                    String retrieveUserName = dataSnapshot.child( "name" ).getValue().toString();
                    String retrieveUserstatus = dataSnapshot.child( "status" ).getValue().toString();
                    String retrieveprofileImage = dataSnapshot.child( "image" ).getValue().toString();

                    username.setText( retrieveUserName );
                    status.setText( retrieveUserstatus );

                    Picasso.get().load( retrieveprofileImage ).into( profileimage );
                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild( "name" ))) {
                    String retrieveUserName = dataSnapshot.child( "name" ).getValue().toString();
                    String retrieveUserstatus = dataSnapshot.child( "status" ).getValue().toString();

                    Picasso.get().load( R.drawable.profile_image ).into( profileimage );

                    username.setText( retrieveUserName );
                    status.setText( retrieveUserstatus );
                } else {
                    Toast.makeText( SettingsActivity.this, "Please update Profile", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );

    }

    private void SendusertoMainActivity() {
        Intent main = new Intent( SettingsActivity.this, MainActivity.class );
        main.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( main );
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri Imageuri = data.getData();
            CropImage.activity()
                    .setGuidelines( CropImageView.Guidelines.ON )
                    .setAspectRatio( 1, 1 )
                    .start( this );


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult( data );

            if (resultCode == RESULT_OK) {

                loadingbar.setTitle( "Set Profile Image" );
                loadingbar.setMessage( "Please Wait, Your profile Image is Updating..." );
                loadingbar.setCanceledOnTouchOutside( false );
                loadingbar.show();

                Uri resulturi = result.getUri();
                StorageReference filepath = userprofileImageRef.child( CurrentuserId + ".jpg" );
                filepath.putFile( resulturi )
                        .addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener( new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        RootRef.child( "users" ).child( CurrentuserId ).child( "image" )
                                                .setValue( downloadUrl )
                                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText( SettingsActivity.this, "Image saved ", Toast.LENGTH_SHORT ).show();

                                                            loadingbar.dismiss();
                                                        }
                                                        else {
                                                            String message = task.getException().toString();
                                                            Toast.makeText( SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();

                                                            loadingbar.dismiss();
                                                        }
                                                    }
                                                } );
                                    }
                                } );
                            }
                        } );
            }
        }
    }
}






