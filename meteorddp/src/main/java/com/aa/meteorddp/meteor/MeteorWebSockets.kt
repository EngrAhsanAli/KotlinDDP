package com.aa.meteorddp.meteor

import com.aa.meteorddp.helper.MeteorLogger
import com.aa.meteorddp.helper.WebSocketEvent
import com.neovisionaries.ws.client.*
import com.aa.meteorddp.helper.logger


// MARK:- 🚀 MeteorWebSockets
open class MeteorWebSockets(url: String, timeout: Int) {

    private var socket: WebSocket
    var onEvent: ((WebSocketEvent, Any?) -> (Unit))? = null
    var isConnected: Boolean = false

    init {
        val factory = WebSocketFactory()
        socket = factory.createSocket(url, timeout)
    }

    /// Configuration
    fun configureWebSocket() {
        socket.connectAsynchronously()
        socket.addListener(object : WebSocketAdapter() {

            override fun onConnected(
                websocket: WebSocket?,
                headers: MutableMap<String, MutableList<String>>?
            ) {
                super.onConnected(websocket, headers)
                isConnected = true
                onEvent?.invoke(WebSocketEvent.connected, true)
                logger.log(MeteorLogger.LogTags.socket, "Connected", MeteorLogger.Level.info)
            }

            override fun onDisconnected(
                websocket: WebSocket?,
                serverCloseFrame: WebSocketFrame?,
                clientCloseFrame: WebSocketFrame?,
                closedByServer: Boolean
            ) {
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                isConnected = false
                onEvent?.invoke(WebSocketEvent.connected, false)
                logger.log(MeteorLogger.LogTags.socket, "Disconnected", MeteorLogger.Level.info)
            }

            override fun onTextMessage(websocket: WebSocket?, text: String?) {
                super.onTextMessage(websocket, text)
                onEvent?.invoke(WebSocketEvent.text, text)
                text?.let { logger.log(MeteorLogger.LogTags.socket, it, MeteorLogger.Level.info) }

            }

            override fun onError(websocket: WebSocket?, cause: WebSocketException?) {
                super.onError(websocket, cause)
                isConnected = false
                onEvent?.invoke(WebSocketEvent.error, cause.toString())
                logger.log(MeteorLogger.LogTags.socket, "Error: ${cause.toString()}", MeteorLogger.Level.info)
            }

            override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
                super.onConnectError(websocket, exception)
                isConnected = false
                onEvent?.invoke(WebSocketEvent.error, exception.toString())
                logger.log(MeteorLogger.LogTags.socket, "Error: ${exception.toString()}", MeteorLogger.Level.info)
            }

        })

    }

    /// Disconnect on demand
    fun disconnect() {
        socket.disconnect(WebSocketCloseCode.NORMAL, null)
    }

    fun send(text: String) {
        socket.sendText(text)
    }
}