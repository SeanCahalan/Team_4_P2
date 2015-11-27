package com.example.android.team4p2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class AlarmsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private ImageButton btnSpeak;
    private TextToSpeech tts;
    private String text;
    private String available_commands = "";
    private static final String[] commands = {"make", "help", "delete"};
    private String CURRENT_PROCESS = "DEFAULT";

    PendingIntent pi;
    BroadcastReceiver br;
    AlarmManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        setup();

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                say("Please say a command.");
                promptSpeechInput();
            }
        });

        tts = new TextToSpeech(this, this);

        // Set up `available_commands` for help command
        for (String command: commands) {
            available_commands = available_commands.concat(command + ", ");
        }
        available_commands = available_commands.trim();
        available_commands = available_commands.substring(0, available_commands.length());
    }

    /*
     *  This is the "top" of the process, where we begin to interpret the commands.
     */
    private void handleCommand() {
        // At this point a command has been issued and stored in the variable `text`
        String nText = MainActivity.normalizeCommand(text);
        String command = extractCommand(nText);
        switch (command) {
            case "delete": {
                CURRENT_PROCESS = "contact-delete-name";
                say("Please say the name of the contact.");
                promptSpeechInput();
                break;
            }
            case "ERROR": {
                say("That command is not valid. Say \"help\" to list available commands");
                return;
            }
            case "make": {
                CURRENT_PROCESS = "alarm-make";
                say("What time should the alarm be set for?");
                promptSpeechInput();
                break;
            }
            case "help": {
                say("The available commands are " + available_commands + ".");
                break;
            }
        }
    }

    /**
     * Receiving speech input
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MainActivity.REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text = result.get(0);
                    nextProcess();
                }
                break;
            }
        }
    }

    private void nextProcess() {
        switch (CURRENT_PROCESS) {
            case "alarm-make": {
                CURRENT_PROCESS = "DEFAULT";
                addAlarm();
                say("Successfully added your alarm for " + text);
                break;
            }
            default: {
                handleCommand();
                break;
            }
        }
    }

    private void addAlarm() {

        // int current_hour = cal.get(Calendar.HOUR_OF_DAY);
        int user_hour = getHour(text);
        // int current_minutes = cal.get(Calendar.MINUTE);
        int user_minutes = getMinutes(text);
        // long needed_time = ((24 - Math.abs(user_hour - current_hour) * 60 + (60 - Math.abs(user_minutes - current_minutes)))) * 60 * 1000;
        // long final_time = System.currentTimeMillis() + needed_time;
        Boolean user_isPm = getIsPM(text);

        if (user_hour == 12 && !user_isPm)
            user_hour = 0;
        else if (user_hour < 12 && user_isPm) {
            user_hour += 12;
        }

        Log.d("TROY", "user_hour: " + user_hour);
        Log.d("TROY", "user_minutes: " + user_minutes);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.CANADA);

        // Initialize a new calendar with the right info (don't pass it anything)
        Calendar cal = new GregorianCalendar();

        // Set the calendar's time to now
        cal.setTime(new Date());

        // Calculate the time values we'll need to add to the calendar
        int hours_to_add = Math.abs(user_hour - cal.get(Calendar.HOUR_OF_DAY));
        int minutes_to_add = Math.abs(user_minutes - cal.get(Calendar.MINUTE));

        Log.d("TROY", "hours_to_add: " + hours_to_add);
        Log.d("TROY", "minutes_to_add: " + minutes_to_add);

        // Add them to the calendar
        cal.add(Calendar.HOUR_OF_DAY, hours_to_add);
        cal.add(Calendar.MINUTE, minutes_to_add);
        cal.add(Calendar.SECOND, -cal.get(Calendar.SECOND));

        // Get the new calendar time in milliseconds
        long final_time = cal.getTimeInMillis();
        String alarmTime = sdf.format(final_time);
        Log.d("TROY", alarmTime);

        AlarmManager.AlarmClockInfo alarm_info = new AlarmManager.AlarmClockInfo(final_time, pi);
        am.setAlarmClock(alarm_info, pi);
    }

    private int getHour(String date) {
        return parseInt(date.split(":")[0]);
    }

    private int getMinutes(String date) {
        return parseInt(date.split(":")[1].split(" ")[0]);
    }

    private Boolean getIsPM(String date) {
        return date.toLowerCase().contains("p");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CANADA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void say(String str) {
        tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, "asdfsdfsd");
        while(tts.isSpeaking()){}
    }

    private String extractCommand(String user_sentence) {
        for (String command: commands) {
            if (MainActivity.sentenceContainsWord(user_sentence, command)) {
                return command;
            }
        }
        return "ERROR";
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, MainActivity.REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    private void setup() {
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent it = new Intent(getApplicationContext(), audioService.class);
                it.setAction("com.example.android.team4p2");
                Toast.makeText(
                        getApplicationContext(),
                        "JOHHHHHHNNNNNNN CENAAAAAAA",
                        Toast.LENGTH_LONG
                ).show();
                startService(it);
            }
        };
        registerReceiver(br, new IntentFilter("com.example.android.team4p2"));
        pi = PendingIntent.getBroadcast(this, 0, new Intent("com.example.android.team4p2"), 0);
        am = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
    }
}
