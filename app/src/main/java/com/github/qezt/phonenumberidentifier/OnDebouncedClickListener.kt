package com.github.qezt.phonenumberidentifier


import android.os.SystemClock
import android.view.View

abstract class OnDebouncedClickListener(private val coolDown: Long = 300) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime < coolDown) return
        lastClickTime = now
        onDebouncedClickClick()
    }

    abstract fun onDebouncedClickClick()
}
