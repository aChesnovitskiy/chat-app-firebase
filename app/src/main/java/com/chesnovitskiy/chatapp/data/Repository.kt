package com.chesnovitskiy.chatapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

object Repository {
    private const val TAG = "My_Repository"
    private val database by lazy { Firebase.firestore }
    val isAddSuccess = MutableLiveData<Boolean>()

    fun getAllMessages(): LiveData<List<Message>> {
        val result = MutableLiveData<List<Message>>()

        database.collection("messages")
            .orderBy("date")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.w(TAG, "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = mutableListOf<Message>()

                    for (message in snapshot) {
                        Log.d(TAG, "${message.id} => ${message.data}")
                        messages.add(message.toObject())
                    }

                    result.value = messages
                } else {
                    Log.d(TAG, "Current data is null")
                }
            }

        return result
    }

    fun addMessage(message: Message) {
        database.collection("messages")
            .add(message)
            .addOnSuccessListener { reference ->
                isAddSuccess.value = true
                Log.d(TAG, "Message added with ID: ${reference.id}")
            }
            .addOnFailureListener { error ->
                isAddSuccess.value = false
                Log.w(TAG, "Error adding message", error)
            }
    }
}