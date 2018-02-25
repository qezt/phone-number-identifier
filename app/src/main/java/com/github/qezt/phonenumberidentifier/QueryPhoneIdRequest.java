package com.github.qezt.phonenumberidentifier;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QueryPhoneIdRequest extends Request<PhoneNumberInfo> {
    private Response.Listener<PhoneNumberInfo> listener;

    QueryPhoneIdRequest(String number, Response.Listener<PhoneNumberInfo> listener, Response.ErrorListener errorListener) {
        super(Method.GET, getUrl(number), errorListener);
        this.listener = listener;
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
    protected Response<PhoneNumberInfo> parseNetworkResponse(NetworkResponse response) {
        String body;
        try {
            body = new String(response.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            body = new String(response.data);
        }
        PhoneNumberInfo phoneNumberInfo = new PhoneNumberInfo();
        phoneNumberInfo.location = extractLocation(body);
        phoneNumberInfo.tag = extractTag(body);
        phoneNumberInfo.tagDesc = extractDesc(body);
        if (phoneNumberInfo.location == null && phoneNumberInfo.tag == null && phoneNumberInfo.tagDesc == null) {
            return null;
        }
        return Response.success(phoneNumberInfo, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(PhoneNumberInfo response) {
        listener.onResponse(response);
    }

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
