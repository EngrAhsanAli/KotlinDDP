package com.aa.meteorddp.callbacks

import android.os.Handler
import android.os.Looper
import java.util.*

class CallbackProxy : MeteorCallback {
    private val mCallbacks: MutableList<MeteorCallback> =
        LinkedList()
    private val mUiHandler = Handler(Looper.getMainLooper())
    fun addCallback(callback: MeteorCallback) {
        mCallbacks.add(callback)
    }

    fun removeCallback(callback: MeteorCallback?) {
        mCallbacks.remove(callback)
    }

    fun removeCallbacks() {
        mCallbacks.clear()
    }

    override fun onConnect(signedInAutomatically: Boolean) {
        for (callback in mCallbacks) mUiHandler.post {
            callback.onConnect(signedInAutomatically)
        }
    }

    override fun onDisconnect() {
        for (callback in mCallbacks) mUiHandler.post {
            callback.onDisconnect()
        }
    }

    override fun onDataAdded(
        collectionName: String?,
        documentID: String?,
        newValuesJson: String?
    ) {
        for (callback in mCallbacks) mUiHandler.post {
            callback.onDataAdded(collectionName, documentID, newValuesJson)
        }
    }

    override fun onDataChanged(
        collectionName: String?,
        documentID: String?,
        updatedValuesJson: String?,
        removedValuesJson: String?
    ) {
        for (callback in mCallbacks) mUiHandler.post {
            callback.onDataChanged(collectionName, documentID, updatedValuesJson, removedValuesJson)
        }
    }

    override fun onDataRemoved(collectionName: String?, documentID: String?) {
        for (callback in mCallbacks) mUiHandler.post {
            callback.onDataRemoved(collectionName, documentID)
        }
    }

    override fun onException(e: Exception?) {
        for (callback in mCallbacks) mUiHandler.post {
            callback.onException(e)
        }
    }

    fun forResultListener(callback: ResultListener?): ResultListener {
        return object :
            ResultListener {
            override fun onSuccess(result: String?) {
                callback?.let {
                    mUiHandler.post {
                        it.onSuccess(result)
                    }
                }
            }

            override fun onError(
                error: String?,
                reason: String?,
                details: String?
            ) {
                callback?.let {
                    mUiHandler.post {
                        it.onError(error, reason, details)
                    }
                }
            }
        }
    }

    fun forSubscribeListener(callback: SubscribeListener?): SubscribeListener {
        return object :
            SubscribeListener {
            override fun onSuccess() {
                callback?.let {
                    mUiHandler.post {
                        it.onSuccess()
                    }
                }
            }

            override fun onError(
                error: String?,
                reason: String?,
                details: String?
            ) {
                callback?.let {
                    mUiHandler.post {
                        it.onError(error, reason, details)
                    }
                }
            }
        }
    }

    fun forUnsubscribeListener(callback: UnsubscribeListener?): UnsubscribeListener {
        return object :
            UnsubscribeListener {
            override fun onSuccess() {
                callback?.let {
                    mUiHandler.post {
                        it.onSuccess()
                    }
                }
            }
        }
    }
}