package com.github.qezt.phonenumberidentifier

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.regex.Pattern


object QueryPhoneIdRequest {
    fun get(number : String) : PhoneNumberInfo? {
        val url = getUrl(number)
        val response = Requests.get(url) ?: return null
        if (response.statusCode != 200 || response.data.isEmpty()) return null
        return parse(response.data.toString(Charset.defaultCharset()))
    }

    private fun getUrl(number: String): String {
        return "https://m.so.com/index.php?q=" + try {
            URLEncoder.encode(number, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            ""
        }
    }

    private fun parse(body : String): PhoneNumberInfo? = PhoneNumberInfo(
            location = extractLocation(body),
            tag = extractTag(body),
            tagDesc = extractDesc(body)
    ).takeIf { it.location != null || it.tag != null || it.tagDesc != null }

    private fun extractLocation(body: String): String? {
        val pattern = Pattern.compile(
                "<div\\s+class=\"mh-tel-adr\"[^>]*>\\s*<p>([^<]*)</p>",
                Pattern.MULTILINE or Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(body)
        if (!matcher.find()) return null
        val location = matcher.group(1)
        return location.replace("[\n\\s]+".toRegex(), " ").trim { it <= ' ' }
    }

    private fun extractTag(body: String): String? {
        val pattern = Pattern.compile(
                "<div\\s+class=\"mh-tel-mark\"[^>]*>\\s*([^<]+)\\s*</div>",
                Pattern.MULTILINE or Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(body)
        return if (!matcher.find()) null else matcher.group(1).replace("[\n\\s]+".toRegex(), " ").trim { it <= ' ' }
    }

    private fun extractDesc(body: String): String? {
        val pattern = Pattern.compile(
                "<div\\s+class=\"mh-tel-desc\"[^>]*>\\s*(.+?)\\s*</div>",
                Pattern.MULTILINE or Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(body)
        return if (!matcher.find()) null else matcher.group(1).replace("<[^>]*?>".toRegex(), " ").replace("\\s+".toRegex(), " ").trim { it <= ' ' }
    }
}
