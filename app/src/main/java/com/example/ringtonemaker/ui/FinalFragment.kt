package com.example.ringtonemaker.ui

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ringtonemaker.R

import com.example.ringtonemaker.ViewBindingFragment
import com.example.ringtonemaker.databinding.FragmentFinalBinding
import com.example.ringtonemaker.utils.createSnackBar
import timber.log.Timber

class FinalFragment: ViewBindingFragment<FragmentFinalBinding>(FragmentFinalBinding::inflate) {

    val args: FinalFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFinalFragmentPlayRingtone.setOnClickListener {
            val action = FinalFragmentDirections.actionFinalFragmentToExoFragment(args.ringtoneUri.path!!)
            findNavController().navigate(action)
        }

        binding.buttonFinalFragmentSetAsACallMelody.setOnClickListener {
            setRingtone2(args.ringtoneUri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setRingtone2(uri: Uri?){
        if (Settings.System.canWrite(requireContext())){
            createSnackBar("canWrite = true")
            RingtoneManager.setActualDefaultRingtoneUri(
                requireContext(),
                RingtoneManager.TYPE_RINGTONE,
                uri
            )
        } else {
            openAndroidPermissionsMenu()
            createSnackBar("canWrite = false")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + requireActivity().packageName)
        startActivity(intent)
    }
}