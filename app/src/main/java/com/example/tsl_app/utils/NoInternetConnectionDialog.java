package com.example.tsl_app.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.tsl_app.R;


public class NoInternetConnectionDialog {
    private static Dialog dialog;


    public static void showDialog(Context context, String title) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.nointernetconnectionlayout);

        CardView okayBtn = dialog.findViewById(R.id.okay_btn);
        TextView errortxt = dialog.findViewById(R.id.errortxt);

        errortxt.setText(title);

        okayBtn.setOnClickListener(v -> {
            dismissDialog();
            ProgressDialogUtils.hideLoader();
        });

        dialog.show();
    }

    public static void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();

        }
        dialog = null;
    }
}
