package com.davidev.takeease

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.davidev.takeease.datasource.DBHelper
import com.davidev.takeease.datasource.DBHelperlog

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val username: TextView = findViewById(R.id.emailEt)
        val password: TextView = findViewById(R.id.passEt)
        val repassword: TextView = findViewById(R.id.verifyPassEt)
        val signup: ImageView = findViewById(R.id.nextBtn)
        val signin: TextView = findViewById(R.id.textViewSignIn)
        val db = DBHelperlog(this)

        signup.setOnClickListener {

            val user: String = username.text.toString()
            val pass = password.text.toString()

            val repass = repassword.text.toString()

            if (user.isEmpty() || pass.isEmpty() || repass.isEmpty()) {
                Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
            } else {
                if (pass == repass) {
                    val checkuser = db.checkusername(user)
                    if (!checkuser) {
                        val insert = db.insertData(user, pass)
                        if (insert) {
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, SignUp::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "User already exists! please sign in",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Passwords not matching", Toast.LENGTH_SHORT).show()
                }
            }

        }

        signin.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
        }
    }
}