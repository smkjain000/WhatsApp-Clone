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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText registeremail, registerpassword;
    private TextView alreadyaccount;
    private Button registerbutton;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        registeremail=findViewById( R.id.registeremail );
        registerpassword=findViewById( R.id.registerpassword );
        alreadyaccount=findViewById( R.id.alreadyaccount );
        registerbutton=findViewById( R.id.registerbutton );
        loadingbar=new ProgressDialog( this );
        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        alreadyaccount.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent k=new Intent( RegisterActivity.this,LoginActivity.class );
                startActivity( k );
            }
        } );

        registerbutton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createnewAccount();
            }
        } );

    }

    private void createnewAccount() {

        String email=registeremail.getText().toString();
        String password=registerpassword.getText().toString();

        if(TextUtils.isEmpty( email ))
        {
            Toast.makeText( this,"Please Enter Email",Toast.LENGTH_SHORT ).show();
        }
        if(TextUtils.isEmpty( password ))
        {
            Toast.makeText( this,"Please enter password",Toast.LENGTH_SHORT ).show();
        }
        else
        {
            loadingbar.setTitle( "Creating New Account" );
            loadingbar.setMessage( "Please Wait" );
            loadingbar.setCanceledOnTouchOutside( true );
            loadingbar.show();
            mAuth.createUserWithEmailAndPassword( email,password )
                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                String CurrentuserID=mAuth.getCurrentUser().getUid();
                                RootRef.child( "users" ).child( CurrentuserID ).setValue( "" );
                                SendusertoMainActivity();
                                loadingbar.dismiss();

                                Toast.makeText(RegisterActivity.this,"Account Created Successfully",Toast.LENGTH_SHORT ).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText( RegisterActivity.this,"Error:"+message,Toast.LENGTH_SHORT ).show();
                                loadingbar.dismiss();
                            }

                        }

                        private void SendusertoMainActivity() {
                            Intent loginintent=new Intent( RegisterActivity.this,MainActivity.class );
                            loginintent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                            startActivity( loginintent );
                            finish();
                        }
                    } );

        }
    }


}
