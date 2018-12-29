package me.venj.remotehelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.contentView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set title for Settings.
        setTitle(R.string.menu_settings)

        // Hide keyboard while click on empty area.
        contentView?.setOnClickListener {
            hideKeyboard()
        }

        saveButton.setOnClickListener {
            val editor = sharedPreferences().edit()
            editor.putString("transmission_address", editAddress.text.toString())
            editor.putString("transmission_port", editPort.text.toString())
            editor.putString("transmission_username", editUsername.text.toString())
            editor.putString("transmission_password", editPassword.text.toString())
            editor.apply()

            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val pref = sharedPreferences()
        editAddress.text = (pref.getString("transmission_address", "") ?: "").toEditable()
        editPort.text = (pref.getString("transmission_port", "") ?: "").toEditable()
        editUsername.text = (pref.getString("transmission_username", "") ?: "").toEditable()
        editPassword.text = (pref.getString("transmission_password", "") ?: "").toEditable()
    }
}

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)