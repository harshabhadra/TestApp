package com.harshabhadra.testapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.harshabhadra.testapp.database.DbEntry
import com.harshabhadra.testapp.databinding.EntryListItemBinding

class EntryListAdapter :
    PagingDataAdapter<DbEntry, EntryListAdapter.EntryListViewHolder>(EntryListDiffUtilCallBack()) {

    class EntryListViewHolder(private val binding: EntryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: DbEntry) {
            binding.entry = entry
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): EntryListViewHolder {
                val binding =
                    EntryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return EntryListViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryListViewHolder {
        return EntryListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: EntryListViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
        }
    }
}

class EntryListDiffUtilCallBack : DiffUtil.ItemCallback<DbEntry>() {
    override fun areItemsTheSame(oldItem: DbEntry, newItem: DbEntry): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: DbEntry, newItem: DbEntry): Boolean {
        return oldItem.api == newItem.api
    }

}