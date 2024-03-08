package com.example.contactapp

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ContactRep(context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver
    private val contactDAO: ContactDAO = ContactAppDatabase.getDatabase(context).contactDAO()

    suspend fun importContacts() {
        withContext(Dispatchers.IO) {
            val contactsFromProvider = queryContactsFromProvider()
            saveContactsToDatabase(contactsFromProvider)
        }
    }

    @SuppressLint("Range")
    private fun queryContactsFromProvider(): List<Contact> {
        val contactsList = mutableListOf<Contact>()

        val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val contact = Contact(id, name, number)
                contactsList.add(contact)
            }
        }
        return contactsList
    }

    private suspend fun saveContactsToDatabase(contacts: List<Contact>) {
        contactDAO.insertAll(contacts)
    }

    suspend fun getAllContacts(): List<Contact> {
        return contactDAO.getAllContacts()
    }

}