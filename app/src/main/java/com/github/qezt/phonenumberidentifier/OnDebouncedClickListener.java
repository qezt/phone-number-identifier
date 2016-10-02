package com.github.qezt.phonenumberidentifier;


import android.os.SystemClock;
import android.view.View;

public abstract class OnDebouncedClickListener implements View.OnClickListener {
    private long coolDown;
    private long lastClickTime;

    public OnDebouncedClickListener() {
        this(300);
    }
    public OnDebouncedClickListener(int coolDown) {
        this.coolDown = coolDown;
    }

    @Override
    public void onClick(View v) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickTime < coolDown) return;
        lastClickTime = now;
        onDebouncedClickClick();
    }

    public abstract void onDebouncedClickClick();
}
