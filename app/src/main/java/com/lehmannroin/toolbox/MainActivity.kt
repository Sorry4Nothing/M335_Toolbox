package com.lehmannroin.toolbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var buttonWetter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        buttonWetter = findViewById(R.id.buttonWetter)

        buttonWetter.setOnClickListener {
            val intent = Intent(this, Wetter::class.java)
            startActivity(intent)
        }

    }

}