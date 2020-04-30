package com.example.acer.bingoonline;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private boolean registered=false;
    private TextView userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        userId=(TextView)findViewById(R.id.tv_userID);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            userId.setText(currentUser.getEmail());
            registered=true;

        }
        else{
            JoinOnline();
        }
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
    }

    public void online(View v){
        if(registered){
            Intent i=new Intent(getApplicationContext(),BingoLobby.class);
            startActivity(i);
        }

    }
    public void JoinOnline() {
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        LayoutInflater inflater=this.getLayoutInflater();
        final View dialogView=inflater.inflate(R.layout.login_dialog,null);
        b.setView(dialogView);

        final EditText etEmail = (EditText) dialogView.findViewById(R.id.email_input);
        final EditText etPassword = (EditText) dialogView.findViewById(R.id.password_input);

        b.setTitle("Please register");
        b.setMessage("Enter you email and password for registration");
        b.setCancelable(false);
        b.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RegisterUser(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });
        b.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finish();
            }
        });
        b.show();
    }

    public void RegisterUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Auth Complete", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        Toast.makeText(getApplicationContext(),"regisetr successfully complete",Toast.LENGTH_SHORT).show();
                        registered=true;
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            registered=false;
                            Toast.makeText(getApplicationContext(), "Auth failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void onBackPressed(){
        finish();
        System.exit(0);
        super.onBackPressed();

    }

}
