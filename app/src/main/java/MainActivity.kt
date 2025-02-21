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
import android.widget.Switch
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Playlist(
    val name: String,
    val tracks: List<Track>
)

data class Track(
    val title: String,
    val artist: String,
    val uri: String
)

fun addTrackToPlaylist(playlist: Playlist, track: Track): Playlist{
    return playlist.copy(tracks = playlist.tracks + track)
}

fun removeTrackFromPlaylist(playlist: Playlist, track: Track): Playlist{
    return playlist.copy(tracks = playlist.tracks - track)
}

@Entity
data class PlaylistEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val trackIds: List<Int>
)

val myPlaylist = Playlist("Мой плейлист", listOf())
val newTrack = Track("Песня", "Исполнитель", "uri_песни")
val updatedPlaylist = addTrackToPlaylist(myPlaylist, newTrack)

var currentSong: Uri = Uri.EMPTY // текущий трек
private lateinit var currentFragment: Fragment // текущий фрагмент
var player: MediaPlayer? = null

// отладка
var debugToast: Boolean = false
var debugLog: Boolean = false

fun debugForEveryFart (context: Context, text: String, toast: Boolean) {
    if (debugToast and toast) { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }
    if (debugLog) { Log.d("Debug", text) }
}

class MainActivity : AppCompatActivity(), OnSongClickListener {
    lateinit var playFragment: PlayFragment
    lateinit var listFragment: ListFragment
    lateinit var settingsFragment: SettingsFragment

    lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        debugForEveryFart(this, "создано main activity", false)

        // кнопки
        navigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // инициализация фрагментов и выбор фрагмента по умолчанию
        debugForEveryFart(this, "инициализация фрагментов...", true)
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

        debugForEveryFart(this, "фрагменты инициализированы", false)

        // запрос разрешения на чтение аудиофайлов с телефона
        debugForEveryFart(this, "проверка разрешения на чтение аудиофайлов", true)
        // Android 13 и выше
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1001)
            // Android 12 и ниже
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001)
        }

        debugForEveryFart(this, "разрешения проверены", false)

        // обработка нажатий кнопок меню (переключение между фрагментами)
        debugForEveryFart(this, "инициализация слушателя нажатий навигации...", true)
        navigation.setOnItemSelectedListener { item ->
            Log.d("Navigation", "Item selected: ${item.itemId}")
            when (item.itemId) {
                R.id.menu_list -> {
                    debugForEveryFart(this, "переключение на фрагмент списка", true)
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .show(listFragment)
                        .commit()
                    currentFragment = listFragment
                    true
                }
                R.id.menu_play -> {
                    debugForEveryFart(this, "переключение на фрагмент проигрывателя", true)
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .show(playFragment)
                        .commit()
                    currentFragment = playFragment
                    true
                }
                R.id.menu_settings -> {
                    debugForEveryFart(this, "переключение на фрагмент настроек", true)
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
        debugForEveryFart(this, "инициализация слушателя нажатий панели навигации завершена", false)
    }

    override fun onSongClick(songUri: Uri) {
        debugForEveryFart(this, "выбран какой-то трек", true)
        if (songUri != currentSong) {
            debugForEveryFart(this, "до этого был выбран другой трек", false)
            currentSong = songUri
            player?.release()
            player = MediaPlayer.create(this, currentSong)
            player!!.start()
            playFragment.playButton.setBackgroundResource(R.drawable.baseline_pause_circle_24)
        } else if (!player!!.isPlaying) {
            player!!.start()
            playFragment.playButton.setBackgroundResource(R.drawable.baseline_pause_circle_24)
        }
        debugForEveryFart(this, "запущено воспроизведение", false)
        navigation.setSelectedItemId(R.id.menu_play)
        currentFragment = playFragment
    }
}

class ListFragment : Fragment() { // фрагмент списка терков

    // Добавление трека
    //val updatedPlaylist = addTrackToPlaylist(myPlaylist, newTrack)

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSongs() {
        debugForEveryFart(requireContext(), "подгрузка треков...", true)
        val sharedPreferences = requireActivity().getSharedPreferences("songs", Context.MODE_PRIVATE) // или this.getSharedPreferences(...) в активити
        val gson = Gson()
        val json = sharedPreferences.getString("songs_list", null)
        val type = object : TypeToken<List<song_list_item>>() {}.type
        val songs = gson.fromJson<List<song_list_item>>(json, type) ?: emptyList()
        adapter.songs.clear() // Очищаем текущий список песен в адаптере
        adapter.songs.addAll(songs) // Добавляем загруженные песни
        adapter.notifyDataSetChanged() // Обновляем RecyclerView
        debugForEveryFart(requireContext(), "треки загружены", false)
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
            debugForEveryFart(requireContext(), "нажата кнопка добавления трека", true)
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
        debugForEveryFart(requireContext(), "инициализирован фрагмент списка", true)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        debugForEveryFart(requireContext(), "получение результата выбора трека...", true)
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
        debugForEveryFart(requireContext(), "результат выбора трека получен", false)
    }

    companion object {
        private const val REQUEST_CODE_PICK_AUDIO = 52
    }
}

class PlayFragment : Fragment() { // фрагмент проигрывателя
    private lateinit var seekBar: SeekBar // Объявляем переменную для SeekBar
    lateinit var playButton : View // кнопка play


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
            debugForEveryFart(requireContext(), "нажата кнопка play", true)
            if (player == null) {
                debugForEveryFart(requireContext(), "трека нет", false)
                Toast.makeText(requireContext(), "Выберите трек", Toast.LENGTH_SHORT).show()
            } else if (player!!.isPlaying) {
                debugForEveryFart(requireContext(), "трек играет", false)
                player!!.pause()
                playButton.setBackgroundResource(R.drawable.baseline_play_circle_24)
            } else {
                debugForEveryFart(requireContext(), "трек не играет", false)
                player!!.start()
                playButton.setBackgroundResource(R.drawable.baseline_pause_circle_24)
            }
            debugForEveryFart(requireContext(), "нажатие кнопки play успешно обработано", false)
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
        debugForEveryFart(requireContext(), "инициализирован фрагмент проигрывателя", true)
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
                Toast.makeText(
                    requireContext(),
                    "Для воспроизведения музыки необходимо разрешение на доступ к хранилищу",
                    Toast.LENGTH_SHORT
                ).show()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toastSwitch: Switch = view.findViewById(R.id.debugToastSwitch)
        val logSwitch: Switch = view.findViewById(R.id.debugLogSwitch)

        // Устанавливаем начальные значения переключателей
        toastSwitch.isChecked = debugToast
        logSwitch.isChecked = debugLog

        // Слушатели для переключателей
        toastSwitch.setOnCheckedChangeListener { _, isChecked ->
            debugToast = isChecked
        }

        logSwitch.setOnCheckedChangeListener { _, isChecked ->
            debugLog = isChecked
        }
        debugForEveryFart(requireContext(), "инициализирован фрагмент настроек", true)
    }
}*/