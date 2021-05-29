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
                .setTitle("Вы хотите выйти из приложения или создать ещё один рингтон?")
                .setPositiveButton("Выйти"){_,_->
                    requireActivity().finish()
                }
                .setNegativeButton("Создать"){_,_->
                    findNavController().navigate(R.id.action_backPressedDialog_to_mainFragment)
                }
                .create()
    }
}