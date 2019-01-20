package me.venj.remotehelper

class APIServer constructor(val address: String = "localhost", val port: String = "4567", val userAgent: String = "remote-helper", private val isSecure: Boolean = true) {
    val scheme: String
    get() {
        return if (this.isSecure) "https" else "http"
    }
}
