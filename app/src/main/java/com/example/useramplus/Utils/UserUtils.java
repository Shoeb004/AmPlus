package com.example.useramplus.Utils;

import android.view.View;

import androidx.annotation.NonNull;

import com.example.useramplus.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserUtils {
    public static void updateUser(View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RIDER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Snackbar.make(view, "Updated information successfullt!", Snackbar.LENGTH_SHORT).show();

                    }
                });
    }
}
