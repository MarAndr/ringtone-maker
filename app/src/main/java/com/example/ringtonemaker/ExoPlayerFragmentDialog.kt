package com.example.ringtonemaker

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ringtonemaker.databinding.DialogExoplayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ExoPlayerFragmentDialog : DialogFragment(), Player.EventListener {

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var mView: View
    private var binding: DialogExoplayerBinding? = null
    private var playBackPosition: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DialogExoplayerBinding.inflate(layoutInflater)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mView = layoutInflater.inflate(R.layout.dialog_exoplayer, null)
        return MaterialAlertDialogBuilder(requireContext())
                .setView(binding?.root)
                .create()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || simpleExoPlayer == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private var playWhenReady = true;
    private var currentWindow = 0;
    private var playbackPosition: Long = 0;

    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            playWhenReady = simpleExoPlayer!!.getPlayWhenReady();
            playbackPosition = simpleExoPlayer!!.getCurrentPosition();
            currentWindow = simpleExoPlayer!!.getCurrentWindowIndex();
            simpleExoPlayer!!.release();
            simpleExoPlayer = null;
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        binding?.playerView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }


    private fun initializePlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext())
                .build()
        binding?.playerView?.player = simpleExoPlayer
        val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
        simpleExoPlayer!!.setMediaItem(mediaItem)
        simpleExoPlayer!!.setPlayWhenReady(playWhenReady);
        simpleExoPlayer!!.seekTo(currentWindow, playbackPosition);
        simpleExoPlayer!!.prepare();
    }
}