package com.aa.meteorddp

import android.content.Context
import android.content.SharedPreferences
import com.aa.meteorddp.callbacks.ObjectMapperCallback
import com.aa.meteorddp.callbacks.StorageCallback
import com.aa.meteorddp.callbacks.WebSocketCallback
import com.aa.meteorddp.helper.Constants
import com.aa.meteorddp.helper.Constants.Info.FILE_NAME
import com.aa.meteorddp.helper.MeteorLogger
import com.aa.meteorddp.helper.WebSocketEvent
import com.aa.meteorddp.helper.logger
import com.aa.meteorddp.meteor.DDPClient
import com.aa.meteorddp.meteor.MeteorWebSockets
import com.google.gson.Gson


open class Meteor(
    url: String, timeout: Int,
    context: Context,
    objectMapperCallback: ObjectMapperCallback,
    webSocketListener: WebSocketCallback,
    storageCallback: StorageCallback,
    protocolVersion: String
) : DDPClient(
    objectMapperCallback,
    webSocketListener,
    storageCallback,
    protocolVersion
) {

    companion object {
        private lateinit var socket: MeteorWebSockets
        private var mInstance: Meteor? = null
        private var mSharedPreferences: SharedPreferences? = null

        @Synchronized
        fun createInstance(url: String, timeout: Int = 5000, context: Context): Meteor? {
            check(mInstance == null) { "An instance has already been created" }

            val webSocketCallback = object : WebSocketCallback {

                override fun connect() {
                    socket.configureWebSocket()
                }

                override fun disconnect() {
                    socket.disconnect()
                }

                override fun sendText(text: String) {
                    socket.send(text)
                }
            }

            val objectMapperCallback = object : ObjectMapperCallback {

                override fun toJson(obj: Any): String? {
                    return Gson().toJson(obj)
                }

                override fun mapper(obj: String): HashMap<*, *> {
                    val map: HashMap<Any?, Any?> = hashMapOf()
                    return Gson().fromJson(obj, map.javaClass) as HashMap<*, *>
                }

            }

            val storageCallback = object : StorageCallback {
                override fun saveLoginToken(key: String, token: String?) {
                    val editor: SharedPreferences.Editor = mSharedPreferences!!.edit()
                    editor.putString(Constants.Keys.LOGIN_TOKEN, token)
                    editor.apply()
                }

                override fun getLoginToken(key: String): String? {
                    return mSharedPreferences!!.getString(Constants.Keys.LOGIN_TOKEN, null);
                }

            }

            val protocolVersion = "1"
            mInstance =
                Meteor(
                    url,
                    timeout,
                    context,
                    objectMapperCallback,
                    webSocketCallback,
                    storageCallback,
                    protocolVersion
                )
            return mInstance
        }

        @get:Synchronized
        val instance: Meteor?
            get() {
                checkNotNull(mInstance) { "Please call `createInstance(...)` first" }
                return mInstance
            }

        @Synchronized
        fun hasInstance(): Boolean {
            return mInstance != null
        }

        @Synchronized
        fun destroyInstance() {
            checkNotNull(mInstance) { "Please call `createInstance(...)` first" }
            mInstance!!.disconnect()
            mInstance!!.removeCallbacks()
            mInstance = null
        }

        fun connectSocket() {
            mInstance!!.connect()
        }

        fun disconnectSocket() {
            mInstance!!.disconnect()
        }

    }


    init {
        mSharedPreferences =
            context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        socket = MeteorWebSockets(url, timeout)
        bindEvent()
    }

    private fun bindEvent() {
        val events: ((WebSocketEvent, Any?) -> (Unit)) = { event, data ->
            when (event) {
                WebSocketEvent.connected -> mInstance!!.onConnected()
                WebSocketEvent.disconnected -> mInstance!!.onDisconnected()
                WebSocketEvent.text -> mInstance!!.onTextMessage(data as String)
                WebSocketEvent.error -> logger.logError(
                    MeteorLogger.LogTags.socket,
                    data.toString()
                )
            }
        }
        socket.onEvent = events
    }


}