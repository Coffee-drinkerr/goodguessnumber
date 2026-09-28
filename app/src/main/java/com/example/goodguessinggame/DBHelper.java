package com.example.goodguessinggame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GoodGuessingGame.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Auth table for login & registration
        db.execSQL("CREATE TABLE IF NOT EXISTS users_auth (username TEXT PRIMARY KEY, password TEXT NOT NULL)");

        // High score table
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, score INTEGER, level INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users_auth");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // --- Authentication Methods ---

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if username already exists
        Cursor cursor = db.rawQuery("SELECT * FROM users_auth WHERE username = ?", new String[]{username});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false; // Username taken
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        long result = db.insert("users_auth", null, values);
        return result != -1;
    }

    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users_auth WHERE username = ? AND password = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // --- Score Methods ---

    public ArrayList<ModelUser> genericSelectByUserName(String name) {
        ArrayList<ModelUser> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE name = ?", new String[]{name});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
                userList.add(new ModelUser(userName, score, level));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userList;
    }

    public void insert(ModelUser user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("score", user.getScore());
        values.put("level", user.getLevel());
        db.insert("users", null, values);
    }

    public void update(ModelUser user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("score", user.getScore());
        values.put("level", user.getLevel());
        db.update("users", values, "name = ?", new String[]{user.getName()});
    }
}