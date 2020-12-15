package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendverificationbtn, verifybtn;
    private EditText phonenoinput, verificationinput;
    private ProgressDialog loadingbar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private  String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_phone_login );

        mAuth=FirebaseAuth.getInstance();

        sendverificationbtn=findViewById( R.id.sendverificationbtn );
        verifybtn=findViewById( R.id.verifybtn );
        phonenoinput=findViewById( R.id.phonenoinput );
        verificationinput=findViewById( R.id.verificationinput );
        loadingbar=new ProgressDialog( this );

        sendverificationbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber=phonenoinput.getText().toString();
                if(TextUtils.isEmpty( phoneNumber ))
                {
                    Toast.makeText( PhoneLoginActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    loadingbar.setTitle( "Phone Verification" );
                    loadingbar.setMessage( "Please wait" );
                    loadingbar.setCanceledOnTouchOutside( false );
                    loadingbar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber( phoneNumber,60,TimeUnit.SECONDS,PhoneLoginActivity.this,callbacks );
                }
            }
        } );

        verifybtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendverificationbtn.setVisibility( View.INVISIBLE );
                phonenoinput.setVisibility( View.INVISIBLE );
                String verificationcode=verificationinput.getText().toString();

                if(TextUtils.isEmpty( verificationcode ))
                {
                    Toast.makeText( PhoneLoginActivity.this, "Please Enter code", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    loadingbar.setTitle( "Phone Verification" );
                    loadingbar.setMessage( "Please wait" );
                    loadingbar.setCanceledOnTouchOutside( false );
                    loadingbar.show();

                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential( mVerificationId,verificationcode );
                    signInwithPhoneAuthcredential( credential );
                }
            }
        } );
        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInwithPhoneAuthcredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText( PhoneLoginActivity.this, "Invalid Phone no", Toast.LENGTH_SHORT ).show();
                loadingbar.dismiss();

                sendverificationbtn.setVisibility( View.VISIBLE );
                phonenoinput.setVisibility( View.VISIBLE );

                verifybtn.setVisibility( View.INVISIBLE );
                verificationinput.setVisibility( View.INVISIBLE );

            }
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token)
            {
                mVerificationId=verificationId;
                mResendToken=token;

                Toast.makeText( PhoneLoginActivity.this, "Code has been Sent", Toast.LENGTH_SHORT ).show();

                sendverificationbtn.setVisibility( View.INVISIBLE );
                phonenoinput.setVisibility( View.INVISIBLE );

                verifybtn.setVisibility( View.VISIBLE );
                verificationinput.setVisibility( View.VISIBLE );
            }
        };

    }

    private void signInwithPhoneAuthcredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential( phoneAuthCredential).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    loadingbar.dismiss();
                    Toast.makeText( PhoneLoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT ).show();
                    Intent mainintent=new Intent(PhoneLoginActivity.this, MainActivity.class);
                    startActivity( mainintent );
                    finish();
                }
                else
                {
                    String message=task.getException().toString();
                    Toast.makeText( PhoneLoginActivity.this, "Error" + message, Toast.LENGTH_SHORT ).show();
                }
            }
        } );

    }

    @Override
    protected void onStart() {

        sendverificationbtn.setVisibility( View.VISIBLE );
        phonenoinput.setVisibility( View.VISIBLE );

        verifybtn.setVisibility( View.INVISIBLE );
        verificationinput.setVisibility( View.INVISIBLE );
        super.onStart();
    }
}