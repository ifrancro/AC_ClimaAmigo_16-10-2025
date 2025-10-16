package com.example.climaamigo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ClimateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.climaamigo.UPDATE_CLIMA".equals(intent.getAction())) {
            String clima = intent.getStringExtra("clima");
            Toast.makeText(context, "Broadcast: " + clima, Toast.LENGTH_SHORT).show();
        }
    }
}
