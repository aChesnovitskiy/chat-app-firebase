package com.chesnovitskiy.chatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.chesnovitskiy.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btn_login.setOnClickListener {
            val email = et_email.text.toString().trim()
            val password = et_password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) return@setOnClickListener

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("My_LoginActivity", "Current data is null")
                        startActivity(Intent(this, ChatActivity::class.java))
                    } else {
                        Log.w("My_LoginActivity", "Error: ${task.exception}")
                        Toast.makeText(this, "Error: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tv_registration.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
