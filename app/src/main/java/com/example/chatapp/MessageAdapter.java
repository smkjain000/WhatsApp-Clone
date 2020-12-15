package com.example.chatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageviewHolder>
{
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter (List<Messages> userMessageList)
    {
        this.userMessageList=userMessageList;
    }


    public class MessageviewHolder extends RecyclerView.ViewHolder
    {

        public TextView sendermessageText, receivermessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messagesenderpicture,messagereceiverpicture;

        public MessageviewHolder(@NonNull View itemView) {
            super( itemView );

            sendermessageText=itemView.findViewById( R.id.sender_message_text );
            receivermessageText=itemView.findViewById( R.id.receiver_message_text );
            receiverProfileImage=itemView.findViewById( R.id.message_profile_image );
            messagesenderpicture=itemView.findViewById( R.id.message_sender_image_view );
            messagereceiverpicture=itemView.findViewById( R.id.message_receiver_image_view );
        }
    }


    @NonNull
    @Override
    public MessageviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from( parent.getContext() )
                .inflate( R.layout.custom_messages_layout , parent, false);

        mAuth=FirebaseAuth.getInstance();

        return new MessageviewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageviewHolder holder, final int position) {

        String messagesenderID=mAuth.getCurrentUser().getUid();
        Messages messages=userMessageList.get( position );

        String fromuserID = messages.getFrom();
        String fromMessagetype = messages.getType();

        usersRef=FirebaseDatabase.getInstance().getReference().child( "users" ).child( fromuserID );

        usersRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.hasChild( "image" ))
                {
                    String retrieveimage=snapshot.child("image").getValue().toString();
                    Picasso.get().load( retrieveimage ).placeholder( R.drawable.profile_image ).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );

        holder.receivermessageText.setVisibility( View.GONE );
        holder.receiverProfileImage.setVisibility( View.GONE);
        holder.sendermessageText.setVisibility( View.GONE );
        holder.messagesenderpicture.setVisibility( View.GONE );
        holder.messagereceiverpicture.setVisibility( View.GONE );

        if(fromMessagetype.equals( "text" ))
        {

               if(fromuserID.equals( messagesenderID ))
               {
                   holder.sendermessageText.setVisibility( View.VISIBLE );
                   holder.sendermessageText.setBackgroundResource( R.drawable.sender_messages_layout );
                   holder.sendermessageText.setText( messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate() );
               }

               else
               {
                   holder.receiverProfileImage.setVisibility( View.VISIBLE );
                   holder.receivermessageText.setVisibility( View.VISIBLE );

                   holder.receivermessageText.setBackgroundResource( R.drawable.receiver_messages_layout);
                   holder.receivermessageText.setText( messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate() );
               }
        }

        else if (fromMessagetype.equals( "image" ))
        {
            if(fromuserID.equals( messagesenderID ))
            {
                holder.messagesenderpicture.setVisibility( View.VISIBLE );

                Picasso.get().load( messages.getMessage() ).into( holder.messagesenderpicture );
            }

            else
            {
                holder.receiverProfileImage.setVisibility( View.VISIBLE );
                holder.messagereceiverpicture.setVisibility( View.VISIBLE );

                Picasso.get().load( messages.getMessage() ).into( holder.messagereceiverpicture );

            }
        }

        else if (fromMessagetype.equals( "pdf" ) || (fromMessagetype.equals( "docs" )))
        {

            if(fromuserID.equals( messagesenderID ))
            {
                holder.messagesenderpicture.setVisibility( View.VISIBLE );

                Picasso.get().load( "https://firebasestorage.googleapis.com/v0/b/chat-app-1006d.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=36c5315f-2441-4177-b3bd-9390575e1d46" )
                        .into( holder.messagesenderpicture );

            }
            else
            {
                holder.receiverProfileImage.setVisibility( View.VISIBLE );
                holder.messagereceiverpicture.setVisibility( View.VISIBLE );

                Picasso.get().load( "https://firebasestorage.googleapis.com/v0/b/chat-app-1006d.appspot.com/o/Image%20Files%2Ffile.png?alt=media&token=36c5315f-2441-4177-b3bd-9390575e1d46" )
                        .into( holder.messagereceiverpicture );


            }


        }

        if(fromuserID.equals( messagesenderID ))
        {
            holder.itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessageList.get( position ).getType().equals("pdf") || userMessageList.get( position ).getType().equals("docs") )
                    {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and view this Document",
                                        "Delete For me",
                                        "Delete For Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder( holder.itemView.getContext() );
                        builder.setTitle( "Delete Message" );

                        builder.setItems( options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {

                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse( userMessageList.get( position ).getMessage() ) );

                                    holder.itemView.getContext().startActivity( intent );



                                }
                                else if (which==1)
                                {

                                    deletesentmessages( position, holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }
                                else if (which==2)
                                {

                                    deleteforeveryonemessages( position,holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }
                            }
                        } );

                        builder.show();


                    }

                   else if(userMessageList.get( position ).getType().equals("text"))
                    {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Delete For Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder( holder.itemView.getContext() );
                        builder.setTitle( "Delete Message" );

                        builder.setItems( options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {

                                    deletesentmessages( position, holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }

                                else if (which==1)
                                {

                                    deleteforeveryonemessages( position,holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }

                            }
                        } );

                        builder.show();


                    }

                   else if(userMessageList.get( position ).getType().equals("image" ))
                    {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "View this Image",
                                        "Delete For me",
                                        "Delete For Everyone",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder( holder.itemView.getContext() );
                        builder.setTitle( "Delete Message" );

                        builder.setItems( options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {

                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra( "url", userMessageList.get( position ).getMessage() );
                                    holder.itemView.getContext().startActivity( intent );

                                }
                                else if (which==1)
                                {

                                    deletesentmessages( position, holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }

                                else if (which==2)
                                {

                                    deleteforeveryonemessages( position,holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }
                            }
                        } );

                        builder.show();


                    }
                }
            } );


        }

        else
        {

            holder.itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessageList.get( position ).getType().equals("pdf") || userMessageList.get( position ).getType().equals("docs") )
                    {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Download and view this Document",
                                        "Delete",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder( holder.itemView.getContext() );
                        builder.setTitle( "Delete Message" );

                        builder.setItems( options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {

                                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse( userMessageList.get( position ).getMessage() ) );

                                    holder.itemView.getContext().startActivity( intent );

                                }
                                else if (which==1)
                                {

                                    deleteReceivemessages( position, holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );
                                }

                            }
                        } );

                        builder.show();


                    }

                    else if(userMessageList.get( position ).getType().equals("text"))
                    {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder( holder.itemView.getContext() );
                        builder.setTitle( "Delete Message" );

                        builder.setItems( options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {

                                    deleteReceivemessages( position, holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }


                            }
                        } );

                        builder.show();


                    }

                    else if(userMessageList.get( position ).getType().equals("image" ))
                    {

                        CharSequence options[] = new CharSequence[]
                                {
                                        "View this Image",
                                        "Delete",
                                        "Cancel",
                                };
                        AlertDialog.Builder builder=new AlertDialog.Builder( holder.itemView.getContext() );
                        builder.setTitle( "Delete Message" );

                        builder.setItems( options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which==0)
                                {

                                    Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                    intent.putExtra( "url", userMessageList.get( position ).getMessage() );
                                    holder.itemView.getContext().startActivity( intent );

                                }
                                else if (which==1)
                                {

                                    deleteReceivemessages( position, holder );

                                    Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                    holder.itemView.getContext().startActivity( intent );

                                }


                            }
                        } );

                        builder.show();


                    }
                }
            } );



        }



    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    private void deletesentmessages(final int position, final MessageviewHolder holder)
    {

        DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();
        Rootref.child( "Messages" )
                .child( userMessageList.get( position ).getFrom() )
                .child( userMessageList.get( position ).getTo() )
                .child( userMessageList.get( position ).getMessageID() )
                .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                
                if(task.isSuccessful())
                {
                    Toast.makeText( holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    Toast.makeText( holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT ).show();
                }
                
            }
        } );

    }

    private void deleteReceivemessages(final int position, final MessageviewHolder holder)
    {

        DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();
        Rootref.child( "Messages" )
                .child( userMessageList.get( position ).getTo() )
                .child( userMessageList.get( position ).getFrom() )
                .child( userMessageList.get( position ).getMessageID() )
                .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText( holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT ).show();
                }
                else
                {
                    Toast.makeText( holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT ).show();
                }

            }
        } );

    }

    private void deleteforeveryonemessages(final int position, final MessageviewHolder holder)
    {

        final DatabaseReference Rootref=FirebaseDatabase.getInstance().getReference();
        Rootref.child( "Messages" )
                .child( userMessageList.get( position ).getTo() )
                .child( userMessageList.get( position ).getFrom() )
                .child( userMessageList.get( position ).getMessageID() )
                .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {

                    Rootref.child( "Messages" )
                            .child( userMessageList.get( position ).getFrom() )
                            .child( userMessageList.get( position ).getTo() )
                            .child( userMessageList.get( position ).getMessageID() )
                            .removeValue().addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Toast.makeText( holder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT ).show();
                            }
                        }
                    } );

                }
                else
                {
                    Toast.makeText( holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT ).show();
                }

            }
        } );

    }


}
