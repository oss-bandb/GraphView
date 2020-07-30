package de.blox.graphview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class GraphAdapter2<VH : GraphAdapter2.ViewHolder>(
        var graph: Graph) : RecyclerView.Adapter<VH>() {
    override fun getItemCount(): Int = graph.nodeCount

    fun getNode(position: Int): Node = graph.getNodeAtPosition(position)

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, graph.getNodeAtPosition(position).data)
    }

    abstract fun onBindViewHolder(holder: VH, data: Any)

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}