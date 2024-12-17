package com.softwareforpeople.lm

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

data class song_list_item(val name: String, val author: String, val uri: String) {}

interface OnSongClickListener {
    fun onSongClick(songUri: Uri)

    class SongListAdapter(private val context: Context, private val listener: OnSongClickListener) :
        RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

        internal val songs: MutableList<song_list_item> = mutableListOf()

        class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val songName: TextView = itemView.findViewById(R.id.song_name)
            val songAuthor: TextView = itemView.findViewById(R.id.song_author)
            val menuButton: ImageButton = itemView.findViewById(R.id.more_actions)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            return SongViewHolder(view)
        }

        override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
            val currentSong = songs[position]
            holder.songName.text = currentSong.name
            holder.songAuthor.text = currentSong.author

            holder.itemView.setOnClickListener { // нажатие на элемент списка
                val currentSongUri = Uri.parse(songs[holder.adapterPosition].uri)
                val songUriString = currentSongUri.toString()
                Log.d("SongListAdapter", "Song URI string: $songUriString")
                listener.onSongClick(currentSongUri)
            }

            holder.menuButton.setOnClickListener { // выпадающее меню
                val popupMenu = PopupMenu(holder.itemView.context, holder.menuButton)
                popupMenu.inflate(R.menu.list_more_actions)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_delete -> { // удаление трека
                            removeSong(position)
                            true
                        }
                        R.id.action_test -> {
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.show() // отображение меню
            }
        }

        fun addSongFromUri(uri: Uri) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val name = if (nameIndex != -1) it.getString(nameIndex) else "Музыка"
                    val author = if (artistIndex != -1) it.getString(artistIndex) else "нн"

                    // Проверка на наличие трека в списке
                    val existingSong = songs.find { it.name == name && it.author == author }
                    if (existingSong == null) {
                        addSong(song_list_item(name, author, uri.toString()))
                    } else {
                        Toast.makeText(
                            context,
                            "Данный трек уже есть в списке",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                it.close()
            }
        }

        override fun getItemCount(): Int {
            return songs.size
        }

        fun addSong(song: song_list_item) {
            songs.add(song)
            notifyItemInserted(songs.size - 1)
            applyChanges()
        }

        fun removeSong(position: Int) {
            songs.removeAt(position)
            notifyItemRemoved(position)
            applyChanges()
        }

        private fun applyChanges() {
            val sharedPreferences = context.getSharedPreferences(
                "songs",
                Context.MODE_PRIVATE
            ) // Используем context адаптера
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val json = gson.toJson(songs)
            editor.putString("songs_list", json)
            editor.apply()
        }
    }
}