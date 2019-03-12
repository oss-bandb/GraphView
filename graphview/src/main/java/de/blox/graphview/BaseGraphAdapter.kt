package de.blox.graphview

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import de.blox.graphview.tree.BuchheimWalkerAlgorithm
import de.blox.graphview.util.VectorF

abstract class BaseGraphAdapter<VH : ViewHolder> @JvmOverloads constructor(graph: Graph = Graph()) :
    GraphAdapter<VH> {
    override var algorithm: Algorithm = BuchheimWalkerAlgorithm()
    override var graph: Graph = graph
        set(value) {
            graph.removeGraphObserver(this)
            field = value
            field.let {
                it.addGraphObserver(this)
                it.setAsTree(algorithm is BuchheimWalkerAlgorithm)
            }

            notifyGraphInvalidated()
        }

    private val dataSetObservable = DataSetObservable()

    override fun notifySizeChanged() {
        if (graph.hasNodes()) {
            algorithm.run(graph)
        }
    }

    override fun getNode(position: Int): Node = graph.getNodeAtPosition(position)

    override fun getScreenPosition(position: Int): VectorF = getNode(position).position

    override fun getCount(): Int = graph.nodeCount

    override fun registerDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        dataSetObservable.unregisterObserver(observer)
    }

    override fun getItem(position: Int): Any = getNode(position).data

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

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

    override fun getItemViewType(position: Int): Int = 0

    override fun getViewTypeCount(): Int = 0

    override fun isEmpty(): Boolean = graph.hasNodes()

    override fun notifyGraphInvalidated() {
        dataSetObservable.notifyInvalidated()
    }
}
