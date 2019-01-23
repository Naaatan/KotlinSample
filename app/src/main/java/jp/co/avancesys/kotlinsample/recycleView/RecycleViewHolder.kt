package jp.co.avancesys.kotlinsample.recycleView

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import jp.co.avancesys.kotlinsample.R

class RecycleViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int, name: String)
    }

    val itemTextView: TextView = view.findViewById(R.id.textViewItem)
    val itemImageView: ImageView = view.findViewById(R.id.imageViewItem)

    init {

    }
}