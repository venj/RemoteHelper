package me.venj.remotehelper

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.EditText
import android.widget.LinearLayout

class KittenInputDialogFragment : DialogFragment() {

    interface KittenInputDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, message: String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    var kittenListener: KittenInputDialogListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.info("Attaching dialog to main activity.")
        try {
            kittenListener = context as? KittenInputDialogListener
        }
        catch (e: ClassCastException) {
            throw java.lang.ClassCastException(activity.toString() + " must implement KittenInputDialogListener.")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater!!.inflate(R.layout.kitten_dialog, null) as? LinearLayout
        val keywordsField = view!!.findViewById<EditText>(R.id.kitten_keywords)
        dialog.setMessage("Please input search keywords.")
        dialog.setView(view)
            .setPositiveButton(R.string.dialog_ok_button) { _, _ ->
                val keywords = keywordsField.text.toString()
                kittenListener?.onDialogPositiveClick(this, keywords)
                Log.info("OK button clicked.")
            }
            .setNegativeButton(R.string.dialog_cancel_button) { _, _ ->
                kittenListener?.onDialogNegativeClick(this)
                Log.info("Cancel button clicked.")
            }
        return dialog.create()
    }
}
