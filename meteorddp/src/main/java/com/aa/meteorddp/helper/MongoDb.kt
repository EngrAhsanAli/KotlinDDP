package com.aa.meteorddp.helper

class MongoDb {

    object Field {
        const val ID = "_id"
        @Deprecated("")
        val VALUE = "_value"
        @Deprecated("")
        val PRIORITY = "_priority"
    }

    object Modifier {
        @Deprecated("")
        val SET = "\$set"
    }

    object Option {
        @Deprecated("")
        val UPSERT = "upsert"
    }
}