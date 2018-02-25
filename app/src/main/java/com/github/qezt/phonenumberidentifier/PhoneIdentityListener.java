package com.github.qezt.phonenumberidentifier;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.android.volley.Response;
import com.github.qezt.phonenumberidentifier.databinding.FloatingInfoBinding;

class PhoneIdentityListener implements Response.Listener<PhoneNumberInfo> {
    @Override
    public void onResponse(PhoneNumberInfo phoneNumberInfo) {
        Context context = Application.instance();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final FloatingInfoBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.floating_info, null, false);

        if (TextUtils.isEmpty(phoneNumberInfo.location)) {
            binding.location.setText(R.string.unknown_number);
        } else {
            binding.location.setText(phoneNumberInfo.location);
        }
        if (TextUtils.isEmpty(phoneNumberInfo.tag)) {
            binding.tag.setVisibility(View.GONE);
        } else {
            binding.tag.setVisibility(View.VISIBLE);
            binding.tag.setText(phoneNumberInfo.tag);
        }

        if (TextUtils.isEmpty(phoneNumberInfo.tagDesc)) {
            binding.tagDesc.setVisibility(View.GONE);
        } else {
            binding.tagDesc.setVisibility(View.VISIBLE);
            binding.tagDesc.setText(phoneNumberInfo.tagDesc);
        }
//        Toast.makeText(context, "hello, showing", Toast.LENGTH_LONG).show();

        final WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        layoutParams.flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;
//                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        windowManager.addView(binding.getRoot(), layoutParams);
        final View[] popup = new View[]{binding.getRoot()};
        // The floating window will hide after 10min
        final Handler handler = new Handler();
        final long startTime = SystemClock.elapsedRealtime();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (popup[0] == null) return;
                long now = SystemClock.elapsedRealtime();
                if (now - startTime > 60000 || ! CallReceiver.duringCall()) {
                    popup[0] = null;
                    windowManager.removeView(binding.getRoot());
                } else {
                    handler.postDelayed(this, 700);
                }
            }
        }, 700);
        binding.getRoot().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                binding.getRoot().getViewTreeObserver().removeOnPreDrawListener(this);
                binding.getRoot().setTranslationY(binding.getRoot().getHeight());
                binding.getRoot().animate().translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator()).start();
                return false;
            }
        });
        binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (popup[0] == null) return false;
                windowManager.removeView(binding.getRoot());
                popup[0] = null;
                return true;
            }
        });
    }

}
