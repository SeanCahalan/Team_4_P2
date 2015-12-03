package com.example.android.team4p2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private ImageButton btnSpeak;

    public final static int REQ_CODE_SPEECH_INPUT = 100;
    public final static String USER_INPUT = "com.example.android.team4p2.USER_INPUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                say("Please say a command.");
                promptSpeechInput();
            }
        });

    }

    /*
     *  Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     *  Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    handleInput(text);

                }
                break;
            }
        }

    }

    /*
     *  We create a hash map to map synonyms to a single word that will be interpreted by our
     *  command interpreter.
     */
    public static final Map<String, String> keywordMap;
    static
    {
        keywordMap = new HashMap<>();
        keywordMap.put("make", "make");
        keywordMap.put("create", "make");
        keywordMap.put("add", "make");
        keywordMap.put("new", "make");
        keywordMap.put("contacts", "contact");
        keywordMap.put("contact", "contact");
        keywordMap.put("note", "note");
        keywordMap.put("notes", "note");
        keywordMap.put("alarm", "alarm");
        keywordMap.put("alarms", "alarm");
        keywordMap.put("help", "help");
        keywordMap.put("delete", "delete");
        keywordMap.put("remove", "delete");
        keywordMap.put("no", "no");
        keywordMap.put("No", "no");
        keywordMap.put("nope", "no");
        keywordMap.put("know", "no");
        keywordMap.put("email", "email");
        keywordMap.put("job", "job");
        keywordMap.put("title", "job");
        keywordMap.put("company", "company");
        keywordMap.put("name", "name");
        keywordMap.put("cancel", "cancel");
        keywordMap.put("list", "list");
        keywordMap.put("yes", "yes");
        keywordMap.put("list", "list");
        keywordMap.put("listen", "listen");
        keywordMap.put("done", "done");
    }

    private static final String[] categories = {"contact", "note", "alarm"};

    public static String keywordConvert(String str) {
        return keywordMap.get(str);
    }

    public static String normalizeCommand(String command) {
        String[] user_input_list = command.split(" ");
        String well_formed_user_input = "";
        for (String word: user_input_list) {
            String well_formed_word = keywordConvert(word);
            if (well_formed_word != null)
                well_formed_user_input = well_formed_user_input.concat(well_formed_word + " ");
        }
        return well_formed_user_input.trim();
    }

    /*
     *  This is our "interpreter" for the user input string. There are two steps to it.
     *
     *    1. Convert the user string into a well-formed string using the keywordConvert function.
     *    2. Interpret the string and call the appropriate Intent.
     */
    private void handleInput(String user_input) {
        String[] user_input_list = user_input.split(" ");
        String well_formed_user_input = "";
        Boolean flag = false;
        String chosen_category = null;
        for (String word: user_input_list) {
            String well_formed_word = keywordConvert(word);
            well_formed_user_input = well_formed_user_input.concat(well_formed_word + " ");
            for (String category: categories) {
                if (category.equals(well_formed_word)) {
                    flag = true;
                    chosen_category = category;
                }
            }
        }
        well_formed_user_input = well_formed_user_input.trim();
        if (flag) {
            launchActivity(chosen_category, well_formed_user_input);
        } else {
            say("That command is not valid. The available commands are 'alarm,' 'contact," +
                    "' and 'note.'");
        }
    }

    private void launchActivity(String category, String user_input) {
        Intent intent;
        switch (category) {
            case "contact":
                intent = new Intent(this, ContactsActivity.class);
                break;
            case "note":
                intent = new Intent(this, NotesActivity.class);
                break;
            case "alarm":
                intent = new Intent(this, AlarmsActivity.class);
                break;
            default:
                throw new Error("This shouldn't happen. An invalid category was passed " +
                        "to launchActivity");
        }
        intent.putExtra(USER_INPUT, user_input);
        startActivity(intent);
    }

    public static Boolean sentenceContainsWord(String sentence, String word_to_match) {
        String[] sentence_arr = sentence.split(" ");
        for (String word: sentence_arr) {
            if (word.equals(word_to_match)) return true;
        }
        return false;
    }

    private void say(String str) {
        tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, "asdfsdfsd");
        while(tts.isSpeaking()){}
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CANADA);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
            }
        }
    }
}
