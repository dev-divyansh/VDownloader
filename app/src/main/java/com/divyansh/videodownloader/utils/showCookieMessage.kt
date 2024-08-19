package com.divyansh.videodownloader.utils

import android.app.Activity
import com.divyansh.videodownloader.R
import org.aviran.cookiebar2.CookieBar

fun showCookieMessage(activity: Activity, message: String) {
    CookieBar.build(activity)
        .setTitle("Message")
        .setCookiePosition(CookieBar.TOP)
        .setBackgroundColor(R.color.blue)
        .setTitleColor(R.color.white)
        .setMessage(message)
        .show()
}