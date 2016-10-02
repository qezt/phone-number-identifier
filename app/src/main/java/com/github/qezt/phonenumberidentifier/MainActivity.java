package com.github.qezt.phonenumberidentifier;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_OVERLAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_CODE_OVERLAY);
        } else {
            checkPhonePermission();
        }
    }

    private void checkPhonePermission() {
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (! report.areAllPermissionsGranted()) {
                    Toast.makeText(MainActivity.this, "Denied", Toast.LENGTH_SHORT).show();
                    return;
                }
                populate();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                Toast.makeText(MainActivity.this, "Showing Rationale", Toast.LENGTH_SHORT).show();
                token.continuePermissionRequest();
            }
        }, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_CODE_OVERLAY) return;
        if (Settings.canDrawOverlays(this)) {
            checkPhonePermission();
        }
    }

    private void populate() {
//        new PhoneStateProcessor().onCallStateChanged(1, "13256719945");
    }
}
