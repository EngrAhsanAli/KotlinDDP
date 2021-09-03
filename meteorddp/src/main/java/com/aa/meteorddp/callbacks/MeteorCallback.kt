package com.aa.meteorddp.callbacks

interface Listener

interface MeteorCallback : DdpCallback {
    fun onConnect(signedInAutomatically: Boolean)
    fun onDisconnect()
    fun onException(e: Exception?)
}

interface SubscribeListener : Listener {
    fun onSuccess()
    fun onError(
        error: String?,
        reason: String?,
        details: String?
    )
}

interface UnsubscribeListener : Listener {
    fun onSuccess()
}

interface ResultListener : Listener {
    fun onSuccess(result: String?)
    fun onError(
        error: String?,
        reason: String?,
        details: String?
    )
}

interface ObjectMapperCallback : Listener {
    fun toJson(obj: Any): String?
    fun mapper(obj: String): HashMap<*, *>
}

interface StorageCallback : Listener {
    fun saveLoginToken(key: String, token: String?)
    fun getLoginToken(key: String): String?
}

interface WebSocketCallback : Listener {
    fun connect()
    fun disconnect()
    fun sendText(text: String)

}






