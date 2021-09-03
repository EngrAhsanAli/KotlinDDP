package com.aa.meteorddp.helper

class Error private constructor(
    val error: String?,
    val reason: String?,
    val details: String?
) {

    companion object {
        @JvmStatic
        fun fromJson(json: Map<*, *>): Error {
            val error: String?
            error = if (json.containsKey(Constants.Field.ERROR)) {
                val errorJson = json[Constants.Field.ERROR]

                if (errorJson is Any) {
                    errorJson.toString()
                } else {
                    throw IllegalArgumentException("Unexpected data type of error.error")
                }
            } else {
                null
            }
            val reason: String? = if (json.containsKey(Constants.Field.REASON)) {
                json[Constants.Field.REASON].toString()
            } else {
                null
            }
            val details: String? = if (json.containsKey(Constants.Field.DETAILS)) {
                json[Constants.Field.DETAILS].toString()
            } else {
                null
            }
            return Error(
                error,
                reason,
                details
            )
        }
    }

}