package com.brys.poc.ig

fun scan(uri: String): Boolean {
    if (uri.contains(Regex("^(https?\\:\\/\\/)?(www\\.youtube\\.com|youtu\\.be)\\/.+\$"))) return true
    return false
}