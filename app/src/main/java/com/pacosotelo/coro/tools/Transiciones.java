package com.pacosotelo.coro.tools;

import android.app.Activity;
import android.os.Build;
import com.pacosotelo.coro.R;

public class Transiciones {
    public static void left(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.left_in, R.anim.left_out);
        } else {
            activity.overridePendingTransition(R.anim.left_in, R.anim.left_out);
        }
    }

    public static void right(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.right_in, R.anim.right_out);
        } else {
            activity.overridePendingTransition(R.anim.right_in, R.anim.right_out);
        }
    }

    public static void fade(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out);
        } else {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
