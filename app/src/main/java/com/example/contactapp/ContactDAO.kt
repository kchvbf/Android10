package com.example.contactapp


import androidx.room.*

@Dao
interface ContactDAO {
    @Delete
    suspend fun deleteContact(contact: Contact)




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long



    @Update
    suspend fun updateContact(contact: Contact)



    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<Contact>



}