package com.softwareforpeople.lm

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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

var currentSong: Uri = Uri.EMPTY // текущий трек
private lateinit var currentFragment: Fragment // текущий фрагмент
private lateinit var oldSong: Uri
var player: MediaPlayer? = null

class MainActivity : AppCompatActivity(), OnSongClickListener {
    lateinit var playFragment: PlayFragment
    lateinit var listFragment: ListFragment
    lateinit var settingsFragment: SettingsFragment

    lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // кнопки
        navigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        oldSong = currentSong

        // инициализация фрагментов и выбор фрагмента по умолчанию
        listFragment = ListFragment()
        playFragment = PlayFragment()
        settingsFragment = SettingsFragment()
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
    /*fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        currentFragment = fragment
    }*/

    override fun onSongClick(songUri: Uri) {
        currentSong = songUri
        navigation.setSelectedItemId(R.id.menu_play)
        supportFragmentManager.beginTransaction()
            .hide(currentFragment)
            .show(playFragment)
            .commit()
        currentFragment = PlayFragment()
        player?.release()
        player = MediaPlayer.create(this, currentSong)
        player!!.start()
        playFragment.playButton.setBackgroundResource(R.drawable.baseline_pause_circle_24)
        oldSong = currentSong
    }
}

class ListFragment : Fragment() { // фрагмент списка терков

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSongs() {
        val sharedPreferences = requireActivity().getSharedPreferences("songs", Context.MODE_PRIVATE) // или this.getSharedPreferences(...) в активити
        val gson = Gson()
        val json = sharedPreferences.getString("songs_list", null)
        val type = object : TypeToken<List<song_list_item>>() {}.type
        val songs = gson.fromJson<List<song_list_item>>(json, type) ?: emptyList()
        adapter.songs.clear() // Очищаем текущий список песен в адаптере
        adapter.songs.addAll(songs) // Добавляем загруженные песни
        adapter.notifyDataSetChanged() // Обновляем RecyclerView
    }

    private lateinit var adapter: OnSongClickListener.SongListAdapter

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

        this.adapter = OnSongClickListener.SongListAdapter(this.requireContext(), this.requireActivity() as OnSongClickListener)
        val recyclerView = view.findViewById<RecyclerView>(R.id.music_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadSongs() // загрузка списка песен

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
                // Запрос постоянного разрешения
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                adapter.addSongFromUri(uri)
            } ?: run {
                data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        adapter.addSongFromUri(uri)
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_AUDIO = 1
    }
}

class PlayFragment : Fragment() { // фрагмент проигрывателя
    private lateinit var seekBar: SeekBar // Объявляем переменную для SeekBar
    lateinit var playButton : View // кнопка play

    private val currentSongObserver = Observer<Uri> { newSongUri ->
        // обновление плеера
        if (player != null) {
            if (player!!.isPlaying) {
                player!!.stop()
                player!!.reset()
            }
            player!!.release()
            player = null
            //player = MediaPlayer.create(requireContext(), currentSong)
            player!!.start()

            // обновление ui

        } else {
            Toast.makeText(requireContext(), "Выберите трек", Toast.LENGTH_SHORT).show()
        }
    }


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
        playButton = view.findViewById(R.id.button_play)
        seekBar = view.findViewById(R.id.seekBar)

        // механика кнопки play (надо будет сделать переключение трека при изменении currentSong)
        playButton.setOnClickListener {
            if (player == null) {
                Toast.makeText(requireContext(), "Выберете трек", Toast.LENGTH_SHORT).show()
            } else {
                // Проверяем, есть ли разрешение на доступ к Uri
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Запрашиваем разрешение
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
                    return@setOnClickListener // Выходим из слушателя, пока не получим разрешение
                }
                if (player!!.isPlaying) {
                    player!!.pause()
                    playButton.setBackgroundResource(R.drawable.baseline_play_circle_24)
                } else {
                    player!!.start()
                    playButton.setBackgroundResource(R.drawable.baseline_pause_circle_24)
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
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, можно воспроизводить музыку
                playButton.performClick() // Вызываем клик по кнопке Play, чтобы запустить воспроизведение
            } else {
                // Разрешение не получено, сообщаем пользователю
                Toast.makeText(requireContext(), "Для воспроизведения музыки необходимо разрешение на доступ к хранилищу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE = 123 // Выберите любое уникальное значение
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