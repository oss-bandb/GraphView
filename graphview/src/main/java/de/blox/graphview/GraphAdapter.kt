package de.blox.graphview

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.annotation.NonNull

abstract class GraphAdapter<VH : GraphView.ViewHolder>(var graph: Graph) : Adapter {

    private val dataSetObservable = DataSetObservable()
    private var graphViewObserver: DataSetObserver? = null

    fun notifyDataSetChanged() {
        synchronized(this) {
            graphViewObserver?.onChanged()
        }
        dataSetObservable.notifyChanged()
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.unregisterObserver(observer)
    }

    open fun setGraphViewObserver(observer: DataSetObserver?) {
        synchronized(this) { graphViewObserver = observer }
    }

    @NonNull
    abstract fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): VH

    abstract fun onBindViewHolder(@NonNull viewHolder: VH, data: Any, position: Int)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: VH

        if (convertView == null) {
            viewHolder = onCreateViewHolder(parent, getItemViewType(position))
            view = viewHolder.itemView
            view.tag = viewHolder
        } else {
            viewHolder = convertView.tag as VH
            view = viewHolder.itemView
        }

        onBindViewHolder(viewHolder, getItem(position), position)

        return view
    }

    override fun getItemId(position: Int): Long = NO_ID

    override fun hasStableIds(): Boolean = false

    override fun getItemViewType(position: Int): Int = 0

    override fun getViewTypeCount(): Int = 0

    companion object {
        const val NO_ID: Long = -1
    }
}
