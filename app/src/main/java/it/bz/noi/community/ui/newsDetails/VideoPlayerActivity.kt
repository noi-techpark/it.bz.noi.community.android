// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.ui.newsDetails

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import it.bz.noi.community.databinding.ActivityVideoPlayerBinding

class VideoPlayerActivity : AppCompatActivity() {
	private var player: ExoPlayer? = null
	private lateinit var binding: ActivityVideoPlayerBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: return finish()

		binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
		setContentView(binding.root)

		initializePlayer(videoUrl)
	}

	private fun initializePlayer(videoUrl: String) {
		player = ExoPlayer.Builder(this)
			.build()
			.also { exoPlayer ->
				binding.videoPlayer.player = exoPlayer
				val mediaItem = MediaItem.fromUri(videoUrl)
				exoPlayer.setMediaItem(mediaItem)
				exoPlayer.playWhenReady = true
				//exoPlayer.addListener(playbackStateListener)
				exoPlayer.prepare()
			}
	}

	override fun onPause() {
		super.onPause()
		player?.pause()
	}

	override fun onDestroy() {
		super.onDestroy()
		player?.release()
	}

	companion object {

		private const val EXTRA_VIDEO_URL = "video_url"

		fun createIntent(context: Context, videoUrl: String): Intent {
			return Intent(context, VideoPlayerActivity::class.java).apply {
				putExtra(EXTRA_VIDEO_URL, videoUrl)
			}
		}
	}
}
