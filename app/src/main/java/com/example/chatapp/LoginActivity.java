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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private Button loginbutton, loginphone;
    private EditText passwordtext, emailtext;
    private TextView forgetpassword, neednew;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_login );

        mAuth=FirebaseAuth.getInstance();

        IntializeFields();

        neednew.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent j = new Intent( LoginActivity.this, RegisterActivity.class );
                startActivity( j );
            }
        } );

        loginbutton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Allowusertologin();
            }
        } );

        loginphone.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneintent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity( phoneintent );
            }
        } );
    }

    private void Allowusertologin() {

        String email=emailtext.getText().toString();
        String password=passwordtext.getText().toString();

        if(TextUtils.isEmpty( email ))
        {
            Toast.makeText( LoginActivity.this,"Please enter email",Toast.LENGTH_SHORT ).show();
        }
        if(TextUtils.isEmpty( password ))
        {
            Toast.makeText( LoginActivity.this,"Please enter password",Toast.LENGTH_SHORT ).show();
        }
        else
        {
            loadingbar.setTitle( "Sign In" );
            loadingbar.setMessage( "Please wait" );
            loadingbar.setCanceledOnTouchOutside( true );
            loadingbar.show();
            mAuth.signInWithEmailAndPassword( email,password )
                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                SendusertoMainActivity();
                                loadingbar.dismiss();
                                Toast.makeText( LoginActivity.this,"Login Successfully",Toast.LENGTH_SHORT ).show();
                            }
                            else
                            {
                                String message=task.getException().toString();
                                Toast.makeText( LoginActivity.this,"Error:" + message,Toast.LENGTH_SHORT ).show();
                                loadingbar.dismiss();
                            }

                        }
                    } );
        }
    }

    private void IntializeFields() {

        loginbutton = findViewById( R.id.loginbutton );
        loginphone = findViewById( R.id.loginphone );
        passwordtext = findViewById( R.id.passwordtext );
        emailtext = findViewById( R.id.emailtext );
        forgetpassword = findViewById( R.id.forgetpassword );
        neednew = findViewById( R.id.neednew );
        loadingbar=new ProgressDialog( this );

    }



    private void SendusertoMainActivity() {

        Intent i = new Intent( LoginActivity.this,MainActivity.class );
        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( i );
        finish();
    }


}
