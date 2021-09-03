package com.aa.kotlinddp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.aa.meteorddp.Meteor

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val meteor = Meteor.createInstance("ws://localhost:4000/websocket", context = this)

        val btnConnect = findViewById<TextView>(R.id.btnConnect)
        btnConnect.setOnClickListener {
            Meteor.connectSocket()
        }


        val btnLoginEmail = findViewById<TextView>(R.id.btnLoginEmail)
        btnLoginEmail.setOnClickListener {

        }


        val btnLoginUsername = findViewById<TextView>(R.id.btnLoginUsername)
        btnLoginUsername.setOnClickListener {

        }

        val btnCheckAttrs = findViewById<TextView>(R.id.btnCheckAttrs)
        btnCheckAttrs.setOnClickListener {
            meteor!!.call("getConfigurationsSso")
        }

        val btnLogout = findViewById<TextView>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            meteor!!.logout()
        }

        val btnReconnect = findViewById<TextView>(R.id.btnReconnect)
        btnReconnect.setOnClickListener {

        }


        val btnDisconnect = findViewById<TextView>(R.id.btnDisconnect)
        btnDisconnect.setOnClickListener {
            meteor!!.disconnect()
        }


    }
}