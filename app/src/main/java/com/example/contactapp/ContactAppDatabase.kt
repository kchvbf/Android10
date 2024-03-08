package com.example.contactapp


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



@Database(entities = [Contact::class, Note::class], version = 1, exportSchema = false)
abstract class ContactAppDatabase : RoomDatabase() {


    abstract fun contactDAO(): ContactDAO

    abstract fun noteDAO(): NoteDAO

    companion object {

        @Volatile
        private var INSTANCE: ContactAppDatabase? = null


        fun getDatabase(context: Context): ContactAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactAppDatabase::class.java,
                    "contact_app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}