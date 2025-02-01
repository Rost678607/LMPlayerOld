package com.softwareforpeople.lm.model

import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast

class player() {
    private var currentSongID: UInt = 0U
    private var mp: MediaPlayer = MediaPlayer.create(this, getUriByID(currentSongID))

    private fun getUriByID(ID: UInt): Uri {

    }

    fun getCurrentSongID():UInt {
        return currentSongID
    }

    fun play(newSongID: UInt) {
        if (newSongID != currentSongID) {
            mp?.release()
            mp = MediaPlayer.create(this, getUriByID(newSongID))
            currentSongID = newSongID

        }
        mp?.start()
    }

    fun pause() {
        if (mp != null) {
            if (mp.isPlaying) {
                mp.pause()
            } else {
                mp.start()
            }
        } else {
            Toast.makeText(this, "Chouse sung", Toast.LENGTH_SHORT).show()
        }
    }

    fun next() {

    }

    fun prev() {

    }
}

class playqueue() {
    private var h = 7
    private lateinit var currentListItem: UInt
} 