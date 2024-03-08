package com.example.contactapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.contactapp.ui.theme.ContactAppTheme
import com.example.contactapp.R


import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var contactRep: ContactRep
    private lateinit var noteRep: NoteRep


    private var notes by mutableStateOf(emptyList<Note>())
    private var contacts by mutableStateOf(emptyList<Contact>())
    private var isContactsLoaded by mutableStateOf(false)

    private var isNotesLoaded by mutableStateOf(false)
    private var isEditDialogActive by mutableStateOf(false)
    private var selectedContactId by mutableStateOf(0L)

    private val READ_CONTACTS_PERMISSION_REQUEST_CODE = 1001
    private val WRITE_CONTACTS_PERMISSION_REQUEST_CODE = 1002



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contactRep = ContactRep(this)
        noteRep = NoteRep(this)

        loadContacts()
        loadNotes()

        setContent {

            ContactAppTheme(
                content = {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFE0FFFF)
                    ) {
                        ContactsScreen()
                    }
                }
            )
        }
        checkAndRequestContactsPermission()
    }


    private fun loadContacts() {
        if (!isContactsLoaded) {
            lifecycleScope.launch {
                contactRep.importContacts()
                contacts = contactRep.getAllContacts()
            }
            isContactsLoaded = true
        }
    }

    private fun loadNotes(){
        if(!isNotesLoaded){
            lifecycleScope.launch {
                notes = noteRep.getAllNotes()
            }
            isNotesLoaded = true;
        }
    }

    private fun checkAndRequestContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_REQUEST_CODE
            )
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_CONTACTS),
                    WRITE_CONTACTS_PERMISSION_REQUEST_CODE
                )
            } else {
                loadContacts()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_CONTACTS_PERMISSION_REQUEST_CODE,
            WRITE_CONTACTS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    @Composable
    private fun ContactsScreen() {
        Text(

            text = stringResource(R.string.contacts_text_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 30.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,

            )
        Column {

            LazyColumn(

                modifier = Modifier
                    .padding(20.dp)
                    .weight(1f)

            ) {
                item {

                    Spacer(modifier = Modifier.height(60.dp))
                }

                items(contacts.size) { index ->

                    val contact = contacts[index]

                    val note = notes.find { note -> note.contactId == contact.id }

                    ContactItem(
                        contact = contact,
                        note = note,
                        onEditBtnClick = {

                            isEditDialogActive = true
                            selectedContactId = contact.id

                        },

                        onDeleteBtnClick = {
                            lifecycleScope.launch {
                                val note = noteRep.getNoteByContactId(contact.id)
                                if(note != null){
                                    noteRep.deleteNote(note)
                                }
                                notes = noteRep.getAllNotes()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                }
            }

            if (isEditDialogActive) {
                val note = notes.find { note -> note.contactId == selectedContactId }

                EditNoteDialog (

                    oldText = note?.desc ?: stringResource(R.string.no_note_text),
                    onCancelBtnClick = {
                        isEditDialogActive = false
                    },

                    onSaveBtnClick = { newText: String ->
                        lifecycleScope.launch {

                            val id = noteRep.getNoteIdByContactId(selectedContactId)
                            if(id != null){

                                val editedNote = Note(
                                    id = id,
                                    desc = newText,
                                    contactId = selectedContactId
                                )
                                noteRep.updateNote(editedNote)
                            }
                            else{

                                val note = Note(
                                    desc = newText,
                                    contactId = selectedContactId
                                )

                                noteRep.insertNote(note)
                            }
                            notes = noteRep.getAllNotes();
                        }
                        isEditDialogActive = false
                    }
                )
            }
        }
    }

    @Composable
    private fun ContactRow(
        textLabel: String,
        textInfo: String
    ){

        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
        ){
            Text(
                text = textInfo,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text= textLabel,
                fontSize = 20.sp

            )
        }

    }



    @Composable
    private fun ContactItem(
        contact: Contact,
        note: Note?,
        onEditBtnClick: () -> Unit,
        onDeleteBtnClick: () -> Unit
    ) {

        val desc: String = note?.desc ?: stringResource(R.string.no_note_text)


        Card(
            colors = CardDefaults.cardColors(Color(0xFFD8BFD8)),
            shape = RoundedCornerShape(15.dp)

        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 15.dp)

            ) {

                ContactRow(
                    textInfo = stringResource(R.string.name_label),
                    textLabel = contact.name
                )
                ContactRow(
                    textInfo = stringResource(R.string.phone_label),
                    textLabel = contact.phone
                )
                ContactRow(
                    textInfo = stringResource(R.string.note_label),
                    textLabel = desc
                )


                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),

                    horizontalArrangement = Arrangement.SpaceAround

                ) {

                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFF4682B4)),
                        onClick = onEditBtnClick

                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFFE0FFFF)
                        )

                    }

                    Button(
                        colors = ButtonDefaults.buttonColors(Color(0xFF4682B4)),
                        onClick = onDeleteBtnClick

                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFE0FFFF)
                        )
                    }
                }

                Divider(
                    color = Color(0xFF4682B4),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }


    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditNoteDialog(
        oldText: String,
        onCancelBtnClick: () -> Unit,
        onSaveBtnClick: (newNote: String) -> Unit

    ) {

        var newNote by remember { mutableStateOf(TextFieldValue(oldText)) }


        AlertDialog(

            onDismissRequest = { onCancelBtnClick() },


            title = { Text(stringResource(R.string.edit_note_dialog_title)) },


            text = {
                Column {
                    TextField(
                        value = newNote,
                        onValueChange = { newNote = it },
                        label = { Text(stringResource(R.string.new_note_dialog_text)) }
                    )
                }
            },



            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(0xFF4682B4)),
                    onClick = {
                        onSaveBtnClick(newNote.text)
                        onCancelBtnClick()
                    }
                ) {
                    Text(stringResource(R.string.save_btn_text))
                }
            },


            dismissButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(Color(0xFF4682B4)),
                    onClick = { onCancelBtnClick() }
                ) {
                    Text(stringResource(R.string.cancel_btn_text))
                }
            }


        )
    }
}