package com.example.whatsapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactsFragment extends Fragment {

    private View ContactsView;
    private RecyclerView myContactsList;
    private String currentUserID;


    private FirebaseDatabase mDatabase;
    private DatabaseReference ContactsRef, UserRef;
    private FirebaseAuth mAuth;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);


        InitializeFields();


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        ContactsRef = mDatabase.getReference().child(ProfileActivity.CONTACTS).child(currentUserID);
        UserRef = mDatabase.getReference().child(MainActivity.USERS);


        return ContactsView;
    }

    private void InitializeFields() {

        myContactsList = ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

    }


    @Override
    public void onStart() {

        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull final Contacts model) {


                        String UserIDs = getRef(position).getKey();

                        UserRef.child(UserIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    if (dataSnapshot.child(MainActivity.USER_STATE).hasChild(MainActivity.STATE)) {

                                        String state = dataSnapshot.child(MainActivity.USER_STATE).child(MainActivity.STATE).getValue().toString();
                                        String date = dataSnapshot.child(MainActivity.USER_STATE).child(GroupChatActivity.DATE).getValue().toString();
                                        String time = dataSnapshot.child(MainActivity.USER_STATE).child(GroupChatActivity.TIME).getValue().toString();

                                        if (state.equals(MainActivity.ONLINE)) {

                                            holder.onlineIcon.setVisibility(View.VISIBLE);

                                        } else if (state.equals(MainActivity.OFFLINE)) {

                                            holder.onlineIcon.setVisibility(View.INVISIBLE);

                                        }

                                    } else {


                                        holder.onlineIcon.setVisibility(View.INVISIBLE);

                                    }


                                    if (dataSnapshot.hasChild(SettingsActivity.IMAGE)) {
                                        String userProfileImage = dataSnapshot.child(SettingsActivity.IMAGE).getValue().toString();


                                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                    }
                                    String profileName = dataSnapshot.child(MainActivity.NAME).getValue().toString();
                                    String profileStatus = dataSnapshot.child(SettingsActivity.STATUS).getValue().toString();

                                    holder.userName.setText(profileName);
                                    holder.userStatus.setText(profileStatus);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}
