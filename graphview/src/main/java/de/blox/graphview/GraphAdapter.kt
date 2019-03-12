package de.blox.graphview

import android.view.ViewGroup
import android.widget.Adapter
import de.blox.graphview.util.VectorF

interface GraphAdapter<VH : ViewHolder> : Adapter, GraphObserver {
    var algorithm: Algorithm
    var graph: Graph

    fun notifySizeChanged()

    fun getNode(position: Int): Node

    fun getScreenPosition(position: Int): VectorF

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    fun onBindViewHolder(viewHolder: VH, data: Any, position: Int)
}
