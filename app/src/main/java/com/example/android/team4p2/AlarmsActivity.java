package com.example.android.team4p2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class AlarmsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private ImageButton btnSpeak;
    private TextToSpeech tts;
    private String text;
    private String available_commands = "";
    private static final String[] commands = {"new", "help", "cancel"};

    private String CURRENT_PROCESS = "DEFAULT";
    private String contact_name = "";
    private String contact_phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                say("Please say a command.");
                promptSpeechInput();
            }
        });


        // Receive the Intent
        Intent intent = getIntent();
        String user_input = intent.getStringExtra(MainActivity.USER_INPUT);

        // Code here using the user input from the main activity (if needed)
        tts = new TextToSpeech(this, this);

        // Set up `available_commands` for help command
        for (String command : commands) {
            available_commands = available_commands.concat(command + ", ");
        }
        available_commands = available_commands.trim();
        available_commands = available_commands.substring(0, available_commands.length());
    }

    private void handleCommand() {
        // At this point a command has been issued and stored in the variable `text`
        String nText = MainActivity.normalizeCommand(text);
        String command = extractCommand(nText);
        switch (command) {
            case "cancel": {
                CURRENT_PROCESS = "///";
                say("/////.");
                promptSpeechInput();
                break;
            }
            case "ERROR": {
                say("That command is not valid. Say \"help\" to list available commands");
                return;
            }
            case "new": {
                CURRENT_PROCESS = "////";
                say("///.");
                promptSpeechInput();
                break;
            }
            case "help": {
                say("The available commands are " + available_commands + ".");
                break;
            }
        }
    }

    // Receiving speech input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MainActivity.REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text = result.get(0);

                    /*
                     *  I don't know how to do this without callbacks, and I can't use callbacks
                     *  because Java.
                     */
                    switch (CURRENT_PROCESS) {
                        case "contact-name": {
                            contact_name = text;
                            CURRENT_PROCESS = "contact-phone"; // Set the next process
                            say("Please say the phone number of the contact.");
                            promptSpeechInput();
                            break;
                        }
                        case "contact-phone": {
                            contact_phone = text;
                            CURRENT_PROCESS = "add-contact"; // Set the next process
                            promptSpeechInput();
                            break;
                        }
                        case "add-contact": {
                            CURRENT_PROCESS = null; // Set the next process
                            // addContact(contact_name, "junk@email.com", contact_phone);
                            say("Successfully added " + contact_name + " to contacts.");
                            break;
                        }
                        case "contact-delete-name": {
                            CURRENT_PROCESS = null;
                             // deleteContact(text);
                            say("Hopefully deleted " + text + " from contacts.");
                            break;
                        }
                        default: {
                            handleCommand();
                            break;
                        }
                    }
                }
                break;
            }
        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CANADA);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                speakOut();
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

    private void speakOut() {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aslkjfds");
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
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
//now inserting the make new alarm and cancel alarm functions (reffer to troys code for general practice)

