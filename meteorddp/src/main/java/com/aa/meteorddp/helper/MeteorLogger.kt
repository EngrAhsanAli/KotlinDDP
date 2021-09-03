package com.aa.meteorddp.helper

// MARK:- 🚀 MeteorLogger - Prints the information in defined manner
open class MeteorLogger {

    enum class Level {
        normal, incomming, info, none
    }

    /// Loggging tags
    enum class LogTags(val string: String) {
        login("Login"),
        signup("Sign up"),
        sub("Meteor Subscribe"),
        unsub("Meteor Unsubscribe"),
        doc("Meteor Document"),
        method("Meteor Method"),
        receiveMessage("Meteor Receive Message"),
        socket("Web Socket"),
        mainThread("Main Thread Warning"),
        error("Meteor Error")
    }

    /// Flag to allow logging information  in the application
    companion object {
        public var loggingLevel = Level.info
    }

    /// Print log information
    /// - Parameters:
    ///   - label: tag
    ///   - items: items to print
    internal fun log(label: LogTags, items: Any, type: Level) {
        if (loggingLevel != Level.none && ((type == loggingLevel) || loggingLevel == Level.normal)) {
            println("\n ❕ ❕ ❕ 🚀 $METEOR_DDP ❕ ❕ ❕\n ${label.string} $items \n\n")
        }
    }

    /// Print log information
    /// - Parameters:
    ///   - label: tag
    ///   - items: items to print
    internal fun logError(label: LogTags, items: Any) {
        if (loggingLevel != Level.none) {
            println("\n ❕ ❕ ❕ 🚀 $METEOR_DDP ❕ ❕ ❕\n ${label.string} $items ‼️\n\n")
        }
    }

}