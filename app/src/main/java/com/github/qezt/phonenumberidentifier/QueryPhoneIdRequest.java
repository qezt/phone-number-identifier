package com.github.qezt.phonenumberidentifier;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class QueryPhoneIdRequest extends StringRequest {
    QueryPhoneIdRequest(String number, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(getUrl(number), listener, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(2000, 100, 1));
    }

    private static String getUrl(String number) {
        try {
            return "https://m.so.com/index.php?q=" + URLEncoder.encode(number, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return "https://m.so.com/index.php?q=";
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}
