package com.example.contactapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface NoteDAO {


    @Delete
    suspend fun deleteNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)



    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>



    @Query("SELECT id FROM notes WHERE contactId =:contactId")
    suspend fun getNoteIdByContactId(contactId: Long): Long?




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long



    @Update
    suspend fun updateNote(note: Note)



    @Query("SELECT * FROM notes WHERE contactId = :contactId")
    suspend fun getByContactId(contactId: Long) : Note?




}