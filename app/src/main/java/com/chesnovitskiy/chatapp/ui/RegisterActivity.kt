package com.chesnovitskiy.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.chesnovitskiy.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btn_registration.setOnClickListener {
            val email = et_email.text.toString().trim()
            val password = et_password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("My_RegisterActivity", "Current data is null")
                        startActivity(Intent(this, ChatActivity::class.java))
                    } else {
                        Log.w("My_RegisterActivity", "Error: ${task.exception}")
                        Toast.makeText(this, "Error: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tv_have_an_account.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
