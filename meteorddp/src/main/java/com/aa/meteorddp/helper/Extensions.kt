package com.aa.meteorddp.helper

import com.google.gson.Gson

fun Map<*, *>.valueToString(key: Any): String {
    if (this.containsKey(key)) return Gson().toJson(this[key])
    return ""
}

