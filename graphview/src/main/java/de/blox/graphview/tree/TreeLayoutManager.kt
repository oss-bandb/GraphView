package de.blox.graphview.tree

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.blox.graphview.GraphAdapter2
import kotlin.math.max
import kotlin.math.min

class TreeLayoutManager : RecyclerView.LayoutManager() {
    private lateinit var adapter: GraphAdapter2<*>
    private val layout = BuchheimWalkerAlgorithm()
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
            RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            )

    override fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?,
                                  newAdapter: RecyclerView.Adapter<*>?) {
        super.onAdapterChanged(oldAdapter, newAdapter)

        if (newAdapter !is GraphAdapter2) {
            throw RuntimeException(
                    "TreeLayoutManager only works with ${GraphAdapter2::class.simpleName}")
        }

        adapter = newAdapter
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        positionItems(recycler, state.itemCount)
//        invalidate()
    }

    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State,
                           widthSpec: Int, heightSpec: Int) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        val adapter = this.adapter

        var maxWidth = 0
        var maxHeight = 0
        var minHeight = Integer.MAX_VALUE

        for (i in 0 until state.itemCount) {
            val child = recycler.getViewForPosition(i)

            var params: ViewGroup.LayoutParams? = child.layoutParams
            if (params == null) {
                params = ViewGroup.LayoutParams(
                        RecyclerView.LayoutParams.WRAP_CONTENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                )
            }
            addView(child)

            val childWidthSpec = makeMeasureSpec(params.width)
            val childHeightSpec = makeMeasureSpec(params.height)

            child.measure(childWidthSpec, childHeightSpec)
            val node = adapter.getNode(i)
            val measuredWidth = child.measuredWidth
            val measuredHeight = child.measuredHeight
            node.size.apply {
                width = measuredWidth
                height = measuredHeight
            }

            maxWidth = max(maxWidth, measuredWidth)
            maxHeight = max(maxHeight, measuredHeight)
            minHeight = min(minHeight, measuredHeight)
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
        adapter.notifyDataSetChanged()
        layout.run {
            val size = run(adapter.graph, paddingLeft.toFloat(), paddingTop.toFloat())
            setMeasuredDimension(size.width + paddingRight + paddingLeft, size.height + paddingBottom + paddingTop)
        }
    }

    private fun positionItems(recycler: RecyclerView.Recycler,
                              itemCount: Int) {
        for (index in 0 until itemCount) {
            val child = recycler.getViewForPosition(index)
            addView(child)
            measureChildWithMargins(child, 0, 0)

            val width = child.measuredWidth
            val height = child.measuredHeight
            val node = adapter.getNode(index)
            val (x, y) = node.position

            // calculate the size and position of this child
            val left = x.toInt()
            val top = y.toInt()
            val right = left + width
            val bottom = top + height

            child.layout(left, top, right, bottom)
        }
    }

    companion object {
//        const val DEFAULT_USE_MAX_SIZE = false
//        const val DEFAULT_LINE_THICKNESS = 5
//        const val DEFAULT_LINE_COLOR = Color.BLACK
//        const val INVALID_INDEX = -1

        private fun makeMeasureSpec(dimension: Int): Int {
            return if (dimension > 0) {
                View.MeasureSpec.makeMeasureSpec(dimension, View.MeasureSpec.EXACTLY)
            } else {
                View.MeasureSpec.UNSPECIFIED
            }
        }
    }

}