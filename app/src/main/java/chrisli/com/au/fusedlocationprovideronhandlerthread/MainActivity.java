package chrisli.com.au.fusedlocationprovideronhandlerthread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvLocation_;
    private int count_ = 0;
    private BroadcastReceiver broadcastReceiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(LocationUpdateService.IntentActionLocationUpdate)) {
                if (tvLocation_ != null) {
                    Location location = intent.getParcelableExtra(LocationUpdateService.IntentExtraLocationUpdateNewLocation);
                    if (location != null) {
                        tvLocation_.setText(++count_ + ": " + location.toString());
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocation_ = (TextView) findViewById(R.id.tvLocation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        count_ = 0;
        startService(new Intent(this, LocationUpdateService.class));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver_, new IntentFilter(LocationUpdateService.IntentActionLocationUpdate));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver_);
        //stop the service
        Intent intent = new Intent(this, LocationUpdateService.class);
        intent.putExtra(LocationUpdateService.IntentExtraLocationUpdateStopService, true);
        startService(intent);
    }
}
