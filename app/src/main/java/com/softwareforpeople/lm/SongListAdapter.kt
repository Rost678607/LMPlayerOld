package com.softwareforpeople.lm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongListAdapter : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    private val songs: MutableList<song_list_item> = mutableListOf()

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.song_name)
        val songAuthor: TextView = itemView.findViewById(R.id.song_author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return SongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentSong = songs[position]
        holder.songName.text = currentSong.name
        holder.songAuthor.text = currentSong.author
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun addSong(song: song_list_item) {
        songs.add(song)
        notifyItemInserted(songs.size - 1)
    }

    fun removeSong(position: Int) {
        songs.removeAt(position)
        notifyItemRemoved(position)
    }
}