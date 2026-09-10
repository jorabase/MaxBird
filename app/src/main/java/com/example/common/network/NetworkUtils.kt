package com.example.common.network

import android.os.Build

object NetworkUtils {
    /**
     * Generates a dynamic User-Agent string using real device hardware info.
     * Prevents using hardcoded models like V2029 or vivo 2027 in analytics and network requests.
     */
    fun getUserAgent(userId: String = ""): String {
        val androidVersion = Build.VERSION.RELEASE
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        return "Shikho/(605) 6.0.5 (Android $androidVersion; $model; $manufacturer; en; WIFI; $userId)"
    }
}
