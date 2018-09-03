package com.github.qezt.phonenumberidentifier

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.Job

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.permission_request_draw_overlay, Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQ_CODE_OVERLAY)
        } else {
            checkPhonePermission()
        }
        numberEditText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode != KeyEvent.KEYCODE_ENTER || event.action != KeyEvent.ACTION_DOWN) {
                return@OnKeyListener false
            }
            request()
            true
        })
    }

    private fun checkPhonePermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(this@MainActivity, R.string.permission_request_denied, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        //                Toast.makeText(MainActivity.this, "Showing Rationale", Toast.LENGTH_SHORT).show();
                        token.continuePermissionRequest()
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_CODE_OVERLAY) return
        if (Settings.canDrawOverlays(this)) {
            checkPhonePermission()
        } else {
            Toast.makeText(this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val rootJob = Job()

    override fun onDestroy() {
        super.onDestroy()
        rootJob.cancel()
    }

    private fun request() {
        statusTip.setOnClickListener(null)
        tagContainer.visibility = View.GONE
        locationContainer.visibility = View.GONE
        statusTip.visibility = View.VISIBLE
        statusTip.setText(R.string.loading)

        Coroutine.launchOnUI(parent = rootJob) {
            val phoneNumberInfo = Coroutine.asyncOnWorkers {
                QueryPhoneIdRequest.get(numberEditText.text.toString())
            }.await()

            if (phoneNumberInfo == null) {
                tagContainer.visibility = View.GONE
                locationContainer.visibility = View.GONE
                statusTip.visibility = View.VISIBLE
                statusTip.setText(R.string.tap_to_retry)
                statusTip.setOnClickListener(object : OnDebouncedClickListener() {
                    override fun onDebouncedClickClick() {
                        request()
                    }
                })
                return@launchOnUI
            }
            statusTip.visibility = View.GONE
            locationContainer.visibility = View.VISIBLE
            if (TextUtils.isEmpty(phoneNumberInfo.location)) {
                location.setText(R.string.unknown_number)
            } else {
                location.text = phoneNumberInfo.location
            }
            tagContainer.visibility = View.GONE
            if (TextUtils.isEmpty(phoneNumberInfo.tag)) {
                tag.visibility = View.GONE
            } else {
                tagContainer.visibility = View.VISIBLE
                tag.visibility = View.VISIBLE
                tag.text = phoneNumberInfo.tag
            }

            if (TextUtils.isEmpty(phoneNumberInfo.tagDesc)) {
                tagDesc.visibility = View.GONE
            } else {
                tagContainer.visibility = View.VISIBLE
                tagDesc.visibility = View.VISIBLE
                tagDesc.text = phoneNumberInfo.tagDesc
            }
        }
    }

    companion object {
        private const val REQ_CODE_OVERLAY = 0
    }
}
