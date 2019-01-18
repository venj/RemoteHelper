package me.venj.remotehelper

class TransmissionServer constructor(_address: String = "localhost", _port: String = "9091", _username: String = "", _password: String = "") {
    var address = _address
    var port = _port
    var username = _username
    var password = _password
}
