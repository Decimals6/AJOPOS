package com.ajoclub.ajopos.view.adapter.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ajoclub.ajopos.R
import com.ajoclub.ajopos.util.TutorialGuide


class TutorialAdapter(
    private val items: List<TutorialGuide>
) : RecyclerView.Adapter<TutorialAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutHeader: LinearLayout = view.findViewById(R.id.layoutHeader)
        val tvTitle: TextView = view.findViewById(R.id.tvGuideTitle)
        val ivArrow: ImageView = view.findViewById(R.id.ivArrow)
        val layoutContent: LinearLayout = view.findViewById(R.id.layoutContent)
        val tvSteps: TextView = view.findViewById(R.id.tvGuideSteps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutorial_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val guide = items[position]

        holder.tvTitle.text = guide.title

        // Format step nomor 1, 2, 3...
        val formattedSteps = guide.steps.mapIndexed { index, step ->
            "${index + 1}. $step"
        }.joinToString("\n\n")

        holder.tvSteps.text = formattedSteps

        // Status buka-tutup
        holder.layoutContent.visibility = if (guide.isExpanded) View.VISIBLE else View.GONE
        holder.ivArrow.rotation = if (guide.isExpanded) 180f else 0f

        holder.layoutHeader.setOnClickListener {
            guide.isExpanded = !guide.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = items.size
}