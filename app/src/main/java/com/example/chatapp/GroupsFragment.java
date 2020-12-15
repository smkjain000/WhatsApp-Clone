package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupfragmentview;
    private ListView list_View;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups=new ArrayList<>();
    private DatabaseReference GroupRef;


    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupfragmentview= inflater.inflate( R.layout.fragment_groups, container, false );
        GroupRef= FirebaseDatabase.getInstance().getReference().child( "Groups" );
        
        Initializefields();
        RetrieveandDisplayGroups();

        list_View.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupname=parent.getItemAtPosition( position ).toString();
                Intent groupchatintent=new Intent(getContext(),GroupchatActivity.class);
                groupchatintent.putExtra( "groupName",currentGroupname );
                startActivity( groupchatintent );
            }
        } );
        return groupfragmentview;
    }

    private void RetrieveandDisplayGroups() {
        GroupRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set=new HashSet<>();
                Iterator iterator=dataSnapshot.getChildren().iterator();

                while ((iterator.hasNext()))
                {
                    set.add( ((DataSnapshot)iterator.next()).getKey() );
                }
                list_of_groups.clear();
                list_of_groups.addAll( set );

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        } );
    }

    private void Initializefields() {

        list_View=groupfragmentview.findViewById( R.id.list_view );
        arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,list_of_groups);
        list_View.setAdapter( arrayAdapter );
    }

}
