package com.ajiew.autopunchding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ajiew.autopunchding.event.PunchFinishedEvent;
import com.ajiew.autopunchding.event.PunchType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private TextView tvClockInTime;

    private TextView tvClockOutTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvClockInTime = findViewById(R.id.tv_clock_in_time);
        tvClockOutTime = findViewById(R.id.tv_clock_out_time);

        startService(new Intent(this, AutoRunService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void requestRoot(View view) {
        String cmd = "input tap 200 200 \n";
        try {
            OutputStream os = Runtime.getRuntime().exec("su").getOutputStream();
            os.write(cmd.getBytes());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPunchFinished(PunchFinishedEvent event) {
        String text = "";
        if (event.getPunchType() == PunchType.CLOCK_IN) {
            text = getString(R.string.punch_clock_in_time, event.getTime());
            tvClockInTime.setText(text);
        } else if (event.getPunchType() == PunchType.CLOCK_OUT) {
            text = getString(R.string.punch_clock_out_time, event.getTime());
            tvClockOutTime.setText(text);
        }

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
