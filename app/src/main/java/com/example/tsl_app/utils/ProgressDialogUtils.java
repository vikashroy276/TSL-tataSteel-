package com.example.tsl_app.utils;

import android.app.Activity;
import android.app.ProgressDialog;


/**
 * Created by mars on 28/02/18.
 */

public class ProgressDialogUtils {

    private static ProgressDialog dialog;
    private static Activity _activity;

    public static void showLoader(Activity activity) {
        try {
            if (dialog == null) {
                dialog = new ProgressDialog(activity);
                dialog.setMessage("Loading....");
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                _activity = activity;
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            dialog = null;
        }

    }


    public static void setMessage(String message) {
        if (dialog != null) dialog.setMessage(message);
    }


    public static void hideLoader() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            dialog = null;
        }

    }

}