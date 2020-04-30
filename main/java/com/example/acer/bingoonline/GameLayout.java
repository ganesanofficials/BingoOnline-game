package com.example.acer.bingoonline;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GameLayout extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[5][5];
    private int numCound = 1;
    private boolean youtTrun=false;
    private boolean put_numbers = true,put_Trues=false,finish=false,win;
    private boolean booleanarray[][]=new boolean[5][5];
    private int numArray[][]=new int[5][5];
    private int yourPoint=0,oppenentPoint=0;
    private TextView playertrun,tvYourPoint,tvOppenentPoint;


    String playerSession = "";
    String userName = "";
    String otherPlayer = "";
    String loginUID = "";
    String requestType = "";

    DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
    DatabaseReference yourPointRef,oppenentPointRef;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game_layout);

        Button bingo = (Button) findViewById(R.id.bingo);
        playertrun = (TextView) findViewById(R.id.your_trun);
        tvYourPoint=(TextView) findViewById(R.id.tv_yourPoint);
        tvOppenentPoint=(TextView)findViewById(R.id.tv_oppenendPoint);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                String ButtonId = "B_" + i + j;
                int resID = getResources().getIdentifier(ButtonId, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setBackgroundColor(getResources().getColor(R.color.NewButton));
                //buttons[i][j].setBackgroundColor(getResources().getColor(R.color.ClickedButton));
                buttons[i][j].setOnClickListener(this);
            }
        }

        userName = getIntent().getExtras().get("user_name").toString();
        loginUID = getIntent().getExtras().get("login_uid").toString();
        otherPlayer = getIntent().getExtras().get("other_player").toString();
        requestType = getIntent().getExtras().get("request_type").toString();
        playerSession = getIntent().getExtras().get("player_session").toString();

        tvYourPoint.setText("Your points :"+yourPoint);
        playertrun.setText("Your Turn");

        ref.child("users").child(userName).removeValue();

        ref.child("playing").child(playerSession).child(userName).child("exit").setValue("no");

        ref.child("playing").child(playerSession).child(userName).child("num").setValue("No");
        //point refs
        ref.child("playing").child(playerSession).child(userName).child("points").setValue(yourPoint+"");


        if(requestType.equalsIgnoreCase("From")){
            ref.child("playing").child(playerSession).child(userName).child("trun").setValue(userName);
            youtTrun=true;
        }
        else{
            ref.child("playing").child(playerSession).child(userName).child("trun").setValue(otherPlayer);
            ref.child("playing").child(playerSession).child(userName).child("finish").setValue("false");
        }

        //exit finder
        ref.child("playing").child(playerSession).child(userName).child("exit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    String str=dataSnapshot.getValue().toString();
                    if(str.equals("yes")){
                        ExitDialog();
                    }

                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        ref.child("playing").child(otherPlayer+":"+userName).child(otherPlayer).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String trun = (String) dataSnapshot.child("trun").getValue();
                    if(requestType.equalsIgnoreCase("From")) {
                    String finised=(String) dataSnapshot.child("finish").getValue();
                    if(finised.equalsIgnoreCase("true")){
                        finish=true;
                        Toast.makeText(getApplicationContext(),"opposite Player Ready",Toast.LENGTH_SHORT).show();
                    }}
                    if (trun.equalsIgnoreCase(userName)) {

                        playertrun.setText("your Turn");
                        youtTrun=true;
                        ref.child("playing").child(otherPlayer+":"+userName).child(otherPlayer).child("trun").setValue("checked");
                    }
                    else if(trun.equalsIgnoreCase(otherPlayer)){
                            playertrun.setText(trun+" Turn");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        ref.child("playing").child(otherPlayer+":"+userName).child(otherPlayer).child("num").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String s = (String)dataSnapshot.getValue();
                    if (!s.equalsIgnoreCase("No")) {
                       int n=Integer.parseInt(s);
                       int x=-1,y=-1;
                       for(int i=0;i<5;i++){
                           for(int j=0;j<5;j++){
                               if(numArray[i][j]==n){
                                   x=i;
                                   y=j;
                                   break;
                               }
                           }
                       }
                        booleanarray[x][y]=true;
                        buttons[x][y].setBackgroundColor(getResources().getColor(R.color.ClickedButton));
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ref.child("playing").child(otherPlayer+":"+userName).child(otherPlayer).child("points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    String str=(String) dataSnapshot.getValue();
                    int point=Integer.parseInt(str);
                    tvOppenentPoint.setText(otherPlayer+" Points :"+str+"");
                    if(point>0){
                        tvOppenentPoint.setText(otherPlayer+" Points :"+str);
                        DialogDisplay(otherPlayer+" win the match");
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View v) {
        if (!((Button) v).getText().toString().equals("") && !put_Trues) {
            return;
        }
        if (put_numbers) {
            String s = numCound + "";
            ((Button) v).setText(s);
            String str= getResources().getResourceEntryName(((Button) v).getId());
            int i=Integer.parseInt(str.substring(2,3));
            int j=Integer.parseInt(str.substring(3,4));
            numArray[i][j]=numCound;
            numCound++;
            if(numCound>25){
                put_numbers=false;
                put_Trues=true;
                if(!requestType.equalsIgnoreCase("From")){
                ref.child("playing").child(playerSession).child(userName).child("finish").setValue("true");}
            }
        }
        else if(put_Trues){
            if(requestType.equalsIgnoreCase("From")){
                if(youtTrun&&finish){
                    String num= String.valueOf(((Button) v).getText());
                    String str= getResources().getResourceEntryName(((Button) v).getId());
                    int i=Integer.parseInt(str.substring(2,3));
                    int j=Integer.parseInt(str.substring(3,4));
                    if(!booleanarray[i][j]){
                        booleanarray[i][j]=true;
                        //((Button) v).setBackgroundColor(R.color.ClickedButton);
                        buttons[i][j].setBackgroundColor(getResources().getColor(R.color.ClickedButton));
                        ref.child("playing").child(playerSession).child(userName).child("num").setValue(num);
                        ref.child("playing").child(playerSession).child(userName).child("trun").setValue(otherPlayer);
                        youtTrun=false;
                        playertrun.setText(otherPlayer+" Turn");
                        //isWin();
                    }
                }
                else if(youtTrun){
                    Toast.makeText(getApplicationContext(),"Opposite player Didn't fill Numbers, please wait..",Toast.LENGTH_SHORT).show();
                }
            }
            else if(youtTrun){
            String num= String.valueOf(((Button) v).getText());
            String str= getResources().getResourceEntryName(((Button) v).getId());
            int i=Integer.parseInt(str.substring(2,3));
            int j=Integer.parseInt(str.substring(3,4));
            if(!booleanarray[i][j]){
                booleanarray[i][j]=true;
                //((Button) v).setBackgroundColor(R.color.ClickedButton);
                buttons[i][j].setBackgroundColor(getResources().getColor(R.color.ClickedButton));
                ref.child("playing").child(playerSession).child(userName).child("num").setValue(num);
                ref.child("playing").child(playerSession).child(userName).child("trun").setValue(otherPlayer);
                youtTrun=false;
                //isWin();
                playertrun.setText(otherPlayer+" Turn");
            }
        }
        }
    }
    @SuppressLint("ResourceAsColor")
    public void reSetGame(){
        numCound = 1;
        youtTrun=false;
        put_numbers = true;
        put_Trues=false;
        finish=false;
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                numArray[i][j]=0;
                booleanarray[i][j]=false;
                buttons[i][j].setText("");
                buttons[i][j].setBackgroundColor(getResources().getColor(R.color.NewButton));
            }
        }

        ref.child("playing").child(playerSession).child(userName).child("num").setValue("No");
        ref.child("playing").child(playerSession).child(userName).child("trun").setValue("No");

        if(requestType.equalsIgnoreCase("From")){
            ref.child("playing").child(playerSession).child(userName).child("trun").setValue(userName);
            youtTrun=true;
        }
        else{
            ref.child("playing").child(playerSession).child(userName).child("trun").setValue(otherPlayer);
            ref.child("playing").child(playerSession).child(userName).child("finish").setValue("false");
        }

    }
    public void isWin(View v){
        if(checkWin()){
            yourPoint++;
            ref.child("playing").child(playerSession).child(userName).child("points").setValue(yourPoint+"");
            tvYourPoint.setText("Your Points :"+yourPoint+" ");
            DialogDisplay("You win the Match");

        }
    }

    public boolean checkWin(){

        int bingo=0;
        for(int i=0;i<5;i++)
        {
            if(booleanarray[i][0]&&booleanarray[i][1]&&booleanarray[i][2]&&booleanarray[i][3]&&booleanarray[i][4]) {
                bingo++;
            }
            if(booleanarray[0][i]&&booleanarray[1][i]&&booleanarray[2][i]&&booleanarray[3][i]&&booleanarray[4][i]) {
                bingo++;
            }
        }
        if(booleanarray[0][0]&&booleanarray[1][1]&&booleanarray[2][2]&&booleanarray[3][3]&&booleanarray[4][4]) {
            bingo++;
        }
        if(booleanarray[0][4]&&booleanarray[1][3]&&booleanarray[2][2]&&booleanarray[3][1]&&booleanarray[4][0]) {
            bingo++;
        }
        if(bingo>=5)
        {
            return true;
        }
            return false;
    }

    public void DialogDisplay(String str){
        AlertDialog.Builder b=new AlertDialog.Builder(this,R.style.Mydialog);
        b.setTitle(str);
        b.setMessage("Do you want Rematch to "+otherPlayer);
        b.setCancelable(false);
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reSetGame();

            }
        });

        b.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ref.child("playing").child(playerSession).removeValue();
                Intent i=new Intent(GameLayout.this,MainActivity.class);
                startActivity(i);
            }
        }).show();
    }

    public void ExitDialog(){
        ref.child("playing").child(playerSession).child(userName).child("exit").setValue("no");
        AlertDialog.Builder b=new AlertDialog.Builder(this,R.style.Mydialog);
        b.setTitle("Opposite player Exit the Game");
       // b.setMessage("Do you want Rematch to "+otherPlayer);
        b.setCancelable(false);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ref.child("playing").child(playerSession).removeValue();
                ref.child("users").child(userName).removeValue();
                Intent i=new Intent(GameLayout.this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        }).show();




    }
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exitByBackKey();
        return true;
    }
        return super.onKeyDown(keyCode,event);
    }
protected void exitByBackKey(){
    AlertDialog.Builder b=new AlertDialog.Builder(GameLayout.this);
    b.setTitle("Exit");
    b.setMessage("Do you want Exit the Game");
    b.setCancelable(false);
    b.setPositiveButton("yes", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ref.child("playing").child(playerSession).removeValue();
            ref.child("users").child(userName).removeValue();
            ref.child("playing").child(otherPlayer+":"+userName).child(otherPlayer).child("exit").setValue("yes");
            Intent i=new Intent(GameLayout.this,MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

        }
    });

    b.setNegativeButton("No", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    });
    b.show();
}

}
