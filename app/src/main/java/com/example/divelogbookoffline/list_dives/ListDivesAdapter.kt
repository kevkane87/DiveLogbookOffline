package com.example.divelogbookoffline.list_dives

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divelogbookoffline.Dive
import com.example.divelogbookoffline.databinding.ItemDiveBinding

class ListDivesAdapter: ListAdapter<Dive, ListDivesAdapter.ViewHolder>(
ARTICLE_DIFF_CALLBACK
) {

    //viewholder for bet items
    class ViewHolder (private val binding: ItemDiveBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Dive) {
            binding.itemDiveTitle.text = item.diveTitle
            binding.itemDate.text = item.date
            binding.itemDiveSite.text = item.diveSite.toString()
            binding.itemBottomTime.text = item.bottomTime.toString()
            binding.itemMaxDepth.text = item.maxDepth.toString()
        }
    }

    //called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDiveBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false))
    }

    //called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dive = getItem(position)
        holder.bind(dive)
    }

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Dive>() {
            override fun areItemsTheSame(oldItem: Dive, newItem: Dive): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Dive, newItem: Dive): Boolean =
                oldItem == newItem
        }
    }
}

