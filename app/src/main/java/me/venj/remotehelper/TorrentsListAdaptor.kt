package me.venj.remotehelper

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class TorrentsListAdaptor(val itemsList: List<TorrentsListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.torrents_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as TorrentsListAdaptor.ViewHolder).bindItem(itemsList[position])
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(item: TorrentsListItem) {
            val titleTextView = itemView.findViewById<TextView>(R.id.torrents_item_title)
            val selectButton = itemView.findViewById<Button>(R.id.index_selection_button)
            titleTextView.text = "${item.title} (${item.count})"
            selectButton.text = "ℹ️"
        }
    }
}