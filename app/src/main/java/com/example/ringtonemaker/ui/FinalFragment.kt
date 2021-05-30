package com.example.ringtonemaker.ui

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ringtonemaker.R

import com.example.ringtonemaker.ViewBindingFragment
import com.example.ringtonemaker.databinding.FragmentFinalBinding
import com.example.ringtonemaker.utils.createSnackBar

class FinalFragment: ViewBindingFragment<FragmentFinalBinding>(FragmentFinalBinding::inflate) {

    private val args: FinalFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFinalFragmentPlayRingtone.setOnClickListener {
            val action = FinalFragmentDirections.actionFinalFragmentToExoPlayerBottomDialog(args.ringtonePath)
            findNavController().navigate(action)
        }

        binding.buttonFinalFragmentSetAsACallMelody.setOnClickListener {
            setRingtone(args.ringtoneUri)
        }

        binding.textViewFinalFragmentRingtoneName.text = args.ringtoneName
        binding.textViewFinalFragmentRingtonePath.text = args.ringtonePath

        requireActivity().onBackPressedDispatcher.addCallback{
            findNavController().navigate(R.id.action_finalFragment_to_backPressedDialog2)
            isEnabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setRingtone(uri: Uri?){
        if (Settings.System.canWrite(requireContext())){
            createSnackBar(getString(R.string.ringtoneSettedAsPhoneMelody))
            RingtoneManager.setActualDefaultRingtoneUri(
                requireContext(),
                RingtoneManager.TYPE_RINGTONE,
                uri
            )
        } else {
            openAndroidPermissionsMenu()
            createSnackBar(getString(R.string.ringtoneSetAsMelodyError))
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + requireActivity().packageName)
        startActivity(intent)
    }

}