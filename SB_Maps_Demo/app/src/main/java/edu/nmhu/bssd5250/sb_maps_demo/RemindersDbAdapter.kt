package edu.nmhu.bssd5250.sb_maps_demo

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class RemindersDbAdapter     // Constructor
    (private val mCtx: Context) {
    private var mDbHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase? = null

    // Open the database
    @Throws(SQLException::class)
    fun open() {
        mDbHelper = DatabaseHelper(mCtx)
        mDb = mDbHelper!!.writableDatabase
    }

    // Close the database
    fun close() {
        if (mDbHelper != null) {
            mDbHelper!!.close()
        }
    }

    // Make a new reminder
    fun createReminder(name: String?, important: Boolean) {
        val values = ContentValues()
        values.put(COL_CONTENT, name)
        values.put(COL_IMPORTANT, if (important) 1 else 0)
        mDb!!.insert(TABLE_NAME, null, values)
    }

    //  Same as above but includes reminder
    fun createReminder(reminder: Reminder): Long {
        val values = ContentValues()
        values.put(COL_CONTENT, reminder.content) // Contact Name
        values.put(COL_IMPORTANT, reminder.important) // Contact Phone Number
        // Inserting Row
        return mDb!!.insert(TABLE_NAME, null, values)
    }

    //  Fetch a single reminder
    fun fetchReminderById(id: Int): Reminder {
        val cursor = mDb!!.query(
            TABLE_NAME, arrayOf(
                COL_ID,
                COL_CONTENT, COL_IMPORTANT
            ), COL_ID + "=?", arrayOf(id.toString()), null, null, null, null
        )
        cursor?.moveToFirst()
        return Reminder(
            cursor!!.getInt(INDEX_ID),
            cursor.getString(INDEX_CONTENT),
            cursor.getInt(INDEX_IMPORTANT)
        )
    }

    // Get all reminders
    fun fetchAllReminders(): Cursor? {
        val mCursor = mDb!!.query(
            TABLE_NAME, arrayOf(
                COL_ID,
                COL_CONTENT, COL_IMPORTANT
            ),
            null, null, null, null, null
        )
        mCursor?.moveToFirst()
        return mCursor
    }

    //  Edit a reminder
    fun updateReminder(reminder: Reminder) {
        val values = ContentValues()
        values.put(COL_CONTENT, reminder.content)
        values.put(COL_IMPORTANT, reminder.important)
        mDb!!.update(
            TABLE_NAME, values,
            COL_ID + "=?", arrayOf(java.lang.String.valueOf(reminder.id))
        )
    }

    // Remove a rminder
    fun deleteReminderById(nId: Int) {
        mDb!!.delete(TABLE_NAME, COL_ID + "=?", arrayOf(nId.toString()))
    }

    fun deleteAllReminders() {
        mDb!!.delete(TABLE_NAME, null, null)
    }

    // SQLLITE inner class database helper
    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            Log.w(TAG, DATABASE_CREATE)
            db.execSQL(DATABASE_CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(
                TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
            )
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
            onCreate(db)
        }
    }

    companion object {
        // Columns
        const val COL_ID = "_id"
        const val COL_CONTENT = "content"
        const val COL_IMPORTANT = "important"

        // Indicies
        const val INDEX_ID = 0
        const val INDEX_CONTENT = INDEX_ID + 1
        const val INDEX_IMPORTANT = INDEX_ID + 2

        // Logging parameters
        private const val TAG = "RemindersDbAdapter"
        private const val DATABASE_NAME = "dba_remdrs"
        private const val TABLE_NAME = "tbl_remdrs"
        private const val DATABASE_VERSION = 1

        // SQLLite stuff
        private const val DATABASE_CREATE = "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                COL_CONTENT + " TEXT, " +
                COL_IMPORTANT + " INTEGER );"
    }
}