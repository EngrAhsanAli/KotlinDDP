package com.aa.kotlinddp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.aa.meteorddp.Meteor
import com.aa.meteorddp.callbacks.MeteorCallback
import com.aa.meteorddp.callbacks.ResultListener

class MainActivity : AppCompatActivity(), MeteorCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val meteor = Meteor.createInstance("ws://localhost:4000/websocket", context = this)

        meteor!!.addCallback(this)
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
            meteor.call("demoMethod", listener = object: ResultListener {
                override fun onSuccess(result: String?) {
                    println(result.toString())
                }

                override fun onError(error: String?, reason: String?, details: String?) {
                    println(error.toString())
                }

            })

            meteor.subscribe("subscribeDemo")

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
            meteor.disconnect()
        }


    }

    override fun onConnect(signedInAutomatically: Boolean) {
        println("On connected")
    }

    override fun onDisconnect() {
        println("On disconnected")
    }

    override fun onException(e: Exception?) {
        println("On exception")
    }

    override fun onDataAdded(collectionName: String?, documentID: String?, newValuesJson: String?) {
        println("On data added in "+ collectionName)
    }

    override fun onDataChanged(
        collectionName: String?,
        documentID: String?,
        updatedValuesJson: String?,
        removedValuesJson: String?
    ) {
        println("On data change in "+ collectionName)
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        println("On data remove in "+ collectionName)
    }
}