package com.github.qezt.phonenumberidentifier

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator

import kotlinx.android.synthetic.main.floating_info.view.*

object PhoneIdentityPopup {
    fun show(phoneNumberInfo: PhoneNumberInfo) {
        val context = Application.app
        val binding = LayoutInflater.from(context).inflate(R.layout.floating_info, null, false)

        if (TextUtils.isEmpty(phoneNumberInfo.location)) {
            binding.location.setText(R.string.unknown_number)
        } else {
            binding.location.text = phoneNumberInfo.location
        }
        if (TextUtils.isEmpty(phoneNumberInfo.tag)) {
            binding.tagName.visibility = View.GONE
        } else {
            binding.tagName.setVisibility(View.VISIBLE)
            binding.tagName.setText(phoneNumberInfo.tag)
        }

        if (TextUtils.isEmpty(phoneNumberInfo.tagDesc)) {
            binding.tagDesc.setVisibility(View.GONE)
        } else {
            binding.tagDesc.setVisibility(View.VISIBLE)
            binding.tagDesc.setText(phoneNumberInfo.tagDesc)
        }
        //        Toast.makeText(context, "hello, showing", Toast.LENGTH_LONG).show();

        val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams()
        //        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        //        layoutParams.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        layoutParams.flags = (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        //                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        //                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        //                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        //        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        windowManager.addView(binding, layoutParams)
        val popup = arrayOf<View?>(binding)
        // The floating window will hide after 10min
        val handler = Handler()
        val startTime = SystemClock.elapsedRealtime()
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (popup[0] == null) return
                val now = SystemClock.elapsedRealtime()
                if (now - startTime > 60000 || !CallReceiver.duringCall()) {
                    popup[0] = null
                    windowManager.removeView(binding)
                } else {
                    handler.postDelayed(this, 700)
                }
            }
        }, 700)
        binding.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.viewTreeObserver.removeOnPreDrawListener(this)
                binding.translationY = binding.height.toFloat()
                binding.animate().translationY(0F).setDuration(1000).setInterpolator(DecelerateInterpolator()).start()
                return false
            }
        })
        binding.setOnLongClickListener(View.OnLongClickListener {
            if (popup[0] == null) return@OnLongClickListener false
            windowManager.removeView(binding)
            popup[0] = null
            true
        })
    }

}
