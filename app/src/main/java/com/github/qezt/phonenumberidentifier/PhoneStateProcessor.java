package com.github.qezt.phonenumberidentifier;

import android.telephony.PhoneStateListener;

class PhoneStateProcessor extends PhoneStateListener {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (state != 1) return;
        // state = 1 means when phone is ringing
    }
}
