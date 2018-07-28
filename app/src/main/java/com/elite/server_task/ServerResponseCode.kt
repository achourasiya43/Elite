package com.elite.server_task

/**
 * Created by mindiii on 14/9/17.
 */

object ServerResponseCode {

    fun getmeesageCode(code: Int): String {
        var valueofmessage = ""
        when (code) {

            101 -> valueofmessage = "Continue"
            200 -> valueofmessage = "Ok"
            202 -> valueofmessage = "Accepted"
            203 -> valueofmessage = "Non-Authoritative Information"
            204 -> valueofmessage = "No Content"
            300 -> valueofmessage = "Multiple Choices"
            302 -> valueofmessage = "Found"
            304 -> valueofmessage = "Not Modified"
            305 -> valueofmessage = "Use Proxy"
            400 -> valueofmessage = "Your session is expired please login again"
            404 -> valueofmessage = "Not Found"
            502 -> valueofmessage = "Bad Gateway"
            503 -> valueofmessage = "Service Unavailable"
            504 -> valueofmessage = "Gateway Timeout"
            505 -> valueofmessage = "HTTP Version Not Supported"
        }
        return valueofmessage
    }


}
