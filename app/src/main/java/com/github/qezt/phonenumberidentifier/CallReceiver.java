package com.github.qezt.phonenumberidentifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (state == null || ! state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            return;
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        Cursor cursor = Application.instance().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) return;
            } finally {
                cursor.close();
            }
        }

        RequestManager.instance().addRequest(new QueryPhoneIdRequest(incomingNumber, new PhoneIdentityListener(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Application.instance(), "Bummer", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}

