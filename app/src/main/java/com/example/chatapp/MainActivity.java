package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myviewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentuserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mAuth=FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar );
        setSupportActionBar( mToolbar );
        getSupportActionBar().setTitle( "WhatsApp" );

        myviewPager = findViewById( R.id.main_tabs_pager );
        mytabsAccessorAdapter = new TabsAccessorAdapter( getSupportFragmentManager() );
        myviewPager.setAdapter( mytabsAccessorAdapter );

        myTabLayout = findViewById( R.id.main_tabs );
        myTabLayout.setupWithViewPager( myviewPager );

        RootRef= FirebaseDatabase.getInstance().getReference();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=mAuth.getCurrentUser();

        if(currentuser==null)
        {
            SendusertologinActivity();
        }
        else
        {
            updateuserstatus("Online");

            verifyuserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentuser=mAuth.getCurrentUser();

        if(currentuser != null)
        {
            updateuserstatus("Offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentuser=mAuth.getCurrentUser();
        if(currentuser != null)
        {
            updateuserstatus("Offline");
        }
    }

    private void verifyuserExistence() {
        String currentuserID=mAuth.getCurrentUser().getUid();
        RootRef.child("users").child( currentuserID).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child( "name" ).exists()))
                {
                    Toast.makeText( MainActivity.this, "Welcome", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    SendusertoSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    private void SendusertologinActivity() {

        Intent loginIntent = new Intent( MainActivity.this,LoginActivity.class );
        loginIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity( loginIntent );
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu( menu );

         getMenuInflater().inflate( R.menu.options_menu,menu );
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected( item );

        if (item.getItemId() == R.id.logout)
        {
            updateuserstatus("Offline");

            mAuth.signOut();
            SendusertologinActivity();
        }

        if(item.getItemId()==R.id.settings){
            SendusertoSettingsActivity();
        }

        if(item.getItemId()==R.id.creategroup){
            RequestNewGroup();
        }

        if(item.getItemId()==R.id.find_friends){
            SendusertoFindFriendsActivity();
        }
        return  true;
    }


    private void RequestNewGroup() {

        AlertDialog.Builder builder=new AlertDialog.Builder( MainActivity.this, R.style.AlertDialog);
        builder.setTitle( "Enter Group Name" );
        final EditText groupnamefield =new EditText (MainActivity.this) ;
        groupnamefield.setHint( "Eg Android Discussion" );
        builder.setView( groupnamefield );
        builder.setPositiveButton( "Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupname=groupnamefield.getText().toString();
                if(TextUtils.isEmpty( groupname ))
                {
                    Toast.makeText( MainActivity.this, "Please write Group Name", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    createNewGroup(groupname);
                }
            }
        } );

        builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        } );

         builder.show();
    }

    private void createNewGroup(final String groupname) {

        RootRef.child( "Groups" ).child( groupname ).setValue( "" ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText( MainActivity.this,groupname + " " + "is Created Successfully", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
    }

    private void SendusertoSettingsActivity() {

        Intent i=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity( i );
    }

    private void SendusertoFindFriendsActivity() {

        Intent friends = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity( friends );
    }

    private void updateuserstatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format( calendar.getTime() );

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format( calendar.getTime() );

        HashMap<String, Object> onlinestateMap = new HashMap<>();
        onlinestateMap.put( "time",saveCurrentTime );
        onlinestateMap.put( "date", saveCurrentDate );
        onlinestateMap.put( "state", state );

        currentuserID=mAuth.getCurrentUser().getUid();

        RootRef.child( "users" ).child( currentuserID ).child( "UserState" )
                .updateChildren( onlinestateMap );

    }
}
