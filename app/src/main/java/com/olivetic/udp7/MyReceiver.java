package com.olivetic.udp7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() { }
    @Override public void onReceive(Context context, Intent intent)
    { Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(activityIntent);
    }
}