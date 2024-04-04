package com.example.useramplus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.useramplus.Model.RiderModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

//    @BindView(R.id.progress_bar)
//    ProgressBar Progress_bar;
//    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

    FirebaseDatabase database;
    DatabaseReference riderInfoRef;


    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

       init();
    }
    private void init(){

        ButterKnife.bind(this);
        
        database = FirebaseDatabase.getInstance();
        riderInfoRef = database.getReference(Common.RIDER_INFO_REFERENCE);
        
        
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth ->{
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null){
                Toast.makeText(this, "Welcome: "+user.getUid(), Toast.LENGTH_SHORT).show();
                checkUserFromFirebase();
            }else {
                showLoginLayout();
            }
        };

    }

    private void checkUserFromFirebase() {
//        Toast.makeText(this, "checking from database", Toast.LENGTH_SHORT).show();
        riderInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(SplashScreenActivity.this, "User already register", Toast.LENGTH_SHORT).show();
                            RiderModel riderInfoModel = snapshot.getValue(RiderModel.class);

                            goToHomeActivity(riderInfoModel);
                        }else {
//                            Toast.makeText(SplashScreenActivity.this, "Else part", Toast.LENGTH_SHORT).show();
                            showRegisterlayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreenActivity.this, "OnCancelled "+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
        
    }
    private void goToHomeActivity(RiderModel riderInfoModel) {
        Common.currentUser = riderInfoModel; //Init value
//        Toast.makeText(this, "goToHomeActivity", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SplashScreenActivity.this, UserHomeActivity.class));
        finish();
    }
    private void showRegisterlayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);

        TextInputEditText edt_first_name = (TextInputEditText)itemView.findViewById(R.id.edt_first_name);
        TextInputEditText edt_last_name = (TextInputEditText)itemView.findViewById(R.id.edt_last_name);
        TextInputEditText edt_phone = (TextInputEditText)itemView.findViewById(R.id.edt_phone_number);

        Button btn_continue = (Button)itemView.findViewById(R.id.btn_register);

        //                set data
        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
            edt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

//        //set view
        builder.setView(itemView);
        AlertDialog dialog = builder.create();
        dialog.show();
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edt_first_name.getText().toString()))
                {
                    Toast.makeText(SplashScreenActivity.this, "Please enter first name", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(edt_last_name.getText().toString()))
                {
                    Toast.makeText(SplashScreenActivity.this, "Please enter last name", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (TextUtils.isEmpty(edt_phone.getText().toString()))
                {
                    Toast.makeText(SplashScreenActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    RiderModel model = new RiderModel();
                    model.setFirstName(edt_first_name.getText().toString());
                    model.setLastName(edt_last_name.getText().toString());
                    model.setPhoneNumber(edt_phone.getText().toString());

                    riderInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(model)
                            .addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(SplashScreenActivity.this, "Failure "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SplashScreenActivity.this, "Register successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                goToHomeActivity(model);

                            });
                }
            }
        });

    }


//    {
//        "rules": {
//        "DriverInfo": {
//            "$uid" : {
//                ".read": "auth != null",
//                        ".write": "$uid === auth.uid"
//            }
//        },
//        "DriversLocation": {
//            "$uid" : {
//                ".read": "auth != null",
//                        ".write": "$uid === auth.uid"
//            }
//        }
//    }
//    }


    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(),LOGIN_REQUEST_CODE);
    }

    private void delaySplashScreen() {

//        progressBar.setVisibility(View.VISIBLE);
        Completable.timer(5, TimeUnit.SECONDS,
                        AndroidSchedulers.mainThread())
                .subscribe( ()->
                                //After show splash screen, ask if login if not login
                                firebaseAuth.addAuthStateListener(listener)
//                                Toast.makeText(SplashScreenActivity.this, "Welcome: "+FirebaseAuth.getInstance().getCurrentUser().getUid(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText( this, "[ERROR]: "+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}