package com.advancesewa.callsync.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.advancesewa.callsync.R

data class CallItem(
    val primary: String,
    val secondary: String,
    val type: Int,
    val hasRecording: Boolean
)

class CallLogAdapter : RecyclerView.Adapter<CallLogAdapter.VH>() {

    private val items = mutableListOf<CallItem>()

    fun submit(newItems: List<CallItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_call_log, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.icon)
        private val primary: TextView = itemView.findViewById(R.id.primary)
        private val secondary: TextView = itemView.findViewById(R.id.secondary)
        private val recBadge: TextView = itemView.findViewById(R.id.recording_badge)

        fun bind(item: CallItem) {
            primary.text = item.primary
            secondary.text = item.secondary
            recBadge.visibility = if (item.hasRecording) View.VISIBLE else View.GONE
            val glyph = when (item.type) {
                1 -> "📥" // Incoming
                2 -> "📤" // Outgoing
                3 -> "❌" // Missed
                5 -> "🚫" // Rejected
                else -> "📞"
            }
            icon.text = glyph
        }
    }
}
