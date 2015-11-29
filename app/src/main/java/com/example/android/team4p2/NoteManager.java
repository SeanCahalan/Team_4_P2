package com.example.android.team4p2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class NoteManager {
    private SQLiteOpenHelper noteHelper;
    private SQLiteDatabase noteDB;

    //array that maps id to tags

    public NoteManager(Context context){
        noteHelper = new MyDataHelper(context);
    }

    public String getTheDamnNote(String text){
        noteDB = noteHelper.getWritableDatabase();
        String query = "SELECT * FROM notes WHERE title = '" + text + "'";
        Cursor c = noteDB.rawQuery(query, null);
        if (c.getCount() == 0)
            return null;
        c.moveToNext();
        String ret = c.getString(c.getColumnIndex("note"));
        c.close();
        return ret;
    }

    public String getAllNotes() {
        noteDB = noteHelper.getWritableDatabase();
        String query = "SELECT * FROM notes";
        Cursor c = noteDB.rawQuery(query, null);
        String titles = "";
        while (c.moveToNext()) {
            titles += ", " + c.getString(c.getColumnIndex("title"));
        }
        c.close();
        noteDB.close();
        return titles;
    }

    public Note getNoteYes(long ID){
        int index=0;
        List<Note> notes = new ArrayList<Note>();
        noteDB = noteHelper.getReadableDatabase();
        Cursor noteCursor = noteDB.query(MyDataHelper.DBItem.TABLE,
                null, null, null, null, null, null);
        while(noteCursor.moveToNext()){
            int newID = noteCursor.getInt(
                    noteCursor.getColumnIndexOrThrow(MyDataHelper.DBItem._ID));
            String newText = noteCursor.getString(
                    noteCursor.getColumnIndexOrThrow(MyDataHelper.DBItem.NOTE_COL));
            Note newNote = new Note();
            newNote.setNoteID(newID);
            newNote.setNoteText(newText);
            notes.add(newNote);
            if(newID == (int)ID)
                index = notes.indexOf(newNote);
        }
        noteCursor.close();
        noteDB.close();
        return notes.get(index);
    }

    public List<Note> getNotes(){
        List<Note> notes = new ArrayList<Note>();
        noteDB = noteHelper.getReadableDatabase();
        Cursor noteCursor = noteDB.query(MyDataHelper.DBItem.TABLE,
                null, null, null, null, null, null);
        while(noteCursor.moveToNext()){
            int newID = noteCursor.getInt(
                    noteCursor.getColumnIndexOrThrow(MyDataHelper.DBItem._ID));
            String newText = noteCursor.getString(
                    noteCursor.getColumnIndexOrThrow(MyDataHelper.DBItem.NOTE_COL));
            Note newNote = new Note();
            newNote.setNoteID(newID);
            newNote.setNoteText(newText);
            notes.add(newNote);
        }
        noteCursor.close();
        noteDB.close();
        return notes;
    }

    public long addNewNote(Note addedNote, String tag){
        noteDB = noteHelper.getWritableDatabase();
        ContentValues noteValues = new ContentValues();
        noteValues.put
                (MyDataHelper.DBItem.NOTE_COL, addedNote.getNoteText());
        noteValues.put
                (MyDataHelper.DBItem.TITLE_COL, tag);
        long added = noteDB.insertOrThrow
                (MyDataHelper.DBItem.TABLE, null, noteValues);
        noteDB.close();
        return added;
    }

    public int deleteNote(long noteID){
        noteDB = noteHelper.getWritableDatabase();
        String[] params = {""+noteID};
        int deleted = noteDB.delete(MyDataHelper.DBItem.TABLE, MyDataHelper.DBItem._ID+" = ?", params);
        noteDB.close();
        return deleted;
    }


}
