package dev.bandb.graphview

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.util.Size
import kotlin.math.max
import kotlin.math.min

abstract class GraphLayoutManager internal constructor(private val context: Context) : RecyclerView.LayoutManager() {
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
        if (adapter == null) {
            Log.e("GraphLayoutManager", "No adapter attached; skipping layout")
            super.onMeasure(recycler, state, widthSpec, heightSpec)
            return
        }

        adapter?.let {
            val graph = adapter?.graph
            if (graph == null || !graph.hasNodes()) {
                super.onMeasure(recycler, state, widthSpec, heightSpec)
                return
            }

            var maxWidth = 0
            var maxHeight = 0
            var minHeight = Integer.MAX_VALUE

            for (i in 0 until state.itemCount) {
                val child = recycler.getViewForPosition(i)

                //var params: ViewGroup.LayoutParams? = child.layoutParams
                var params: ViewGroup.MarginLayoutParams? = child.layoutParams as? ViewGroup.MarginLayoutParams
                if (params == null) {
                    params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }

                addView(child)

                val childWidthSpec = makeMeasureSpec(params.width)
                val childHeightSpec = makeMeasureSpec(params.height)

                child.measure(childWidthSpec, childHeightSpec)
                val node = it.getNode(i)
                val measuredWidth = child.measuredWidth
                val measuredHeight = child.measuredHeight
                node?.size?.apply {
                    width = measuredWidth
                    height = measuredHeight
                }

                maxWidth = max(maxWidth, measuredWidth)
                maxHeight = max(maxHeight, measuredHeight)
                minHeight = min(minHeight, measuredHeight)
            }

            it.notifyDataSetChanged()

            val size = run(graph, paddingLeft.toFloat(), paddingTop.toFloat())
            setMeasuredDimension(size.width + paddingRight + paddingLeft, size.height + paddingBottom + paddingTop)
        }

//        maxChildWidth = maxWidth
//        maxChildHeight = maxHeight

//        if (isUsingMaxSize) {
//            removeAllViewsInLayout()
//            for (i in 0 until adapter.count) {
//                val child = adapter.getView(i, null, this)
//
//                var params: ViewGroup.LayoutParams? = child.layoutParams
//                if (params == null) {
//                    params = ViewGroup.LayoutParams(
//                            RecyclerView.LayoutParams.WRAP_CONTENT,
//                            RecyclerView.LayoutParams.WRAP_CONTENT
//                    )
//                }
//                addViewInLayout(child, -1, params, true)
//
//                val widthSpec =
//                        View.MeasureSpec.makeMeasureSpec(maxChildWidth, View.MeasureSpec.EXACTLY)
//                val heightSpec =
//                        View.MeasureSpec.makeMeasureSpec(maxChildHeight, View.MeasureSpec.EXACTLY)
//                child.measure(widthSpec, heightSpec)
//
//                val node = adapter.getNode(i) as Node
//                node.size.apply {
//                    width = child.measuredWidth
//                    height = child.measuredHeight
//                }
//            }
//        }

    }

    private fun positionItems(recycler: RecyclerView.Recycler,
                              itemCount: Int) {
        for (index in 0 until itemCount) {
            val child = recycler.getViewForPosition(index)
            addView(child)
            measureChildWithMargins(child, 0, 0)

            adapter?.getNode(index)?.let {
                val width = child.measuredWidth
                val height = child.measuredHeight
                val (x, y) = it.position

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