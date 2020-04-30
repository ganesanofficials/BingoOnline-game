package com.example.acer.bingoonline;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BingoLobby extends AppCompatActivity {

    ListView lv_loginUsers,lv_requstedUsers;
    ArrayList<String> list_loginUsers=new ArrayList<String>();
    ArrayAdapter adpt,reqUsersAdpt;
    TextView tvAcceptRequest,tvsendRequest,TvuserId;
    ArrayList<String> list_requestedUsers = new ArrayList<String>();

    String UserName="",uid=" ",LoginUserID;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    DatabaseReference add=database.getReference();

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_bingo_lobby);
        mAuth = FirebaseAuth.getInstance();

        tvAcceptRequest=(TextView) findViewById(R.id.tvAcceptRequest);
        tvsendRequest=(TextView) findViewById(R.id.tvSendRequest);
        TvuserId=findViewById(R.id.userId);
        tvAcceptRequest.setText("please wait...");
        tvsendRequest.setText("please wait...");

        lv_loginUsers=(ListView) findViewById(R.id.lv_loginUsers);
        adpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_loginUsers);
        lv_loginUsers.setAdapter(adpt);

        lv_requstedUsers = (ListView) findViewById(R.id.lv_requestedUsers);
        reqUsersAdpt = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_requestedUsers);
        lv_requstedUsers.setAdapter(reqUsersAdpt);

        myRef.getRoot().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateLoginUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        lv_loginUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String requestToUser = ((TextView)view).getText().toString();
                confirmRequest(requestToUser, "To");
                //check this number
                //reqUsersAdpt.clear();
            }
        });



        lv_requstedUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String requestFromUser = ((TextView)view).getText().toString();
                confirmRequest(requestFromUser, "From");

            }
        });

    }


    public void onStart(){
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            LoginUserID=currentUser.getEmail();
            TvuserId.setText(convertEmailToString(LoginUserID));
            uid=currentUser.getUid();
            UserName = convertEmailToString(LoginUserID);
            UserName = UserName.replace(".", "");
            myRef.child("users").child(UserName).child("request").setValue(uid);
            reqUsersAdpt.clear();
            AcceptIncomingRequest();

        }

    }

    public void onBackPressed(){
        myRef.child("users").child(UserName).removeValue();
        super.onBackPressed();

    }

    private String convertEmailToString(String Email){
        String value = Email.substring(0, Email.indexOf('@'));
        value = value.replace(".", "");
        return value;
    }

    void confirmRequest(final String OtherPlayer, final String reqType) {
        AlertDialog.Builder b = new AlertDialog.Builder(this,R.style.Mydialog);
        //LayoutInflater inflater = this.getLayoutInflater();
        //final View dialogView = inflater.inflate(R.layout.connect_player_dialog, null);
        // b.setView(dialogView);

        b.setTitle("Start Game?");
        b.setMessage("Connect with " + OtherPlayer);
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myRef.child("users")
                        .child(OtherPlayer).child("request").push().setValue(LoginUserID);
                if(reqType.equalsIgnoreCase("From")) {
                    StartGame(UserName + ":" + OtherPlayer, OtherPlayer, "From");
                }else{
                    myRef.child("users").child(UserName).removeValue();
                    StartGame(UserName + ":" + OtherPlayer, OtherPlayer, "To");
                }
            }
        });
        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.dismiss();
                //Intent i=new Intent(getApplicationContext(),check.class);
                //startActivity(i);
            }
        });
        b.show();
    }

    void StartGame(String PlayerGameID, String OtherPlayer, String requestType){
        myRef.child("playing").child(PlayerGameID).removeValue();
        Intent intent = new Intent(getApplicationContext(), GameLayout.class);
        intent.putExtra("player_session", PlayerGameID);
        intent.putExtra("user_name", UserName);
        intent.putExtra("other_player", OtherPlayer);
        intent.putExtra("login_uid", uid);
        intent.putExtra("request_type", requestType);
        startActivity(intent);
    }

    public void updateLoginUsers(DataSnapshot dataSnapshot){
        String key = "";
        Set<String> set = new HashSet<String>();
        Iterator i = dataSnapshot.getChildren().iterator();

        while(i.hasNext()){
            key = ((DataSnapshot) i.next()).getKey();
            if(!key.equalsIgnoreCase(UserName)) {
                set.add(key);
            }
        }

        adpt.clear();
        adpt.addAll(set);
        adpt.notifyDataSetChanged();
        tvsendRequest.setText("Online Players");
        tvAcceptRequest.setText("Requested Players");
    }

    private void AcceptIncomingRequest() {
        myRef.child("users").child(UserName).child("request")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                            if(map != null){
                                String value = "";
                                for(String key:map.keySet()){
                                    value = (String) map.get(key);
                                    reqUsersAdpt.add(convertEmailToString(value));
                                    reqUsersAdpt.notifyDataSetChanged();
                                    myRef.child("users").child(UserName).child("request").setValue(uid);
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }
}
