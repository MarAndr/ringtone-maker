package com.example.ringtonemaker.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.ringtonemaker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BackPressedDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.backPressedDialogQuestion))
                .setPositiveButton(getString(R.string.backDialogPositiveButtonText)){ _, _->
                    requireActivity().finish()
                }
                .setNegativeButton(getString(R.string.backDialogNegativeButtonText)){ _, _->
                    findNavController().navigate(R.id.action_backPressedDialog_to_mainFragment)
                }
                .create()
    }
}