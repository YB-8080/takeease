package com.davidev.takeease

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.davidev.takeease.datasource.DBHelperlog
import com.davidev.takeease.ui.MainActivity

class Signin : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var btnlogin: ImageView
    private lateinit var signup: TextView
    private lateinit var db: DBHelperlog
    //private lateinit var binding: Signin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


        username = findViewById(R.id.emailEt)
        password = findViewById(R.id.passEt)
        btnlogin = findViewById(R.id.nextBtn)
        signup   = findViewById(R.id.signupbtn)
        db = DBHelperlog(this)

        btnlogin.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
            } else {
                val checkuserpass = db.checkusernamepassword(user, pass)
                if (checkuserpass) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }

        }
        signup.setOnClickListener {
            val goScreen = Intent(applicationContext, SignUp::class.java)
            startActivity(goScreen)
        }

    }
}