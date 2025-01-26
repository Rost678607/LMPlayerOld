package com.softwareforpeople.lm.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.media.MediaPlayer
import android.net.Uri
import android.widget.SeekBar
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Switch
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class player() {
    var palyer: MediaPlayer = MediaPlayer.create(this, )
    var currentSongID: Int = 0

    fun play(newID: Int) {
        if (newID != currentSongID) {

        }
    }

    fun resume() {

    }

    fun pause() {

    }

    fun next() {

    }

    fun prev() {

    }
}

class