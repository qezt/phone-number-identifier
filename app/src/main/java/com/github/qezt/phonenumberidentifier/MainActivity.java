package com.github.qezt.phonenumberidentifier;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.qezt.phonenumberidentifier.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_OVERLAY = 0;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // FIXME: this is API 23+
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, R.string.permission_request_draw_overlay, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_CODE_OVERLAY);
        } else {
            checkPhonePermission();
        }
        binding.number.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                request();
                return true;
            }
        });
    }

    private void checkPhonePermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (! report.areAllPermissionsGranted()) {
                            Toast.makeText(MainActivity.this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                Toast.makeText(MainActivity.this, "Showing Rationale", Toast.LENGTH_SHORT).show();
                        token.continuePermissionRequest();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_CODE_OVERLAY) return;
        if (Settings.canDrawOverlays(this)) {
            checkPhonePermission();
        } else {
            Toast.makeText(this, R.string.permission_request_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void request() {
        binding.statusTip.setOnClickListener(null);
        binding.tagContainer.setVisibility(View.GONE);
        binding.locationContainer.setVisibility(View.GONE);
        binding.statusTip.setVisibility(View.VISIBLE);
        binding.statusTip.setText(R.string.loading);
        RequestManager.instance().addRequest(new QueryPhoneIdRequest(binding.number.getText().toString(), new Response.Listener<PhoneNumberInfo>() {
            @Override
            public void onResponse(@Nullable PhoneNumberInfo phoneNumberInfo) {
                if (phoneNumberInfo == null) {
                    binding.tagContainer.setVisibility(View.GONE);
                    binding.locationContainer.setVisibility(View.GONE);
                    binding.statusTip.setVisibility(View.VISIBLE);
                    binding.statusTip.setText(R.string.unknown_number);
                    return;
                }
                binding.statusTip.setVisibility(View.GONE);
                binding.locationContainer.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(phoneNumberInfo.location)) {
                    binding.location.setText(R.string.unknown_number);
                } else {
                    binding.location.setText(phoneNumberInfo.location);
                }
                binding.tagContainer.setVisibility(View.GONE);
                if (TextUtils.isEmpty(phoneNumberInfo.tag)) {
                    binding.tag.setVisibility(View.GONE);
                } else {
                    binding.tagContainer.setVisibility(View.VISIBLE);
                    binding.tag.setVisibility(View.VISIBLE);
                    binding.tag.setText(phoneNumberInfo.tag);
                }

                if (TextUtils.isEmpty(phoneNumberInfo.tagDesc)) {
                    binding.tagDesc.setVisibility(View.GONE);
                } else {
                    binding.tagContainer.setVisibility(View.VISIBLE);
                    binding.tagDesc.setVisibility(View.VISIBLE);
                    binding.tagDesc.setText(phoneNumberInfo.tagDesc);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                binding.tagContainer.setVisibility(View.GONE);
                binding.locationContainer.setVisibility(View.GONE);
                binding.statusTip.setVisibility(View.VISIBLE);
                binding.statusTip.setText(R.string.tap_to_retry);
                binding.statusTip.setOnClickListener(new OnDebouncedClickListener() {
                    @Override
                    public void onDebouncedClickClick() {
                        request();
                    }
                });
            }
        }));
    }
}
