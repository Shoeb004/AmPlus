package com.example.useramplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.useramplus.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.useramplus.databinding.ActivityUserHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

public class UserHomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1000;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityUserHomeBinding binding;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private  NavController navController;
    private ImageView img_avatar;
    private Uri imageUri;
    private AlertDialog waitingDialog;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarUserHome.toolbar);

        drawer = binding.drawerLayout;
        navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        init();
    }
    public void init(){

        storageReference = FirebaseStorage.getInstance().getReference();

        waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Waithing...")
                .create();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_sign_out)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserHomeActivity.this);
                    builder.setTitle("Sign out");
                    builder.setMessage("Do you really want to sign out");
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }
                    }).setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(UserHomeActivity.this, SplashScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }
                    }).setCancelable(false);

                    AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(ContextCompat.getColor(UserHomeActivity.this,android.R.color.holo_red_dark));

                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(UserHomeActivity.this,R.color.colorAccent));

                        }
                    });

                    dialog.show();

                }
                return true;
            }
        });

        //set data for user
        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = (TextView) headerView.findViewById(R.id.text_name);
        TextView txt_phone = (TextView) headerView.findViewById(R.id.txt_phone);
        img_avatar = (ImageView)headerView.findViewById(R.id.img_avatar);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentUser != null ? Common.currentUser.getPhoneNumber() : "");

        img_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);

            }
        });

        if (Common.currentUser != null && Common.currentUser.getAvatar() != null &&
                !TextUtils.isEmpty(Common.currentUser.getAvatar()))
        {
            Glide.with(this)
                    .load(Common.currentUser.getAvatar())
                    .into(img_avatar);
        }

        // Zego cloud
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if(id==R.id.video_menu){
                    startActivity(new Intent(UserHomeActivity.this, VideoCallActivity.class));
                }
//                else if(id==R.id.chat_bot){
//                    startActivity(new Intent(SideBarActivity.this,HomePage.class));
//                }
                else if(id==R.id.chat_with_us){
                    startActivity(new Intent(UserHomeActivity.this,InAppActivity.class));
                }
                else if(id==R.id.nav_sign_out){
                    signOutFunction();
                }

                else if(id==R.id.share_menu){
                    ApplicationInfo api = getApplicationContext().getApplicationInfo();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String shareBody = "Here is the share content";
                    intent.setType("text/plain");
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Try subject");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT,shareBody);
                    startActivity(Intent.createChooser(intent, "ShareVia"));
                }


                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_user_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    void signOutFunction(){

//                startActivity(new Intent(UserHomeActivity.this,SplashScreenActivity.class));
        AlertDialog.Builder builder = new AlertDialog.Builder(UserHomeActivity.this);
        builder.setTitle("Sign out");
        builder.setMessage("Do you really want to sign out");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserHomeActivity.this, SplashScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        }).setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(UserHomeActivity.this,android.R.color.holo_red_dark));

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(UserHomeActivity.this,R.color.colorAccent));

            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imageUri = data.getData();
                img_avatar.setImageURI(imageUri);

                showImageDialog();

            }

        }
    }

    private void showImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserHomeActivity.this);
        builder.setTitle("Change avatar");
        builder.setMessage("Do you really want to change avatar");
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (imageUri != null)
                {
                    waitingDialog.setMessage("Uploading...");
                    waitingDialog.show();

                    String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    StorageReference avatarFolder = storageReference.child("avatar/"+unique_name);

                    avatarFolder.putFile(imageUri)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();
                                    Snackbar.make(drawer, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()){
                                        avatarFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Map<String, Object> updateData = new HashMap<>();
                                                updateData.put("avatar", uri.toString());

                                                UserUtils.updateUser(drawer, updateData);

                                            }
                                        });

                                    }
                                    waitingDialog.dismiss();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                    waitingDialog.setMessage(new StringBuilder("Uploading").append(progress).append("%"));
                                }

                            });
                }

            }
        }).setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.colorAccent));

            }
        });

        dialog.show();
    }
}