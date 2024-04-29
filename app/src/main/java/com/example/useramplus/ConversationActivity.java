package com.example.useramplus;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zegocloud.zimkit.common.ZIMKitRouter;
import com.zegocloud.zimkit.common.enums.ZIMKitConversationType;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;


import java.util.Arrays;
import java.util.List;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMErrorCode;

public class ConversationActivity extends AppCompatActivity {

    FloatingActionButton actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        actionButton = findViewById(R.id.floating_btn);

        actionButton.setOnClickListener(v -> {
            showPopupMenu();
        });
    }

    void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, actionButton);
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {

            if (menuItem.getItemId() == R.id.new_chat) {
                showNewChatDialog();
                return true;
            }
            if (menuItem.getItemId() == R.id.create_group) {
                showNewGroupDialog();
                return true;
            }

            if (menuItem.getItemId() == R.id.join_group) {
                showJoinGroupDialog();
                return true;
            }
            if (menuItem.getItemId() == R.id.logout) {
                startActivity(new Intent(ConversationActivity.this,UserHomeActivity.class));
                finish();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    void showNewChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Chat");

        EditText editText = new EditText(this);
        editText.setHint("User ID");
        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ZIMKitRouter.toMessageActivity(ConversationActivity.this,editText.getText().toString(), ZIMKitConversationType.ZIMKitConversationTypePeer);
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void showNewGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Group");

        final EditText editText = new EditText(this);
        final EditText editText2 = new EditText(this);

        editText.setHint("Group Name");
        editText2.setHint("Other User's Id");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText);
        linearLayout.addView(editText2);
        builder.setView(linearLayout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<String> ids = Arrays.asList(editText2.getText().toString().split(","));
                createGroupChat(ids, editText.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void createGroupChat(List<String> ids, String groupName) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ZIMKit.createGroup(groupName, ids, (groupInfo, inviteUserErrors, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                if (!inviteUserErrors.isEmpty()) {
                    // Implement the logic for the prompt window based on your business logic when there is a non-existing user ID in the group.
                } else {
                    // Directly enter the chat page when the group chat is created successfully.
                    ZIMKitRouter.toMessageActivity(this, groupInfo.getId(),ZIMKitConversationType.ZIMKitConversationTypeGroup);
                }
            } else {
                // Implement the logic for the prompt window based on the returned error info when failing to create a group chat.
            }
        });
    }

    void showJoinGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Group");

        EditText editText = new EditText(this);
        editText.setHint("Group ID");
        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                joinGroupChat(editText.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void joinGroupChat(String groupId) {
        ZIMKit.joinGroup(groupId, (groupInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                // Enter the group chat page after joining the group chat successfully.
                ZIMKitRouter.toMessageActivity(this, groupInfo.getId(),ZIMKitConversationType.ZIMKitConversationTypeGroup);
            } else {
                // Implement the logic for the prompt window based on the returned error info when failing to join the group chat.
            }
        });
    }


}