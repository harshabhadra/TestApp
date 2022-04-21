package com.harshabhadra.testapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.harshabhadra.testapp.databinding.EntryListItemBinding
import com.harshabhadra.testapp.network.Entry

class EntryListAdapter :
    ListAdapter<Entry, EntryListAdapter.EntryListViewHolder>(EntryListDiffUtilCallBack()) {

    class EntryListViewHolder(private val binding: EntryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: Entry) {
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

class EntryListDiffUtilCallBack : DiffUtil.ItemCallback<Entry>() {
    override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return oldItem.api == newItem.api
    }

}