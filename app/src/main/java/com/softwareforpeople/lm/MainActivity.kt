package com.softwareforpeople.lm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.media.MediaPlayer
import android.widget.SeekBar
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    // функция замены фрагментов
    private fun replaceFragment(fragment: Fragment) {
        Log.d("Fragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // переменные


        // непонятные переменные (но они нужные)
        val REQUEST_CODE = 1001

        // кнопки
        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, ListFragment())
                .commit()
        }

        // запрос разрешения на чтение аудиофайлов с телефона
        // Android 13 и выше
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), REQUEST_CODE)
        // Android 12 и ниже
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }

        // обработка нажатий кнопок меню
        navigation.setOnItemSelectedListener { item ->
            Log.d("Navigation", "Item selected: ${item.itemId}")
            when (item.itemId) {
                R.id.menu_list -> {
                    replaceFragment(ListFragment())
                    true
                }
                R.id.menu_play -> {
                    replaceFragment(PlayFragment())
                    true
                }
                R.id.menu_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
}

class ListFragment : Fragment() { // фрагмент списка терков

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_list, container, false)
    }
}

class PlayFragment : Fragment() {

    private var player: MediaPlayer? = null
    private lateinit var buttonPlay: View // Объявляем переменную для кнопки
    private lateinit var seekBar: SeekBar // Объявляем переменную для SeekBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // переменные
        val currentSong = mutableListOf(R.raw.song)

        // кнопки
        buttonPlay = view.findViewById(R.id.button_play)
        seekBar = view.findViewById(R.id.seekBar)

        // механика кнопки play
        buttonPlay.setOnClickListener {
            if (player == null) {
                player = MediaPlayer.create(requireContext(), currentSong[0])
                player!!.start()
                buttonPlay.setBackgroundResource(R.drawable.baseline_pause_circle_24)
            } else {
                if (player!!.isPlaying) {
                    player!!.pause()
                    buttonPlay.setBackgroundResource(R.drawable.baseline_play_circle_24)
                } else {
                    player!!.start()
                    buttonPlay.setBackgroundResource(R.drawable.baseline_pause_circle_24)
                }
            }
        }

        // Механика SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && player != null) {
                    player!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Обновление SeekBar во время проигрывания
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (player != null && player!!.isPlaying) {
                    seekBar.progress = player!!.currentPosition
                    seekBar.max = player!!.duration
                }
                handler.postDelayed(this, 1000) // обновление каждую секунду
            }
        })
    }
}

class SettingsFragment : Fragment() { // фрагмент настроек

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_settings, container, false)
    }
}