package com.softwareforpeople.lm

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class SongListAdapter(private val context: Context) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    internal val songs: MutableList<song_list_item> = mutableListOf()

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.song_name)
        val songAuthor: TextView = itemView.findViewById(R.id.song_author)
    }

    // создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return SongViewHolder(view)
    }
    // привязка данных к ViewHolder
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentSong = songs[position]
        holder.songName.text = currentSong.name
        holder.songAuthor.text = currentSong.author
        // ... другие действия с данными песни ...
    }

    // количество песен в списке
    override fun getItemCount(): Int {
        return songs.size
    }

    // добавление песни в список
    fun addSong(song: song_list_item) {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.TITLE, song.name)
            put(MediaStore.Audio.Media.ARTIST, song.author)
            // ... другие метаданные песни ...
        }
        val contentUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

        if (contentUri != null) {
            songs.add(song.copy(contentUri = contentUri))
            notifyItemInserted(songs.size - 1)
        } else {
            // Обработка ошибки добавления
            Toast.makeText(context, "Иди нахуй даун!!!1!111! !11", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeSong(position: Int) {
        val song = songs[position]
        context.contentResolver.delete(song.contentUri, null, null)
        songs.removeAt(position)
        notifyItemRemoved(position)
    }
}