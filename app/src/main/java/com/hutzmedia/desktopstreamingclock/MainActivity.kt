/**
 * Developed by Hutz Media Ltd. <info@hutzmedia.com>
 * Copyright 2023-01-05
 * See README.md
 */
package com.hutzmedia.desktopstreamingclock

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : AppCompatActivity() {
    companion object {
        const val rtspUrl = "rtsp://192.168.1.75:8554/live"
        const val tag = "MEDIA_PLAYER_LOG"
    }

    private var mLibVLC: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null

    private var timerTextView : TimerTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(tag,"onCreate()")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById<View>(
            R.id.textView
        ) as TimerTextView
    }

    /**
     * Initialize Player View
     */
    private fun initPlayerView() {
        // set libvlc options
        mLibVLC = LibVLC(this, ArrayList<String>().apply {
            add("--no-drop-late-frames")
            //add("--no-skip-frames") // let player skip frames
            add("--rtsp-tcp")
            add("-vvv")
        })
        mMediaPlayer = MediaPlayer(mLibVLC)

        // add media player to vlc video layout
        val layout: VLCVideoLayout = findViewById<View>(R.id.view_vlc_layout) as VLCVideoLayout
        mMediaPlayer?.attachViews(layout, null, false, false)
        mMediaPlayer?.videoScale = MediaPlayer.ScaleType.SURFACE_FIT_SCREEN // scale to fill screen

        try {
            // listen to media player events
            mMediaPlayer?.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Opening -> onOpening()
                    MediaPlayer.Event.Buffering -> Log.i(tag, "Event Buffering=" + event.buffering)
                    MediaPlayer.Event.Stopped -> onStopped()
                    MediaPlayer.Event.EncounteredError -> onEncounteredError()
                    MediaPlayer.Event.EndReached -> onEndReached()
                }
            }

            startStream()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Start Stream
     */
    private fun startStream() {
        Log.i(tag, "Starting Stream")

        val uri = Uri.parse(rtspUrl)

        Media(mLibVLC, uri).apply {
            setHWDecoderEnabled(true, true)
            addOption(":network-caching=0") // less latency
            addOption(":clock-jitter=0")
            addOption(":clock-synchro=0")
            addOption(":rtsp-timeout=3") // 3s timeout
            mMediaPlayer?.media = this
        }.release()

        mMediaPlayer?.play()
    }

    /**
     * Activity stopped. Stop play and detach views.
     */
    override fun onStop() {
        Log.i(tag,"onStop()")
        super.onStop()

        // stop stream and detach to free memory
        mMediaPlayer?.stop()
        mMediaPlayer?.detachViews()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mLibVLC?.release()
        mLibVLC = null
    }

    /**
     * Activity resumed.
     */
    override fun onResume() {
        Log.i(tag,"onResume()")
        super.onResume()

        // reinitialize player and resume stream
        initPlayerView()
    }

    /**
     * Activity destroyed. Release memory.
     */
    override fun onDestroy() {
        super.onDestroy()

        // stop player
        mMediaPlayer?.stop()
        mMediaPlayer?.detachViews()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mLibVLC?.release()
        mLibVLC = null

        // stop timer
        stopTimer()
    }

    /**
     * Event: Stream is opening.
     */
    private fun onOpening() {
        Log.i(tag, "Event Opening")
    }

    /**
     * Event: Stream has encountered an error.
     */
    private fun onEncounteredError() {
        Log.i(tag, "Event EncounteredError")
        // onStopped() will usually be called with EncounteredError
    }

    /**
     * Event: Stream has stopped. Attempt to reconnect.
     */
    private fun onStopped() {
        // reconnect to stream
        Log.i(tag, "Event Stopped")
        startStream()
    }

    /**
     * Event: Stream has reached end / stopped. Attempt to reconnect.
     */
    private fun onEndReached() {
        // reconnect to stream
        Log.i(tag, "Event EndReached")
        startStream()
    }

    /**
     * Restart timer
     */
    private fun restartTimer() {
        timerTextView?.restart()
    }

    /**
     * Stop timer
     */
    private fun stopTimer() {
        timerTextView?.stop()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            restartTimer()
        }

        // call super
        return super.dispatchTouchEvent(ev)
    }
}