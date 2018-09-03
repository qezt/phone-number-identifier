package com.github.qezt.phonenumberidentifier

@Suppress("ArrayInDataClass")
data class Response (
        val statusCode : Int,
        val statusMessage : String,
        val headers : Map<String, List<String>>,
        val data : ByteArray
)