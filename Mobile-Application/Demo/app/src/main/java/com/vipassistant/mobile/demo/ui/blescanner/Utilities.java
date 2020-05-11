package com.vipassistant.mobile.demo.ui.blescanner;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class Utilities {

    public static void toast(Context context, String string) {

        Toast toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}
