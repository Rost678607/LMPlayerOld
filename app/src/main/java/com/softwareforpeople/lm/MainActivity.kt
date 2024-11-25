package com.softwareforpeople.lm

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import android.net.Uri
import android.widget.SeekBar
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView

var currentSong = mutableListOf(R.raw.song) // текущий трек

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // переменные
        var currentFragment: Fragment // текущий фрагмент

        // непонятные переменные (но они нужные)
        // val REQUEST_CODE: Int = 1001 // нужно для запроса разрешения на чтение аудиофайлов с телефона

        // кнопки
        val navigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // инициализация фрагментов и выбор фрагмента по умолчанию
        val listFragment = ListFragment()
        val playFragment = PlayFragment()
        val settingsFragment = SettingsFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, listFragment)
            .add(R.id.fragment_container, playFragment)
            .add(R.id.fragment_container, settingsFragment)
            .hide(playFragment)
            .hide(settingsFragment)
            .commit()
        currentFragment = listFragment

        // запрос разрешения на чтение аудиофайлов с телефона
        // Android 13 и выше
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1001)
        // Android 12 и ниже
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001)
        }

        // обработка нажатий кнопок меню (переключение между фрагментами)
        navigation.setOnItemSelectedListener { item ->
            Log.d("Navigation", "Item selected: ${item.itemId}")
            when (item.itemId) {
                R.id.menu_list -> {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .show(listFragment)
                        .commit()
                    currentFragment = listFragment
                    true
                }
                R.id.menu_play -> {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .show(playFragment)
                        .commit()
                    currentFragment = playFragment
                    true
                }
                R.id.menu_settings -> {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .show(settingsFragment)
                        .commit()
                    currentFragment = settingsFragment
                    true
                }
                else -> false
            }
        }
    }
}

class ListFragment : Fragment() { // фрагмент списка терков

    private lateinit var adapter: SongListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // кнопки
        val addSongButton: FloatingActionButton = view.findViewById(R.id.add_song)

        adapter = SongListAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.music_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // обработка нажатия на кнопку добавления трека и добавление его в список
        addSongButton.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                addSongFromUri(uri)
            } ?: run {
                data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        addSongFromUri(uri)
                    }
                }
            }
        }
    }

    private fun addSongFromUri(uri: Uri) {
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val name = if (nameIndex != -1) it.getString(nameIndex) else "Музыка"
                val artist = if (artistIndex != -1) it.getString(artistIndex) else "нн"

                // Проверка на наличие трека в списке
                val existingSong = adapter.songs.find { it.name == name && it.author == artist }
                if (existingSong == null) {
                    adapter.addSong(song_list_item(name, artist))
                } else {
                    // Трек уже есть в списке, можно показать сообщение пользователю
                    Toast.makeText(requireContext(), "Нахуя тебе два одинаковых трека даун", Toast.LENGTH_SHORT).show()
                }
            }
            it.close()
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_AUDIO = 1
    }
}

class PlayFragment : Fragment() { // фрагмент проигрывателя

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

        // кнопки
        buttonPlay = view.findViewById(R.id.button_play)
        seekBar = view.findViewById(R.id.seekBar)

        // механика кнопки play (эти костыли надо будет переписать)
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