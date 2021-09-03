package com.aa.meteorddp.meteor

import com.aa.meteorddp.callbacks.*
import com.aa.meteorddp.helper.Constants.*
import com.aa.meteorddp.helper.Error
import com.aa.meteorddp.helper.MongoDb
import com.aa.meteorddp.helper.valueToString
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.set

open class DDPClient(
    val objectMapperCallback: ObjectMapperCallback,
    val websocketListener: WebSocketCallback,
    val storageCallback: StorageCallback,
    protocolVersion: String
) {

    companion object {

        private var mLoggingEnabled = false

        fun isVersionSupported(protocolVersion: String): Boolean =
            listOf(* Info.SUPPORTED_DDP_VERSIONS).contains(protocolVersion)

        fun log(message: String) {
            if (mLoggingEnabled) println("$Info.TAG ====================== $message")
        }

        fun uniqueID(): String = UUID.randomUUID().toString()

        private fun emptyMap(): Map<String, Any> = HashMap()

    }

    private var mListeners: HashMap<String, Listener> = HashMap()
    private var mQueuedMessages: Queue<String> = ConcurrentLinkedQueue()
    private var mDdpVersion: String? = protocolVersion
    private var mReconnectAttempts = 0
    protected val mCallbackProxy = CallbackProxy()
    private var mSessionID: String? = null
    private var mConnected = false
    private var mLoggedInUserId: String? = null

    init {
        require(isVersionSupported(protocolVersion)) { "DDP protocol version not supported: $protocolVersion" }
    }

    open fun onConnected() {
        log("  onOpen")

        mConnected = true
        mReconnectAttempts = 0

        initConnection(mSessionID)
    }

    open fun onDisconnected() {
        log("  onClose")

        val lostConnection = mConnected

        mConnected = false

        if (lostConnection) {
            mReconnectAttempts++
            if (mReconnectAttempts <= Info.RECONNECT_ATTEMPTS_MAX) reconnect() else disconnect()
        }

        mCallbackProxy.onDisconnect()
    }

    open fun onTextMessage(text: String) {
        log("  onTextMessage")
        log("    payload == $text")

        handleMessage(text)
    }

    open fun handleCallbackError(cause: Throwable?) {
        mCallbackProxy.onException(Exception(cause))
    }

    open fun onError(cause: Exception?) {
        mCallbackProxy.onException(Exception(cause))
    }

    open fun connect() {
        openConnection(false)
    }

    open fun isConnected(): Boolean {
        return mConnected
    }

    open fun reconnect() {
        openConnection(true)
    }

    private fun isLoginResult(result: HashMap<*, *>): Boolean =
        result.containsKey(Field.TOKEN) && result.containsKey(Field.ID)

    private fun openConnection(isReconnect: Boolean) =
        if (isReconnect && mConnected) initConnection(mSessionID)
        else websocketListener.connect()

    private fun initConnection(existingSessionID: String?) {
        val data: MutableMap<String, Any> =
            HashMap()
        data[Field.MESSAGE] = Message.CONNECT
        data[Field.VERSION] = mDdpVersion!!
        data[Field.SUPPORT] = Info.SUPPORTED_DDP_VERSIONS
        if (existingSessionID != null) data[Field.SESSION] = existingSessionID
        send(data)
    }

    open fun disconnect() {
        mConnected = false

        mListeners.clear()
        this.mSessionID = null
        try {
            // DELETED CODE
            websocketListener.disconnect()
        } catch (e: Exception) {
            mCallbackProxy.onException(e)
        }
    }

    private fun send(obj: Any) {

        val jsonStr: String = objectMapperCallback.toJson(obj)
            ?: throw IllegalArgumentException("Object would be serialized to `null`")
        send(jsonStr)
    }

    fun send(message: String) {
        log("  send")
        log("    message == $message")
        if (mConnected) {
            log("    dispatching")
            websocketListener.sendText(message)
        } else {
            log("    queueing")
            mQueuedMessages.add(message)
        }
    }

    open fun addCallback(callback: MeteorCallback?) {
        mCallbackProxy.addCallback(callback!!)
    }

    open fun removeCallback(callback: MeteorCallback?) {
        mCallbackProxy.removeCallback(callback)
    }

    open fun removeCallbacks() {
        mCallbackProxy.removeCallbacks()
    }

    open fun isLoggedIn(): Boolean {
        return mLoggedInUserId != null
    }

    open fun getUserId(): String? {
        return mLoggedInUserId
    }

    private fun sendPong(id: String?) {
        val data: MutableMap<String, Any> =
            HashMap()
        data[Field.MESSAGE] = Message.PONG
        if (id != null) {
            data[Field.ID] = id
        }
        send(data)
    }

    open fun setLoggingEnabled(enabled: Boolean) {
        mLoggingEnabled = enabled
    }

    open fun insert(collectionName: String, data: Map<String, Any>) =
        insert(collectionName, data, null)

    open fun insert(collectionName: String, data: Map<String, Any>, listener: ResultListener?) =
        call("/$collectionName/insert", arrayOf(data), listener)

    open fun update(collectionName: String, query: Map<String, Any>, data: Map<String, Any>) =
        update(collectionName, query, data, emptyMap())

    open fun update(
        collectionName: String, query: Map<String, Any>,
        data: Map<String, Any>, options: Map<String, Any>
    ) =
        update(collectionName, query, data, options, null)

    open fun update(
        collectionName: String,
        query: Map<String, Any>,
        data: Map<String, Any>,
        options: Map<String, Any>,
        listener: ResultListener?
    ) = call("/$collectionName/update", arrayOf<Any>(query, data, options), listener)

    open fun remove(collectionName: String, documentID: String?) =
        remove(collectionName, documentID, null)

    open fun remove(
        collectionName: String,
        documentId: String?,
        listener: ResultListener?
    ) {
        val query: MutableMap<String, Any?> =
            HashMap()
        query[MongoDb.Field.ID] = documentId
        call("/$collectionName/remove", arrayOf<Any>(query), listener)
    }

    open fun loginWithUsername(
        username: String?, password: String, listener: ResultListener
    ) = login(username, null, password, listener)

    open fun loginWithEmail(
        email: String?, password: String, listener: ResultListener
    ) = login(null, email, password, listener)

    open fun login(
        username: String?,
        email: String?,
        password: String,
        listener: ResultListener
    ) {
        val userData: MutableMap<String, Any> =
            HashMap()
        when {
            username != null -> {
                userData["username"] = username
            }
            email != null -> {
                userData["email"] = email
            }
            else -> {
                throw IllegalArgumentException("You must provide either a username or an email address")
            }
        }
        val authData: MutableMap<String, Any> =
            HashMap()
        authData["user"] = userData
        authData["password"] = password
        call("login", arrayOf<Any>(authData), listener)
    }

    open fun loginWithToken(token: String, listener: ResultListener) {
        val authData: MutableMap<String, Any> =
            HashMap()
        authData["resume"] = token
        call("login", arrayOf<Any>(authData), listener)
    }

    open fun logout() = logout(null)

    open fun logout(listener: ResultListener?) {
        call("logout", arrayOf<Any>(), object :
            ResultListener {
            override fun onSuccess(result: String?) { // remember that we're not logged in anymore
                mLoggedInUserId = null
                // delete the last login token which is now invalid
                saveLoginToken(null)
                if (listener != null) {
                    mCallbackProxy.forResultListener(listener).onSuccess(result)
                }
            }

            override fun onError(
                error: String?,
                reason: String?,
                details: String?
            ) {
                if (listener != null) {
                    mCallbackProxy.forResultListener(listener).onError(error, reason, details)
                }
            }
        })
    }

    open fun registerAndLogin(
        username: String, email: String, password: String, listener: ResultListener?
    ) = registerAndLogin(username, email, password, null, listener)


    open fun registerAndLogin(
        username: String?,
        email: String?,
        password: String,
        profile: HashMap<String?, Any?>?,
        listener: ResultListener?
    ) {
        require(!(username == null && email == null)) { "You must provide either a username or an email address" }
        val accountData: MutableMap<String, Any> =
            HashMap()
        if (username != null) {
            accountData["username"] = username
        }
        if (email != null) {
            accountData["email"] = email
        }
        accountData["password"] = password
        if (profile != null) {
            accountData["profile"] = profile
        }
        call("createUser", arrayOf(accountData), listener)
    }

    open fun call(methodName: String?) = call(methodName, null, null)

    open fun call(methodName: String?, params: Array<Any>?) = call(methodName, params, null)

    open fun call(methodName: String?, listener: ResultListener?) = call(methodName, null, listener)

    open fun call(methodName: String?, params: Array<Any>?, listener: ResultListener?) =
        callWithSeed(methodName, null, params, listener)

    open fun callWithSeed(methodName: String?, randomSeed: String?) =
        callWithSeed(methodName, randomSeed, null, null)

    open fun callWithSeed(methodName: String?, randomSeed: String?, params: Array<Any>?) =
        callWithSeed(methodName, randomSeed, params, null)

    open fun callWithSeed(
        methodName: String?,
        randomSeed: String?,
        params: Array<Any>?,
        listener: ResultListener?
    ) { // create a new unique ID for this request
        val callId: String = uniqueID()
        // save a reference to the listener to be executed later
        if (listener != null) {
            mListeners.put(callId, listener)
        }
        val data: MutableMap<String, Any?> =
            HashMap()
        data[Field.MESSAGE] = Message.METHOD
        data[Field.METHOD] = methodName
        data[Field.ID] = callId
        if (params != null) {
            data[Field.PARAMS] = params
        }
        if (randomSeed != null) {
            data[Field.RANDOM_SEED] = randomSeed
        }
        send(data)
    }

    open fun subscribe(subscriptionName: String?): String? = subscribe(subscriptionName, null)

    open fun subscribe(subscriptionName: String?, params: Array<Any?>?): String? =
        subscribe(subscriptionName, params, null)

    open fun subscribe(
        subscriptionName: String?, params: Array<Any?>?, listener: SubscribeListener?
    ): String? { // create a new unique ID for this request
        val subscriptionId: String = uniqueID()
        // save a reference to the listener to be executed later
        if (listener != null) {
            mListeners.put(subscriptionId, listener)
        }
        val data: MutableMap<String, Any?> =
            HashMap()
        data[Field.MESSAGE] = Message.SUBSCRIBE
        data[Field.NAME] = subscriptionName
        data[Field.ID] = subscriptionId
        if (params != null) {
            data[Field.PARAMS] = params
        }
        send(data)
        // return the generated subscription ID
        return subscriptionId
    }

    open fun unsubscribe(subscriptionId: String) = unsubscribe(subscriptionId, null)

    open fun unsubscribe(
        subscriptionId: String,
        listener: UnsubscribeListener?
    ) { // save a reference to the listener to be executed later
        if (listener != null) {
            mListeners.put(subscriptionId, listener)
        }
        val data: MutableMap<String, Any?> =
            HashMap()
        data[Field.MESSAGE] = Message.UNSUBSCRIBE
        data[Field.ID] = subscriptionId
        send(data)
    }

    private fun saveLoginToken(token: String?) =
        storageCallback.saveLoginToken(Keys.LOGIN_TOKEN, token)

    private fun getLoginToken(): String? =
        storageCallback.getLoginToken(Keys.LOGIN_TOKEN)

    private fun initSession() { // get the last login token
        val loginToken = getLoginToken()
        // if we found a login token that might work
        if (loginToken != null) { // try to sign in with that token
            loginWithToken(loginToken, object :
                ResultListener {
                override fun onSuccess(result: String?) {
                    announceSessionReady(true)
                }

                override fun onError(
                    error: String?,
                    reason: String?,
                    details: String?
                ) { // clear the user ID since automatic sign-in has failed
                    mLoggedInUserId = null
                    // discard the token which turned out to be invalid
                    saveLoginToken(null)
                    announceSessionReady(false)
                }
            })
        } else {
            announceSessionReady(false)
        }
    }

    private fun announceSessionReady(signedInAutomatically: Boolean) {
        mCallbackProxy.onConnect(signedInAutomatically)
        var queuedMessage: String? = null
        while (mQueuedMessages.poll().also { queuedMessage = it } != null) {
            send(queuedMessage!!)
        }

    }

    open fun handleMessage(payload: String) {
        val map = objectMapperCallback.mapper(payload)

        if (map.containsKey(Field.MESSAGE)) {

            val message: String = map.valueToString(Field.MESSAGE)
            if (message == Message.CONNECTED) {
                if (map.containsKey(Field.SESSION)) {
                    mSessionID = map.valueToString(Field.SESSION)
                }
                initSession()
            } else if (message == Message.FAILED) {
                if (map.containsKey(Field.VERSION)) {
                    // the server wants to use a different protocol version
                    val desiredVersion: String = map.valueToString(Field.VERSION)
                    // if the protocol version that was requested by the server is supported by this client
                    mDdpVersion =
                        if (isVersionSupported(desiredVersion)) { // remember which version has been requested
                            desiredVersion
                            // the server should be closing the connection now and we will re-connect afterwards
                        } else {
                            throw RuntimeException("Protocol version not supported: $desiredVersion")
                        }
                }
            } else if (message == Message.PING) {
                val id: String? = if (map.containsKey(Field.ID)) {
                    map.valueToString(Field.ID)
                } else {
                    null
                }
                sendPong(id)
            } else if (message == Message.ADDED || message == Message.ADDED_BEFORE) {
                val documentID: String? = if (map.containsKey(Field.ID)) {
                    map.valueToString(Field.ID)
                } else {
                    null
                }
                val collectionName: String? = if (map.containsKey(Field.COLLECTION)) {
                    map.valueToString(Field.COLLECTION)
                } else {
                    null
                }
                val newValuesJson: String? = if (map.containsKey(Field.FIELDS)) {
                    // TODO: MAYBE WORK, need testing
                    map.valueToString(Field.FIELDS)
                } else {
                    null
                }

                mCallbackProxy.onDataAdded(collectionName, documentID, newValuesJson)
            } else if (message == Message.CHANGED) {
                val documentID: String? =
                    if (map.containsKey(Field.ID)) map.valueToString(Field.ID) else null

                val collectionName: String? =
                    if (map.containsKey(Field.COLLECTION)) map.valueToString(Field.COLLECTION) else null

                val updatedValuesJson: String? =
                    // TODO: MAYBE WORK, need testing
                    if (map.containsKey(Field.FIELDS)) map.valueToString(Field.FIELDS) else null

                val removedValuesJson: String? =
                    // TODO: MAYBE WORK, need testing
                    if (map.containsKey(Field.CLEARED)) map.valueToString(Field.CLEARED) else null

                mCallbackProxy.onDataChanged(
                    collectionName,
                    documentID,
                    updatedValuesJson,
                    removedValuesJson
                )
            } else if (message == Message.REMOVED) {
                val documentID: String? = if (map.containsKey(Field.ID)) {
                    map.valueToString(Field.ID)
                } else {
                    null
                }
                val collectionName: String? = if (map.containsKey(Field.COLLECTION)) {
                    map.valueToString(Field.COLLECTION)
                } else {
                    null
                }
                mCallbackProxy.onDataRemoved(collectionName, documentID)
            } else if (message == Message.RESULT) { // check if we have to process any result data internally
                if (map.containsKey(Field.RESULT)) {
                    // TODO:- MAYBE WORK, NEED TESTING
                    val resultData =
                        map[Field.RESULT] as HashMap<*, *>?
                    // if the result is from a previous login attempt
                    resultData?.let {
                        if (isLoginResult(it)) {
                            val loginToken: String = it.valueToString(Field.TOKEN)
                            saveLoginToken(loginToken)
                            mLoggedInUserId = it.valueToString(Field.ID)
                        }

                    }

                }
                val id: String? =
                    if (map.containsKey(Field.ID)) map.valueToString(Field.ID) else null

                val listener = mListeners[id]

                if (listener is ResultListener) {
                    mListeners.remove(id)
                    val result: String? = if (map.containsKey(Field.RESULT)) {
                        // TODO: MAYBE WORK, need testing
                        map.valueToString(Field.RESULT)
                    } else {
                        null
                    }
                    if (map.containsKey(Field.ERROR)) {
                        val error = Error.fromJson(map)
                        mCallbackProxy.forResultListener(listener as ResultListener?)
                            .onError(error.error, error.reason, error.details)
                    } else {
                        mCallbackProxy.forResultListener(listener as ResultListener?)
                            .onSuccess(result)
                    }
                }
            } else if (message == Message.READY) {
                if (map.containsKey(Field.SUBS)) {
                    // TODO:- NEED TO TEST
                    val elements = map[Field.SUBS] as HashMap<*, *>?
                    // if the result is from a previous login attempt
                    elements?.let {

                        var subscriptionId: String

                        it.forEach { (key, value) ->
                            val stringValue = it.valueToString(key!!)
                            if (stringValue != "") {
                                subscriptionId = stringValue
                                val listener =
                                    mListeners[subscriptionId]
                                if (listener is SubscribeListener) {
                                    mListeners.remove(subscriptionId)
                                    mCallbackProxy.forSubscribeListener(listener as SubscribeListener?)
                                        .onSuccess()
                                }
                            }
                        }
                    }
                }
            } else if (message == Message.NOSUB) {
                val subscriptionId: String? = if (map.containsKey(Field.ID)) {
                    map.valueToString(Field.ID)
                } else {
                    null
                }
                val listener = mListeners[subscriptionId]
                if (listener is SubscribeListener) {
                    mListeners.remove(subscriptionId)
                    if (map.containsKey(Field.ERROR)) {
                        val error = Error.fromJson(map)
                        mCallbackProxy.forSubscribeListener(listener as SubscribeListener?)
                            .onError(error.error, error.reason, error.details)
                    } else {
                        mCallbackProxy.forSubscribeListener(listener as SubscribeListener?)
                            .onError(null, null, null)
                    }
                } else if (listener is UnsubscribeListener) {
                    mListeners.remove(subscriptionId)
                    mCallbackProxy.forUnsubscribeListener(listener as UnsubscribeListener?)
                        .onSuccess()
                }
            }
        }


    }

}