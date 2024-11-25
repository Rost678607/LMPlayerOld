package com.softwareforpeople.lm

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class SongListAdapter(private val context: Context) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    internal val songs: MutableList<song_list_item> = mutableListOf()

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.song_name)
        val songAuthor: TextView = itemView.findViewById(R.id.song_author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentSong = songs[position]
        holder.songName.text = currentSong.name
        holder.songAuthor.text = currentSong.author

        holder.itemView.setOnClickListener {
            val uri = Uri.parse(currentSong.uri)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun addSong(song: song_list_item) {
        songs.add(song)
        notifyItemInserted(songs.size - 1)
        saveSong()
    }

    fun removeSong(position: Int) {
        songs.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun saveSong() {
        val sharedPreferences = context.getSharedPreferences("songs", Context.MODE_PRIVATE) // Используем context адаптера
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(songs)
        editor.putString("songs_list", json)
        editor.apply()
    }
}