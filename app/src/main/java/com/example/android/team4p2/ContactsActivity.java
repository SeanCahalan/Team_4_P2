package com.example.android.team4p2;

import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ContactsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private ImageButton btnSpeak;
    private TextToSpeech tts;
    private String text;
    private String available_commands = "";
    private static final String[] commands = {"make", "help", "delete"};

    // We use global variables for the async stuff because I can't Java
    private String CURRENT_PROCESS = "DEFAULT";
    private String contact_name = "";
    private String contact_phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

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

        // Code here using the user input from the main activity if need be...

        tts = new TextToSpeech(this, this);
        Button btn = (Button) findViewById(R.id.btn);

        // This is a button to playback the string right now. Eventually this needs
        // to be replaced with voice commands.
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                speakOut();
                addContact(text, "adolf@fake.com", "6131234567");
                say("Added " + text + " to contacts.");
            }
        });

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
                CURRENT_PROCESS = "contact-name";
                say("Please say the name of the contact.");
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
                            addContact(contact_name, "junk@email.com", contact_phone);
                            say("Successfully added " + contact_name + " to contacts.");
                            break;
                        }
                        case "contact-delete-name": {
                            CURRENT_PROCESS = null;
                            deleteContact(text);
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

    private void addContact(String name, String email, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Phone Number
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "1").build());

        //Display name/Contact name
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        //Email details
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, "2").build());


        //Postal Address

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POBOX, "Postbox")

                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, "street")

                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, "city")

                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, "region")

                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, "postcode")

                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, "country")

                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, "3")

                .build());


        //Organization details
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE )
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, "Devindia")
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE )
                .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, "Developer")
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE )
                .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, "0")

                .build());
        //IM details
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Im.DATA, "adolf123")
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE )
                .withValue(ContactsContract.CommonDataKinds.Im.DATA5, "2")


                .build());
        try {
            ContentProviderResult[] res = getContentResolver().applyBatch(
                    ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void deleteContact(String name) {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String cName = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Log.d("TROY", cName);
                if (name.equalsIgnoreCase(cName)) {
                    ArrayList<ContentProviderOperation> ops =
                            new ArrayList<ContentProviderOperation>();
                    ops.add(ContentProviderOperation
                            .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                            .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", new String[] { id })
                            .build());
                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
