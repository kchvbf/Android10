package com.example.contactapp

import android.content.Context


class NoteRep(context: Context) {

    private val noteDAO: NoteDAO = ContactAppDatabase.getDatabase(context).noteDAO()

    suspend fun getAllNotes(): List<Note> {
        return noteDAO.getAllNotes();
    }

    suspend fun insertAll(notes: List<Note>) {
        return noteDAO.insertAll(notes);
    }

    suspend fun insertNote(note: Note): Long {
        return noteDAO.insertNote(note)
    }

    suspend fun updateNote(note: Note){
        return noteDAO.updateNote(note)
    }

    suspend fun deleteNote(note: Note){
        return noteDAO.deleteNote(note);
    }

    suspend fun getNoteByContactId(contactId: Long) : Note?{
        return noteDAO.getByContactId(contactId)
    }

    suspend fun getNoteIdByContactId(contactId: Long): Long? {
        return noteDAO.getNoteIdByContactId(contactId)
    }
}