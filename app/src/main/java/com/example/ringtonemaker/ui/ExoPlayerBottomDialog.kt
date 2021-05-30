package com.example.ringtonemaker.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.ringtonemaker.databinding.DialogExoplayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class ExoPlayerBottomDialog: BottomSheetDialogFragment() {

    private lateinit var binding: DialogExoplayerBinding
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private var playWhenReady = true
    private val args: ExoFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogExoplayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
            initializePlayer()
    }

    override fun onPause() {
        super.onPause()
            releasePlayer()
    }

    override fun onStop() {
        super.onStop()
            releasePlayer()
    }


    private fun releasePlayer() {
            playWhenReady = simpleExoPlayer.playWhenReady
            simpleExoPlayer.release()
    }

    private fun initializePlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext())
                .build()
        binding.playerView.player = simpleExoPlayer
        val uri = Uri.fromFile(File(args.ringtonePath))
        val mediaItem = MediaItem.fromUri(uri)
        with(simpleExoPlayer){
            setMediaItem(mediaItem)
            prepare()
        }
        simpleExoPlayer.playWhenReady = playWhenReady
    }

}