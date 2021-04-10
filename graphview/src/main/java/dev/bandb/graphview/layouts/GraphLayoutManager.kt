package dev.bandb.graphview.layouts

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Graph
import dev.bandb.graphview.util.Size
import kotlin.math.max

abstract class GraphLayoutManager internal constructor(context: Context)
    : RecyclerView.LayoutManager() {

    var useMaxSize: Boolean = DEFAULT_USE_MAX_SIZE
        set(value) {
            field = value
            requestLayout()
        }

    private var adapter: AbstractGraphAdapter<*>? = null

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
            RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            )

    override fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?,
                                  newAdapter: RecyclerView.Adapter<*>?) {
        super.onAdapterChanged(oldAdapter, newAdapter)

        if (newAdapter !is AbstractGraphAdapter) {
            throw RuntimeException(
                    "GraphLayoutManager only works with ${AbstractGraphAdapter::class.simpleName}")
        }

        adapter = newAdapter
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        positionItems(recycler, state.itemCount)
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State,
                           widthSpec: Int, heightSpec: Int) {
        val adapter = adapter
        if (adapter == null) {
            Log.e("GraphLayoutManager", "No adapter attached; skipping layout")
            super.onMeasure(recycler, state, widthSpec, heightSpec)
            return
        }

        val graph = adapter.graph
        if (graph == null || !graph.hasNodes()) {
            Log.e("GraphLayoutManager", "No graph set; skipping layout")
            super.onMeasure(recycler, state, widthSpec, heightSpec)
            return
        }

        var maxWidth = 0
        var maxHeight = 0

        for (i in 0 until state.itemCount) {
            val child = recycler.getViewForPosition(i)

            var params: ViewGroup.MarginLayoutParams? = child.layoutParams as? ViewGroup.MarginLayoutParams
            if (params == null) {
                params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            addView(child)

            val childWidthSpec = makeMeasureSpec(params.width)
            val childHeightSpec = makeMeasureSpec(params.height)
            child.measure(childWidthSpec, childHeightSpec)

            val measuredWidth = child.measuredWidth
            val measuredHeight = child.measuredHeight

            val node = adapter.getNode(i)
            node?.size?.apply {
                width = measuredWidth
                height = measuredHeight
            }

            maxWidth = max(maxWidth, measuredWidth)
            maxHeight = max(maxHeight, measuredHeight)
        }

        if (useMaxSize) {
            detachAndScrapAttachedViews(recycler)
            for (i in 0 until state.itemCount) {
                val child = recycler.getViewForPosition(i)

                addView(child)

                val childWidthSpec = makeMeasureSpec(maxWidth)
                val childHeightSpec = makeMeasureSpec(maxHeight)
                child.measure(childWidthSpec, childHeightSpec)

                val node = adapter.getNode(i)
                node?.size?.apply {
                    width = child.measuredWidth
                    height = child.measuredHeight
                }
            }
        }

        val size = run(graph, paddingLeft.toFloat(), paddingTop.toFloat())
        setMeasuredDimension(size.width + paddingRight + paddingLeft, size.height + paddingBottom + paddingTop)
    }

    private fun positionItems(recycler: RecyclerView.Recycler,
                              itemCount: Int) {
        for (index in 0 until itemCount) {
            val child = recycler.getViewForPosition(index)

            adapter?.getNode(index)?.let {
                val width = it.width
                val height = it.height
                val (x, y) = it.position

                addView(child)
                val childWidthSpec = makeMeasureSpec(it.width)
                val childHeightSpec = makeMeasureSpec(it.height)
                child.measure(childWidthSpec, childHeightSpec)

                // calculate the size and position of this child
                val left = x.toInt()
                val top = y.toInt()
                val right = left + width
                val bottom = top + height

                child.layout(left, top, right, bottom)
            }
        }
    }

    /**
     * Executes the algorithm.
     * @param shiftY Shifts the y-coordinate origin
     * @param shiftX Shifts the x-coordinate origin
     * @return The size of the graph
     */
    abstract fun run(graph: Graph, shiftX: Float, shiftY: Float): Size

    companion object {
        const val DEFAULT_USE_MAX_SIZE = false

        private fun makeMeasureSpec(dimension: Int): Int {
            return if (dimension > 0) {
                View.MeasureSpec.makeMeasureSpec(dimension, View.MeasureSpec.EXACTLY)
            } else {
                View.MeasureSpec.UNSPECIFIED
            }
        }
    }

}