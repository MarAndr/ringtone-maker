package com.example.ringtonemaker

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract.Directory
import android.view.View
import com.example.ringtonemaker.databinding.FragmentExoplayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File


class ExoFragment: ViewBindingFragment<FragmentExoplayerBinding>(FragmentExoplayerBinding::inflate) {
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private var playWhenReady = true;
    private var currentWindow = 0;
    private var playbackPosition: Long = 0;


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
        binding.playerView2.setSystemUiVisibility(
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
        binding.playerView2.player = simpleExoPlayer
//        Uri.fromFile(new File(path))
        val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
        simpleExoPlayer.setMediaItem(mediaItem)
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