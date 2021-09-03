package com.aa.meteorddp.helper

import com.aa.meteorddp.helper.MeteorLogger

// MARK:- 🚀 MeteorDDP global declaration

internal val METEOR_DDP = "MeteorDDP"

internal val logger = MeteorLogger()

class Constants {

    object Info {
        const val TAG = "Meteor"
        val SUPPORTED_DDP_VERSIONS = arrayOf("1", "pre2", "pre1")
        const val RECONNECT_ATTEMPTS_MAX = 5
        const val FILE_NAME = "android_ddp"
    }

    object Keys {
        const val LOGIN_TOKEN = "login_token"
    }

    object Message {
        const val ADDED = "added"
        const val ADDED_BEFORE = "addedBefore"
        const val CHANGED = "changed"
        const val CONNECT = "connect"
        const val CONNECTED = "connected"
        const val FAILED = "failed"
        const val METHOD = "method"
        const val NOSUB = "nosub"
        const val PING = "ping"
        const val PONG = "pong"
        const val READY = "ready"
        const val REMOVED = "removed"
        const val RESULT = "result"
        const val SUBSCRIBE = "sub"
        const val UNSUBSCRIBE = "unsub"
    }

    object Field {
        const val CLEARED = "cleared"
        const val COLLECTION = "collection"
        const val DETAILS = "details"
        const val ERROR = "error"
        const val FIELDS = "fields"
        const val ID = "id"
        const val MESSAGE = "msg"
        const val METHOD = "method"
        const val NAME = "name"
        const val PARAMS = "params"
        const val RANDOM_SEED = "randomSeed"
        const val REASON = "reason"
        const val RESULT = "result"
        const val SESSION = "session"
        const val SUBS = "subs"
        const val SUPPORT = "support"
        const val VERSION = "version"
        const val TOKEN = "token"
    }


}


