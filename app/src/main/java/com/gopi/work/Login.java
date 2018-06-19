package com.gopi.work;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class Login extends AppCompatActivity {

    private AutoCompleteTextView phNo;
    private AutoCompleteTextView code;

    private Button click;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseusers;

    private ProgressDialog progressDialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private int buttonType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setBackgroundDrawableResource(R.color.colorPrimary);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        phNo = (AutoCompleteTextView) findViewById(R.id.phNO);
        code = (AutoCompleteTextView) findViewById(R.id.code);

        click = (Button) findViewById(R.id.click);

        mAuth = FirebaseAuth.getInstance();
        databaseusers = FirebaseDatabase.getInstance().getReference().child("User");
        databaseusers.keepSynced(true);


        progressDialog = new ProgressDialog(this);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ph = "+91"+phNo.getText().toString().trim();

                if(buttonType == 0) {

                    if (TextUtils.isEmpty(ph)) {
                        makeText(Login.this, "Fields are empty", LENGTH_LONG).show();
                    } else {

                        phNo.setEnabled(false);
                        click.setEnabled(false);

                        progressDialog.setMessage("Sending Code");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                ph,
                                60,
                                TimeUnit.SECONDS,
                                Login.this,
                                mCallbacks
                        );

                    }
                }else {
                    progressDialog.dismiss();
                    click.setEnabled(false);
                    code.setVisibility(View.VISIBLE);

                    String verificationCode = code.getText().toString().trim();

                    if (TextUtils.isEmpty(verificationCode)){
                        makeText(Login.this,"Fields are Empty",LENGTH_LONG).show();
                    }else {
                        progressDialog.setMessage("Verifying Code");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);

                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                progressDialog.dismiss();

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

//                makeText(Login.this, "Error in verification", LENGTH_LONG).show();

                Log.w("Login", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    makeText(Login.this, "Invalid request", LENGTH_LONG).show();
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    makeText(Login.this, "The SMS quota for the project has been exceeded", LENGTH_LONG).show();
                    // The SMS quota for the project has been exceeded
                    // ...
                }


            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                progressDialog.dismiss();
                buttonType = 1;
                mVerificationId = verificationId;
                mResendToken = token;

                phNo.setVisibility(View.INVISIBLE);
                code.setVisibility(View.VISIBLE);

                click.setText("Verify Code");
                click.setEnabled(true);

                // ...
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = task.getResult().getUser();

                            Intent main = new Intent(Login.this, MainActivity.class);
                            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(main);
                            finish();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            progressDialog.dismiss();
                            makeText(Login.this, "Sign In Problem", LENGTH_LONG).show();
                            phNo.setEnabled(true);
                            click.setEnabled(true);
                        }
                    }
                });
    }
}
