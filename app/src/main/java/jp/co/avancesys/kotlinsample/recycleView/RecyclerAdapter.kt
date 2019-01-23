package jp.co.avancesys.kotlinsample.recycleView

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.co.avancesys.kotlinsample.R

@Suppress("UNREACHABLE_CODE")
class RecyclerAdapter(
    private val context: Context,
    private val itemClickListener: RecycleViewHolder.ItemClickListener,
    private val itemList: List<String>
) : RecyclerView.Adapter<RecycleViewHolder>() {

    private var mRecyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecycleViewHolder {

        val layoutInflater = LayoutInflater.from(context)
        val listItem = layoutInflater.inflate(R.layout.list_item_card, parent, false)

        listItem.setOnClickListener { view ->
            mRecyclerView?.let {
                val position = it.getChildAdapterPosition(view)
                itemClickListener.onItemClick(view, position, itemList[position])
            }
        }

        return RecycleViewHolder(listItem)
    }

    override fun getItemCount(): Int {

        return itemList.size
    }

    override fun onBindViewHolder(holder: RecycleViewHolder, position: Int) {
        holder.itemTextView.text = itemList.get(position)
        holder.itemImageView.setImageResource(R.mipmap.ic_launcher)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

}