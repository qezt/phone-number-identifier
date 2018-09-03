package com.github.qezt.phonenumberidentifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.widget.Toast

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        CallReceiver.state = state
        if (state != TelephonyManager.EXTRA_STATE_RINGING) {
            return
        }
        if (incomingNumber == null) {
            Toast.makeText(Application.app, "Number is empty", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(Application.app, "Number is $incomingNumber", Toast.LENGTH_LONG).show()

        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber))
        val cursor = Application.app.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
        if (cursor != null) {
            try {
                if (cursor.count > 0) return
            } finally {
                cursor.close()
            }
        }

        Coroutine.launchOnUI {
            val result = Coroutine.asyncOnWorkers {
                QueryPhoneIdRequest.get(incomingNumber)
            }.await()
            if (result == null) {
                Toast.makeText(Application.app, "Bummer", Toast.LENGTH_SHORT).show()
                return@launchOnUI
            }
            PhoneIdentityPopup.show(result)
        }
    }

    companion object {
        private var state: String? = null
        fun duringCall(): Boolean {
            return state == null || state != TelephonyManager.EXTRA_STATE_IDLE
        }
    }
}

