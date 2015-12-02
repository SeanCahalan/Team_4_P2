package com.example.android.team4p2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class NotesActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private ImageButton btnSpeak;
    private TextToSpeech tts;
    private String text;
    private String available_commands = "";
    private static final String[] commands = {"make", "help", "delete", "listen", "list"};

    // We use global variables for the async stuff because I can't Java
    private String CURRENT_PROCESS = "DEFAULT";
    private String note_tag = "";
    private String note_content = "";

    private NoteManager noteMan;
    //private ListView theList;
    //private int notePosn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                say("Please say a command.");
                promptSpeechInput();
            }
        });

        Intent intent = getIntent();
        String user_input = intent.getStringExtra(MainActivity.USER_INPUT);

        tts = new TextToSpeech(this, this);

        // Set up `available_commands` for help command
        for (String command : commands) {
            available_commands = available_commands.concat(command + ", ");
        }
        available_commands = available_commands.trim();
        available_commands = available_commands.substring(0, available_commands.length());

        // Code here...
        noteMan = new NoteManager(getApplicationContext());
    }

    private void handleCommand() {
        // At this point a command has been issued and stored in the variable `text`
        String nText = MainActivity.normalizeCommand(text);
        String command = extractCommand(nText);
        switch (command) {
            case "delete": {
                CURRENT_PROCESS = "note-delete-title";
                say("Please say the title of the note.");
                promptSpeechInput();
                break;
            }
            case "ERROR": {
                say("That command is not valid. Say \"help\" to list available commands");
                return;
            }
            case "make": {
                CURRENT_PROCESS = "note-title";
                say("Please say the title of the note.");
                promptSpeechInput();
                break;
            }
            case "help": {
                say("The available commands are " + available_commands + ".");
                break;
            }
            case "listen": {
                CURRENT_PROCESS = "note-listen";
                say("Please say the title of the note you want to listen to.");
                promptSpeechInput();
                break;
            }
            case "list": {
                CURRENT_PROCESS = "DEFAULT";
                say("The notes are " + noteMan.getAllNotes() + ".");
//                say("This is not implemented yet.");
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
            case "note-title": {
                note_tag = text;
                CURRENT_PROCESS = "note-content";
                say("Please say the content to be recorded.");
                promptSpeechInput();
                break;
            }
            case "note-content": {
                note_content = text;
                CURRENT_PROCESS = "DEFAULT"; // Set the next process
                addNote(note_tag, note_content);
                say("Successfully added " + note_tag + " to notes.");
                break;
            }
            case "note-listen": {
                CURRENT_PROCESS = "DEFAULT";
                String result = noteMan.getTheDamnNote(text);
                if (result == null) {
                    say("The note " + text + " is not in the database.");
                } else {
                    say(result);
                }
                break;
            }
            /*
            case "additional-content": {
                String command = MainActivity.normalizeCommand(text);
                if (command.equalsIgnoreCase("no")) {
                    CURRENT_PROCESS = "add-note";
                    nextProcess();
                } else if (command.equalsIgnoreCase("yes")) { //fix this
                    CURRENT_PROCESS = "additional-content";
                    say("Please say 'done'  or 'add.'");
                    promptSpeechInput();
                } else if (command.equalsIgnoreCase("done")) {
                    CURRENT_PROCESS = "add-note";
                    nextProcess();
                } else if (command.equalsIgnoreCase("make")) {
                    CURRENT_PROCESS = "note-add-content";
                    say("Please say the content to be added.");
                    promptSpeechInput();
                } else {
                    CURRENT_PROCESS = "additional-content";
                    say("That command was not valid. Please say \"yes\", \"no\", \"done\" or \"add.\"");
                    promptSpeechInput();
                }
                break;
            }

            case "add-note": {
                CURRENT_PROCESS = "DEFAULT"; // Set the next process
                addNote(note_tag, note_content);
                say("Successfully added " + note_tag + " to notes.");
                break;
            }
            */
            case "note-delete-title": {
                CURRENT_PROCESS = "DEFAULT";
                if (deleteNote(text)) {
                    say("Deleted " + text + " from notes.");
                } else {
                    say("Could not delete note " + text + ".");
                }
                break;
            }
            default: {
                handleCommand();
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
        while (tts.isSpeaking()) {
        }
    }

    private String extractCommand(String user_sentence) {
        for (String command : commands) {
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

    private void addNote(String tag, String content){
        Note inputNote = new Note();
        inputNote.setNoteText(content);
        long addedID = noteMan.addNewNote(inputNote, tag);
        inputNote.setNoteID(addedID);
    }

    public Boolean deleteNote(String tag){
        return noteMan.deleteNote(tag);
    }
}
