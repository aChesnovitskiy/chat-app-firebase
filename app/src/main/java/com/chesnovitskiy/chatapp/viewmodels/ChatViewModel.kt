package com.chesnovitskiy.chatapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chesnovitskiy.chatapp.data.Message
import com.chesnovitskiy.chatapp.data.Repository

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val messages: LiveData<List<Message>> = Repository.getAllMessages()
    val isMessageSend: LiveData<Boolean> = Repository.isAddSuccess

    fun addMessage(message: Message) {
        Repository.addMessage(message)
    }

    @Suppress("UNCHECKED_CAST")
    class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}