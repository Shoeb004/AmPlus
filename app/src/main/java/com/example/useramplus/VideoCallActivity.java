package com.example.useramplus;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VideoCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        TextView yourUserID = findViewById(R.id.your_user_id);
        TextView yourUserName = findViewById(R.id.your_user_name);
//        String userID = getIntent().getStringExtra("userID");
//        String userName = getIntent().getStringExtra("userName");
        String generateUserId = generateUserId();
        String username = generateUserId + "_" + Build.MANUFACTURER;


        yourUserID.setText("Your User ID :" + generateUserId);
        yourUserName.setText("Your User Name :" + username);

        long appID = 1972428243; //Enter your appid
        String appSign = "17ee2e7e2c0e1cfe066deed470d539954e1ae9aea31d4229482e62a7f94a30cc"; //Enter your appsign

        initCallInviteService(appID, appSign, generateUserId, username);

        initVoiceButton();

        initVideoButton();

        findViewById(R.id.user_logout).setOnClickListener(v -> {
            AlertDialog.Builder builder = new Builder(VideoCallActivity.this);
            builder.setTitle("Sign Out");
            builder.setMessage("Are you sure to Go Back?");
            builder.setNegativeButton("Cancel", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ZegoUIKitPrebuiltCallInvitationService.unInit();
                    finish();
                }
            });
            builder.create().show();
        });
    }

    public void initCallInviteService(long appID, String appSign, String userID, String userName) {

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallInvitationService.init(getApplication(), appID, appSign, userID, userName,
                callInvitationConfig);
    }

    private void initVideoButton() {
        ZegoSendCallInvitationButton newVideoCall = findViewById(R.id.new_video_call);
        newVideoCall.setIsVideoCall(true);
        newVideoCall.setOnClickListener(v -> {
            TextInputLayout inputLayout = findViewById(R.id.target_user_id);
            String targetUserID = inputLayout.getEditText().getText().toString();
            String[] split = targetUserID.split(",");
            List<ZegoUIKitUser> users = new ArrayList<>();
            for (String userID : split) {
                String userName = userID + "_name";
                users.add(new ZegoUIKitUser(userID, userName));
            }
            newVideoCall.setInvitees(users);
        });
    }

    private void initVoiceButton() {
        ZegoSendCallInvitationButton newVoiceCall = findViewById(R.id.new_voice_call);
        newVoiceCall.setIsVideoCall(false);
        newVoiceCall.setOnClickListener(v -> {
            TextInputLayout inputLayout = findViewById(R.id.target_user_id);
            String targetUserID = inputLayout.getEditText().getText().toString();
            String[] split = targetUserID.split(",");
            List<ZegoUIKitUser> users = new ArrayList<>();
            for (String userID : split) {
                String userName = userID + "_name";
                users.add(new ZegoUIKitUser(userID, userName));
            }
            newVoiceCall.setInvitees(users);
        });
    }

    private String generateUserId() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 5){
            int nextInt = random.nextInt(10);
            if(builder.length() == 0 && nextInt == 0){
                continue;
            }
            builder.append(nextInt);
        }
        return builder.toString();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUIKitPrebuiltCallInvitationService.unInit();
    }
}