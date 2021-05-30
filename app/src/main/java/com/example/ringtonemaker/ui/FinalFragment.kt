package com.example.ringtonemaker.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ringtonemaker.R
import com.example.ringtonemaker.ViewBindingFragment
import com.example.ringtonemaker.const.Constants
import com.example.ringtonemaker.databinding.FragmentFinalBinding
import com.example.ringtonemaker.utils.createSnackBar


class FinalFragment : ViewBindingFragment<FragmentFinalBinding>(FragmentFinalBinding::inflate) {

    private val args: FinalFragmentArgs by navArgs()
    private lateinit var ringtoneName: String
    private lateinit var ringtonePath: String
    private lateinit var ringtoneUri: Uri



    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ringtoneName = args.ringtoneName
        ringtonePath = args.ringtonePath
        ringtoneUri = args.ringtoneUri
        super.onViewCreated(view, savedInstanceState)
        binding.buttonFinalFragmentPlayRingtone.setOnClickListener {
            val action =
                FinalFragmentDirections.actionFinalFragmentToExoPlayerBottomDialog(ringtonePath)
            findNavController().navigate(action)
        }

        binding.buttonFinalFragmentSetAsACallMelody.setOnClickListener {
            setRingtone(requireActivity(), ringtoneUri)
        }

        binding.textViewFinalFragmentRingtoneName.text = ringtoneName
        binding.textViewFinalFragmentRingtonePath.text = ringtonePath

        requireActivity().onBackPressedDispatcher.addCallback {
            findNavController().navigate(R.id.action_finalFragment_to_backPressedDialog2)
            isEnabled = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.CODE_WRITE_SETTINGS_PERMISSION && Settings.System.canWrite(requireContext())){
            setActualDefaultRingtone(requireContext(), ringtoneUri)
        }
    }

    private fun openAndroidPermissionsMenu(context: Activity) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + context.packageName)
        context.startActivityForResult(intent, Constants.CODE_WRITE_SETTINGS_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CODE_WRITE_SETTINGS_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setActualDefaultRingtone(requireContext(), ringtoneUri)
        }
    }

    private fun setRingtone(context: Activity, ringtoneUri: Uri) {
        val permission: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (permission) {
            try {
                setActualDefaultRingtone(requireContext(), ringtoneUri)
                createSnackBar(getString(R.string.ringtoneSettedAsPhoneMelody))
            } catch (e: Exception) {
                createSnackBar(getString(R.string.ringtoneSetAsMelodyError))
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openAndroidPermissionsMenu(context)
            } else {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.WRITE_SETTINGS),
                    Constants.CODE_WRITE_SETTINGS_PERMISSION
                )
            }
        }
    }

    private fun setActualDefaultRingtone(context: Context, ringtoneUri: Uri){
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            ringtoneUri
        )
    }

}