package com.aa.meteorddp.helper

fun Map<*, *>.valueToString(key: Any): String {
    if (this.containsKey(key)) return this[key].toString()
    return ""
}

