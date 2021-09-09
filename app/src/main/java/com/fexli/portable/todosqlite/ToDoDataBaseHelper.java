package com.fexli.portable.todosqlite;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.fexli.portable.todosqlite.models.ToDoTask;

import java.util.ArrayList;
import java.util.List;

public class ToDoDataBaseHelper extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String TAG = "ToDoDatabaseHelper";
    public String TABLE_NAME;
    private SQLiteDatabase cur_db;

    public ToDoDataBaseHelper(Context ctx, String name) {
        super(ctx, name, null, VERSION);
        this.TABLE_NAME = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate() called");
        String sql = "create table " + this.TABLE_NAME + ToDoTask.CREATE_TABLE_INFO;
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + this.TABLE_NAME);
        onCreate(db);
    }

    public ToDoDataBaseHelper openDataBase() {
        cur_db = this.getWritableDatabase();
        return this;
    }

    public ToDoDataBaseHelper closeDataBase() {
        cur_db = this.getWritableDatabase();
        return this;
    }

    public void insertTask(ToDoTask task) {
        ContentValues ctx = new ContentValues();
        task.fillContent(ctx);
        cur_db.insert(this.TABLE_NAME, null, ctx);
    }

    public List<ToDoTask> getAllTasks() {
        List<ToDoTask> taskList = new ArrayList<>();
        cur_db.beginTransaction();
        try (Cursor cur = cur_db.query(this.TABLE_NAME, null, null, null, null, null, null)) {
            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        taskList.add(new ToDoTask(cur.getInt(cur.getColumnIndex(ToDoTask.ID)),
                                cur.getString(cur.getColumnIndex(ToDoTask.NAME)),
                                cur.getString(cur.getColumnIndex(ToDoTask.DESCRIPTION)),
                                cur.getInt(cur.getColumnIndex(ToDoTask.DONE_TS)),
                                cur.getString(cur.getColumnIndex(ToDoTask.CATEGORY)),
                                cur.getInt(cur.getColumnIndex(ToDoTask.IS_EMERGENCY)) != 0,
                                cur.getInt(cur.getColumnIndex(ToDoTask.IS_FINISHED)) != 0));
                    } while (cur.moveToNext());
                }
            }
        } finally {
            cur_db.endTransaction();
        }
        return taskList;
    }


    public boolean find(String column, int value) {
        boolean result = false;
        Cursor cursor = cur_db.query(this.TABLE_NAME, new String[]{column}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            if (value == cursor.getInt(0)) {
                result = true;
            }
        }
        cursor.close();
        return result;
    }

    public boolean find(String column, String value) {
        boolean result = false;
        Cursor cursor = cur_db.query(this.TABLE_NAME, new String[]{column}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            if (value.equals(cursor.getString(0))) {
                result = true;
            }
        }
        cursor.close();
        return result;
    }

    public int findByName(String name) {
        return findByName(name, false, false);
    }

    public int findByName(String name, boolean isEmergency) {
        return findByName(name, isEmergency, false);
    }

    public int findByName(String name, boolean isEmergency, boolean isFinished) {
        int result = -1;
        Cursor cursor = cur_db.query(this.TABLE_NAME, new String[]{"name", "emerg", "finished", "_id"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            if (name.equals(cursor.getString(0)) && !(isFinished && cursor.getInt(2) == 0) && !(isEmergency && cursor.getInt(1) == 0)) {
                result = cursor.getInt(3);
            }
        }
        cursor.close();
        return result;
    }

    public void deleteByName(String name) {
        cur_db.delete(this.TABLE_NAME, "name = " + name, null);
    }
}