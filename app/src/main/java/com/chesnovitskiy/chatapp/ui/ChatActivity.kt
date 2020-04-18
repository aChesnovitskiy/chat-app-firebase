package com.chesnovitskiy.chatapp.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chesnovitskiy.chatapp.R
import com.chesnovitskiy.chatapp.data.Message
import com.chesnovitskiy.chatapp.viewmodels.ChatViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity() {
    private val messagesAdapter: MessagesAdapter by lazy { MessagesAdapter(this) }
    private val viewModel: ChatViewModel by lazy {
        val vmFactory = ChatViewModel.ViewModelFactory(Application())
        ViewModelProvider(this, vmFactory).get(ChatViewModel::class.java)
    }

    private var imageUri: Uri? = null
    private lateinit var preferences: SharedPreferences

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        auth = FirebaseAuth.getInstance()

        storage = Firebase.storage
        // Create a storage reference from our app
        storageRef = storage.reference
        // Create a child reference
        // imagesRef now points to "images"

        with(rv_messages) {
            adapter = messagesAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }

    override fun onResume() {
        super.onResume()

        setViewModel()

        setListeners()
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            preferences.edit().putString("author", currentUser.email).apply()
        } else {
            signOut()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_sign_out) {
            auth.signOut()
            signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_GET_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = data?.data
                    uri ?: return

                    val imageRef = storageRef.child("images/${uri.lastPathSegment}")

                    val uploadTask = imageRef.putFile(uri)

                    val urlTask = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        imageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            imageUri = task.result
                            sendMessage()
                            Log.d("My_ChatActivity", imageUri.toString())
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                }
            }

            RC_SIGN_IN -> {
                val response = IdpResponse.fromResultIntent(data)

                if (resultCode == Activity.RESULT_OK) {
                    // Successfully signed in
                    val user = auth.currentUser
                    if (user != null) {
                        preferences.edit().putString("author", user.email).apply()
                        makeShortToast("User signed in with email: ${user.email}")
                    }
                } else {
                    // Sign in failed
                    if (response != null) {
                        makeShortToast("Error: ${response.error}")
                    } else {
                        makeShortToast("Error")
                    }
                }
            }
        }
    }

    private fun setViewModel() {
        viewModel.messages.observe(this, Observer { messages ->
            messagesAdapter.setMessages(messages)
            rv_messages.scrollToPosition(messages.size - 1)
        })

        viewModel.isMessageSend.observe(this, Observer {
            if (it) {
                et_message.setText("")
            } else {
                makeShortToast(getString(R.string.message_has_not_been_send))
            }
        })
    }

    private fun setListeners() {
        iv_send.setOnClickListener {
            sendMessage()
        }

        iv_add_image.setOnClickListener {
            getImage()
        }
    }

    private fun sendMessage() {
        val message = et_message.text.toString().trim()
        val author = preferences.getString("author", "Anonim")
        if (message.isNotEmpty()) {
            viewModel.addMessage(Message(author, message, System.currentTimeMillis()))
        } else if (imageUri != null) {
            viewModel.addMessage(
                Message(
                    author,
                    null,
                    System.currentTimeMillis(),
                    imageUri.toString()
                )
            )
        }
    }

    private fun getImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }

        startActivityForResult(intent, RC_GET_IMAGE)
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Choose authentication providers
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )

                    // Create and launch sign-in intent
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                        RC_SIGN_IN
                    )
                }
            }
    }

    private fun makeShortToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 1
        private const val RC_GET_IMAGE = 2
    }
}
