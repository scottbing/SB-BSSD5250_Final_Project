package edu.nmhu.bssd5250.sb_maps_demo

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteConfirmationDialogFragment : DialogFragment() {
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment?)
        fun onDialogNegativeClick(dialog: DialogFragment?)
    }

    // Use this instance of the interface to deliver action events
    private var mListener: NoticeDialogListener? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Build the dialog and set up the button click handlers
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        builder.setMessage(R.string.dialog_delete_confirm)
            .setPositiveButton(
                R.string.ok
            ) { dialog, id -> // Send the positive button event back to the host activity
                mListener!!.onDialogPositiveClick(this@DeleteConfirmationDialogFragment)
            }
            .setNegativeButton(R.string.cancel
            ) { dialog, id -> // Send the negative button event back to the host activity
                mListener!!.onDialogNegativeClick(this@DeleteConfirmationDialogFragment)
            }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val a: Activity
        if (context is Activity) {
            a = context
        }
        // Verify that the host activity implements the callback interface
        mListener = try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                context.toString()
                        + " must implement NoticeDialogListener"
            )
        }
    }
}