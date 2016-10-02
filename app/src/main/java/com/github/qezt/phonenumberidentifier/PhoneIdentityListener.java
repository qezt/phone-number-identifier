package com.github.qezt.phonenumberidentifier;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.android.volley.Response;
import com.github.qezt.phonenumberidentifier.databinding.FloatingInfoBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PhoneIdentityListener implements Response.Listener<String> {
    @Override
    public void onResponse(String response) {
        String location = extractLocation(response);
        String tag = extractTag(response);
        String desc = extractDesc(response);

        Context context = Application.instance();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final FloatingInfoBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.floating_info, null, false);

        binding.location.setText(location);
        if (TextUtils.isEmpty(tag)) {
            binding.tag.setVisibility(View.GONE);
        } else {
            binding.tag.setVisibility(View.VISIBLE);
            binding.tag.setText(tag);
        }

        if (TextUtils.isEmpty(desc)) {
            binding.tagDesc.setVisibility(View.GONE);
        } else {
            binding.tagDesc.setVisibility(View.VISIBLE);
            binding.tagDesc.setText(desc);
        }

        final WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        windowManager.addView(binding.getRoot(), layoutParams);
        final View[] popup = new View[]{binding.getRoot()};
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (popup[0] == null) return;
                popup[0] = null;
                windowManager.removeView(binding.getRoot());
            }
        }, 50000);
        binding.getRoot().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                binding.getRoot().getViewTreeObserver().removeOnPreDrawListener(this);
                binding.getRoot().setTranslationY(binding.getRoot().getHeight());
                binding.getRoot().animate().translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator()).start();
                return false;
            }
        });
//        binding.getRoot().setOnClickListener(new OnDebouncedClickListener() {
//            @Override
//            public void onDebouncedClickClick() {
//                if (popup[0] == null) return;
//                windowManager.removeView(binding.getRoot());
//                popup[0] = null;
//            }
//        });
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

    @Nullable
    private String extractLocation(String body) {
        Pattern pattern = Pattern.compile(
                "<div\\s+class=\"mh-tel-adr\"[^>]*>\\s*<p>([^<]*)</p>",
                Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(body);
        if (! matcher.find()) return null;
        String location = matcher.group(1);
        return location.replaceAll("[\n\\s]+", " ").trim();
    }

    private String extractTag(String body) {
        Pattern pattern = Pattern.compile(
                "<div\\s+class=\"mh-tel-mark\"[^>]*>\\s*([^<]+)\\s*</div>",
                Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(body);
        if (! matcher.find()) return null;
        return matcher.group(1).replaceAll("[\n\\s]+", " ").trim();
    }

    private String extractDesc(String body) {
        Pattern pattern = Pattern.compile(
                "<div\\s+class=\"mh-tel-desc\"[^>]*>\\s*(.+?)\\s*</div>",
                Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(body);
        if (! matcher.find()) return null;
        return matcher.group(1).replaceAll("<[^>]*?>", " ").replaceAll("\\s+", " ").trim();
    }
}
