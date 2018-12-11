package com.example.pa_art.ramentimer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_RINGTONE_PICKER = 1;
    private final static int S_WAIT = 0;
    private final static int S_COUNT = 1;
    private final static int S_ALARM = 2;
    private final static int S_PAUSE = 3;
    private final static int TONE_TYPE = RingtoneManager.TYPE_ALL;

    Ringtone alarm_sound;
    Uri uri_title;
    String uri_str = "";
    TextView text_alarm;
    TextView text_min, text_sec;
    private int status = S_WAIT;
    private int t_min = 0, t_sec = 0, t_min_set = 0, t_sec_set = 0;
    private long t_msec_left = 0, t_msec_cnt = 0, t_msec_pause = 0, interval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set activity in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // set alarm textview
        text_alarm = findViewById(R.id.id_text_alarm);
        // set minute textview
        text_min = findViewById(R.id.id_text_min);
        // set second textview
        text_sec = findViewById(R.id.id_text_sec);

        // load alarm setting
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);
        uri_str = sharedPreferences.getString("TITLE_URI", null);

        // set default alarm sound
        if (uri_str == null) {
            uri_title = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL);
        } else {
            uri_title = Uri.parse(uri_str);
        }
        // set alarm sound and print textview
        alarm_sound = RingtoneManager.getRingtone(getApplicationContext(), uri_title);
        text_alarm.setText(alarm_sound.getTitle(getApplicationContext()));

        //

        // alarm select button
        Button b_sel = findViewById(R.id.id_button_alarm);
        b_sel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status != S_ALARM) {
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, TONE_TYPE);
                    startActivityForResult(intent, REQUEST_CODE_RINGTONE_PICKER);
                }
            }
        });

        // minute up bottun
        Button b_min_up = findViewById(R.id.id_button_min_up);
        b_min_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // only in S_WAIT
                if (status == S_WAIT) {
                    t_min_set++;
                    if (t_min_set > 99) {
                        t_min_set = 0;
                    }
                    // show minute in text_view
                    text_min.setText(String.format("%1$02d", t_min_set));
                }
            }
        });
        // minute down button
        Button b_min_down = findViewById(R.id.id_button_min_down);
        b_min_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // only in S_WAIT
                if (status == S_WAIT) {
                    t_min_set--;
                    if (t_min_set < 0) {
                        t_min_set = 99;
                    }
                    // show minute in text_view
                    text_min.setText(String.format("%1$02d", t_min_set));
                }
            }
        });
        // second up button
        Button b_sec_up = findViewById(R.id.id_button_sec_up);
        b_sec_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // only in S_WAIT
                if (status == S_WAIT) {
                    t_sec_set++;
                    if (t_sec_set > 59) {
                        t_sec_set = 0;
                    }
                    // show second in text view
                    text_sec.setText(String.format("%1$02d", t_sec_set));
                }
            }
        });
        // second down button
        Button b_sec_down = findViewById(R.id.id_button_sec_down);
        b_sec_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // only in S_WAIT
                if (status == S_WAIT) {
                    t_sec_set--;
                    if (t_sec_set < 0) {
                        t_sec_set = 59;
                    }
                    // show second in text view
                    text_sec.setText(String.format("%1$02d", t_sec_set));
                }
            }
        });


        // start button
        Button b_start = findViewById(R.id.id_button_start);
        b_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // t_msec_left
                switch (status) {
                    case S_WAIT:
                        t_msec_left = (t_min_set *60 + t_sec_set) * 1000;
                        break;
                    case S_PAUSE:
                        t_msec_left = t_msec_pause;
                        break;
                    default :
                        break;
                }
                // create countDown instance
                final CountDown countDown = new CountDown(t_msec_left, interval);
                // stop button
                Button b_stop = findViewById(R.id.id_button_stop);
                b_stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // if in S_COUNT status, change into S_PAUSE status and pause count down
                        if (status == S_COUNT) {
                            status = S_PAUSE;
                            t_msec_pause = (t_min * 60 + t_sec) * 1000;
                            countDown.cancel();
                        // else if in S_ALARM status, stop alarm and change into S_WAIT
                        } else if (status == S_ALARM) {
                            // set textView last set value
                            text_min.setText(String.format("%1$02d", t_min_set));
                            text_sec.setText(String.format("%1$02d", t_sec_set));
                            // stop alarm
                            alarm_sound.stop();
                            // set WAIT status
                            status = S_WAIT;
                        }
                    }
                });
                // reset button
                Button b_reset = findViewById(R.id.id_button_reset);
                b_reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // set status S_WAIT
                        status = S_WAIT;
                        // stop count down
                        countDown.cancel();
                        // clear set minute and second
                        t_min_set = t_sec_set = 0;
                        // set set minute and second to textView
                        text_min.setText(String.format("%1$02d", t_min_set));
                        text_sec.setText(String.format("%1$02d", t_sec_set));
                        // alarm stop
                        alarm_sound.stop();
                    }
                });
                if (t_msec_left != 0) {
                    // set count status as S_COUNT
                    status = S_COUNT;
                    // if in S_WAIT
                    countDown.start();
                }
            }
        });
/*
        // stop button
        Button b_stop = findViewById(R.id.id_button_stop);
        b_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == S_ALARM) {
                    // set textView last set value
                    text_min.setText(String.format("%1$02d", t_min_set));
                    text_sec.setText(String.format("%1$02d", t_sec_set));
                    // stop alarm
                    alarm_sound.stop();
                    // set count status
                    status = S_WAIT;
                }
            }
        });
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RINGTONE_PICKER) {
            if (resultCode == RESULT_OK) {
                uri_title = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                alarm_sound = RingtoneManager.getRingtone(this, uri_title);
                text_alarm.setText(alarm_sound.getTitle(this));
                SharedPreferences pref
                        = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("TITLE_URI", uri_title.toString());
                editor.commit();
            }
        }
    }

    // countdown timer task
    class CountDown extends CountDownTimer {

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        // action on finished
        @Override
        public void onFinish() {
            // set status as S_ALARM
            status = S_ALARM;
            // display zero in sec textView
            text_sec.setText(String.format("%1$02d", 0));
            // play alarm sound
            alarm_sound.play();
        }

        // action on interval
        @Override
        public void onTick(long millisUntilFinished) {
            // calculate last minute and second
            t_min = (int)(millisUntilFinished / 1000 / 60);
            t_sec = (int)(millisUntilFinished / 1000 % 60);
            // display minute and second in textView
            text_min.setText(String.format("%1$02d", t_min));
            text_sec.setText(String.format("%1$02d", t_sec));
        }
    }



}
