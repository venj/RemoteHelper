package me.venj.remotehelper

class APIServer constructor(_address: String = "localhost", _port: String = "4567", _userAgent: String = "remote-helper", _isSecure: Boolean = true) {
    var address = _address
    var port = _port
    var userAgent = _userAgent
    private var isSecure = _isSecure
    val scheme: String
    get() {
        return if (this.isSecure) "https" else "http"
    }
}
