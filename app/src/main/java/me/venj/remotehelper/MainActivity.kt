package me.venj.remotehelper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.util.logging.Logger

fun AppCompatActivity.sharedPreferences() : SharedPreferences {
    val preferencesFile = "${packageName}_preferences"
    return getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
}

fun AppCompatActivity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(contentView?.windowToken, 0)
}

val Log = Logger.getLogger(MainActivity::class.java.name)

class MainActivity : AppCompatActivity(), KittenInputDialogFragment.KittenInputDialogListener {

    private lateinit var transmissionAddress: String
    private lateinit var transmissionPort: String
    private lateinit var transmissionUsername: String
    private lateinit var transmissionPassword: String

    private val magnetPrefix = "magnet:?xt=urn:btih:"

    private var sessionHeader = ""
    private var currentDownloadDir = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide keyboard while click on empty area.
        contentView?.setOnClickListener {
            hideKeyboard()
        }

        addMagnetButton.setOnClickListener {
            if (transmissionUsername.isEmpty() || transmissionPassword.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please transmission settings not fully set.", Toast.LENGTH_LONG).show()
            }
            hideKeyboard()
            val server = TransmissionServer(transmissionAddress, transmissionPort, transmissionUsername, transmissionPassword)
            val magnet = normalizeMagnet(editMagnet.text.toString())
            if (magnet == "") {
                clearEditMagnet()
                Toast.makeText(this@MainActivity, "Invalid magnet url!", Toast.LENGTH_LONG).show()
            }
            else {
                addTask(magnet, server, currentDownloadDir, {
                    runOnUiThread {
                        clearEditMagnet()
                        Toast.makeText(this@MainActivity, "Magnet added!", Toast.LENGTH_LONG).show()
                    }
                }, {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Failed to add magnet!", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        clearButton.setOnClickListener {
            hideKeyboard()
            clearEditMagnet()
        }
    }

    private fun clearEditMagnet() {
        editMagnet.text.clear()
    }

    private fun normalizeMagnet(_magnet: String) : String {
        val magnet = _magnet.trim()
        if (magnet.startsWith(magnetPrefix)) {
            return magnet
        }
        else if (magnet.matches(Regex("^[0-9A-Fa-f]{40}|[0-9A-Za-z]{32}$"))) {
            return "$magnetPrefix$magnet"
        }
        else {
            return ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menuSettings) {
            showSettings()
            return true
        }
        else if (item?.itemId == R.id.menuTorrents) {
            loadTorrentsList()
            return true
        }
        else if (item?.itemId == R.id.menuKitten) {
            showSearchPrompt()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val pref = sharedPreferences()
        transmissionAddress = pref.getString("transmission_address", "localhost") ?: ""
        transmissionPort = pref.getString("transmission_port", "9091") ?: ""
        transmissionUsername = pref.getString("transmission_username", "") ?: ""
        transmissionPassword = pref.getString("transmission_password", "") ?: ""
    }

    private fun showSearchPrompt() {
        val dialog = KittenInputDialogFragment()
        dialog.show(supportFragmentManager, "KittenDialog")
    }

    private fun loadTorrentsList() {
        listTorrents({ torrents ->
            val list = (torrents["items"] as JSONArray).toList() as List<String>
            val counts = (torrents["count"] as JSONArray).toList() as List<Int>

            val torrentsList = list.mapIndexed { i, o ->
                TorrentsListItem(o, counts[i])
            }
            Log.info("$torrentsList")
            runOnUiThread {
                showTorrentsList(torrentsList)
            }
        }, {
            val message = "Error loading torrents list."
            Log.warning(message)
            runOnUiThread {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        })
    }

    // KittenInputDialogListener

    override fun onDialogPositiveClick(dialog: DialogFragment, message: String) {
        Log.info("OK clicked in main activity: $message")
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Log.info("Cancel clicked in main activity.")
    }

    // List torrents
    private fun listTorrents(onSuccess: ((JSONObject) -> Unit)? = null, onFailure: (() -> Unit)? = null) {
        doAsync {
            // TODO("Read API server configuration from user settings.")
            val apiServer = APIServer()
            val client: OkHttpClient =
                OkHttpClient().newBuilder().addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val builder = originalRequest.newBuilder()
                    builder.header("User-Agent", apiServer.userAgent)
                    builder.get()
                    val newRequest = builder.build()
                    chain.proceed(newRequest)
                }.build()
            val url = "${apiServer.scheme}://${apiServer.address}:${apiServer.port}/torrents?stats=true"
            Log.info(url)
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.info("Response code: ${response.code()}")
                val responseString = response.body()?.string() ?: "{}"
                val obj = JSONObject(responseString)
                Log.info("JSON: ${obj}")
                onSuccess?.invoke(obj)
            }
            else {
                val code = response.code()
                Log.warning("Error fetch torrent list: $code")
                onFailure?.invoke()
            }
        }
    }

    // Show Settings Page.
    private fun showTorrentsList(list: List<TorrentsListItem>) {
        val torrentsListIntent = Intent(this, TorrentsListActivity::class.java)
        torrentsListIntent.putExtra("list", (list as Serializable))
        startActivity(torrentsListIntent)
    }

    // Show Settings Page.
    private fun showSettings() {
        val appSettings = Intent(this, SettingsActivity::class.java)
        startActivity(appSettings)
    }

    private fun addTask(magnet: String, toServer: TransmissionServer, downloadDir: String = "",  onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null) {
        Log.info("Adding magnet: $magnet")
        doAsync {
            val client: OkHttpClient =
                OkHttpClient().newBuilder().addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val builder = originalRequest.newBuilder()
                    // Basic auth
                    val username = toServer.username
                    val password = toServer.password
                    Log.info("Username: $username, password: $password")
                    if (!(username.isEmpty() && password.isEmpty())) {
                        builder.header("Authorization", Credentials.basic(username, password))
                    }
                    // Session Header
                    if (!sessionHeader.isEmpty()) {
                        Log.info("Add session id to header: $sessionHeader")
                        builder.header("X-Transmission-Session-Id", sessionHeader)
                    }

                    val params =
                        if (downloadDir == "") hashMapOf(Pair("method", "session-get"))
                        else hashMapOf(Pair("method", "torrent-add"), Pair("arguments", hashMapOf(Pair("download-dir", currentDownloadDir), Pair("paused", false), Pair("filename", magnet))))
                    val json = JSONObject(params).toString()
                    Log.info("Transmission request: $json")
                    val body = RequestBody.create(MediaType.get("application/json"), json)
                    builder.post(body)
                    val newRequest = builder.build()
                    chain.proceed(newRequest)
                }.build()
            Log.info(toServer.url)
            val request = Request.Builder().url(toServer.url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.info("Response code: ${response.code()}")
                val responseString = response.body()?.string() ?: "{}"
                val obj = JSONObject(responseString)
                Log.info("JSON: ${obj}")
                if (currentDownloadDir == "") { // Fetch download_dir
                    val dir = obj.getJSONObject("arguments").getString("download-dir")
                    if (dir == null) {
                        Log.warning("download-dir not found!")
                        onFailure?.invoke()
                    }
                    else {
                        currentDownloadDir = dir
                        // Recursive call!!!
                        Log.info("Current download-dir is $currentDownloadDir !")
                        addTask(magnet, toServer, currentDownloadDir, onSuccess, onFailure)
                    }
                }
                else {
                    val result = obj.getString("result")
                    if (result == "success") {
                        Log.info("Successfully added magnet.")
                        onSuccess?.invoke()
                    }
                    else {
                        Log.warning("Error add magnet: $result")
                        onFailure?.invoke()
                    }
                }
            }
            else {
                val code = response.code()
                if (code == 409) {
                    Log.warning("Get session id")
                    sessionHeader = response.header("X-Transmission-Session-Id") ?: ""
                    // Recursive call!!!
                    addTask(magnet, toServer, currentDownloadDir, onSuccess, onFailure)
                }
                else {
                    Log.warning("Error add task: $code")
                    onFailure?.invoke()
                }
            }
        }
    }
}

fun JSONArray.toList(): List<Any> = MutableList(length(), this::get)
