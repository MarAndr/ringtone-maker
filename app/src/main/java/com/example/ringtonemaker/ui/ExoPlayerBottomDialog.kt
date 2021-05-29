package com.example.ringtonemaker.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.ringtonemaker.databinding.DialogExoplayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class ExoPlayerBottomDialog: BottomSheetDialogFragment() {

    private lateinit var binding: DialogExoplayerBinding
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private var playWhenReady = true;
    private var currentWindow = 0;
    private var playbackPosition: Long = 0;
    val args: ExoFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogExoplayerBinding.inflate(layoutInflater)
        return binding.root
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


    private fun releasePlayer() {
        if (simpleExoPlayer != null) {
            playWhenReady = simpleExoPlayer.getPlayWhenReady();
            playbackPosition = simpleExoPlayer.getCurrentPosition();
            currentWindow = simpleExoPlayer.getCurrentWindowIndex();
            simpleExoPlayer.release();
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        binding.playerView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }


    private fun initializePlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext())
                .build()
        binding.playerView.player = simpleExoPlayer
        val uri = Uri.fromFile(File(args.ringtonePath))
        val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
        val mediaItem2 = MediaItem.fromUri(uri)
        simpleExoPlayer.setMediaItem(mediaItem2)
        simpleExoPlayer.playWhenReady = playWhenReady;
        simpleExoPlayer.seekTo(currentWindow, playbackPosition);
        simpleExoPlayer.prepare();
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(
                        requireContext(), Util.getUserAgent(
                        requireContext(),
                        "simpleExoPlayer"
                )
                )
    }

}