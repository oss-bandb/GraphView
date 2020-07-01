package de.blox.graphview

import android.content.Context
import android.database.DataSetObserver
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import kotlin.math.max
import kotlin.math.min


class GraphView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AdapterView<GraphAdapter<GraphView.ViewHolder>>(context, attrs, defStyleAttr) {
    private var linePaint: Paint

    init {
        linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = lineThickness.toFloat()
            color = lineColor
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND    // set the join to round you want
            pathEffect = CornerPathEffect(10f)   // set the path effect when they join.
        }

        attrs?.let { initAttrs(context, it) }
    }

    var lineThickness: Int = DEFAULT_LINE_THICKNESS
        set(@Px value) {
            linePaint.strokeWidth = value.toFloat()
            field = value
            invalidate()
        }

    var lineColor: Int = DEFAULT_LINE_COLOR
        @ColorInt get
        set(@ColorInt value) {
            linePaint.color = value
            field = value
            invalidate()
        }

    var isUsingMaxSize: Boolean = DEFAULT_USE_MAX_SIZE
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    private var adapter: GraphAdapter<ViewHolder>? = null

    private var layout: Layout? = null

    private var maxChildWidth: Int = 0
    private var maxChildHeight: Int = 0
    private val rect: Rect by lazy {
        Rect()
    }

    private var observer: DataSetObserver? = null
    private val gestureDetector: GestureDetector = GestureDetector(getContext(), GestureListener())

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.GraphView, 0, 0)

        lineThickness = a.getDimensionPixelSize(R.styleable.GraphView_lineThickness, DEFAULT_LINE_THICKNESS)
        lineColor = a.getColor(R.styleable.GraphView_lineColor, DEFAULT_LINE_COLOR)
        isUsingMaxSize = a.getBoolean(R.styleable.GraphView_useMaxSize, DEFAULT_USE_MAX_SIZE)

        a.recycle()
    }

    private fun positionItems() {
        var maxLeft = Integer.MAX_VALUE
        var maxRight = Integer.MIN_VALUE
        var maxTop = Integer.MAX_VALUE
        var maxBottom = Integer.MIN_VALUE

        for (index in 0 until adapter!!.count) {
            val child = adapter!!.getView(index, null, this)
            addAndMeasureChild(child)

            val width = child.measuredWidth
            val height = child.measuredHeight
            val node = adapter!!.getItem(index) as Node

            val (x, y) = node.position

            // calculate the size and position of this child
            val left = x.toInt()
            val top = y.toInt()
            val right = left + width
            val bottom = top + height

            child.layout(left, top, right, bottom)

            maxRight = max(maxRight, right)
            maxLeft = min(maxLeft, left)
            maxBottom = max(maxBottom, bottom)
            maxTop = min(maxTop, top)
        }
    }

    private fun addAndMeasureChild(child: View) {
        var params: LayoutParams? = child.layoutParams
        if (params == null) {
            params = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            )
        }

        addViewInLayout(child, -1, params, false)
        var widthSpec = makeMeasureSpec(params.width)
        var heightSpec = makeMeasureSpec(params.height)

        if (isUsingMaxSize) {
            widthSpec = MeasureSpec.makeMeasureSpec(
                    maxChildWidth, MeasureSpec.EXACTLY
            )
            heightSpec = MeasureSpec.makeMeasureSpec(
                    maxChildHeight, MeasureSpec.EXACTLY
            )
        }

        child.measure(widthSpec, heightSpec)
    }


    private fun getContainingChildIndex(x: Int, y: Int): Int {
        for (index in 0 until childCount) {
            getChildAt(index).getHitRect(rect)
            if (rect.contains(x, y)) {
                return index
            }
        }
        return INVALID_INDEX
    }

    private fun clickChildAt(x: Int, y: Int) {
        val index = getContainingChildIndex(x, y)
        // no child found at this position
        if (index == INVALID_INDEX) {
            return
        }

        val itemView = getChildAt(index)
        val id = adapter!!.getItemId(index)
        performItemClick(itemView, index, id)
    }

    private fun longClickChildAt(x: Int, y: Int) {
        val index = getContainingChildIndex(x, y)
        // no child found at this position
        if (index == INVALID_INDEX) {
            return
        }

        val itemView = getChildAt(index)
        val id = adapter!!.getItemId(index)
        val listener = onItemLongClickListener
        listener?.onItemLongClick(this, itemView, index, id)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onLayout(
            changed: Boolean, left: Int, top: Int, right: Int,
            bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        if (adapter == null) {
            Log.e("GraphView", "No adapter attached; skipping layout")
            return
        }

        removeAllViewsInLayout()    //TODO: recycle views
        positionItems()
        invalidate()
    }


    fun setLayout(layout: Layout?) {
        if (layout === this.layout) {
            return
        }
        this.layout = layout
        requestLayout()
    }

    override fun getSelectedView(): View? {
        return null
    }

    override fun setSelection(position: Int) {}

    override fun dispatchDraw(canvas: Canvas) {
        val adapter = getAdapter()
        adapter?.run {
            if (graph.hasNodes()) {
                layout?.drawEdges(canvas, graph, linePaint)
            }
        }
        super.dispatchDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val adapter = this.adapter ?: return
        if (!adapter.graph.hasNodes()) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
            return
        }

        var maxWidth = 0
        var maxHeight = 0
        var minHeight = Integer.MAX_VALUE

        for (i in 0 until adapter.count) {
            val child = adapter.getView(i, null, this)

            var params: MarginLayoutParams? = child.layoutParams as? MarginLayoutParams
            if (params == null) {
                params = MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }
            addViewInLayout(child, -1, params, true)
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val node = adapter.getItem(i) as Node
            val measuredWidth = child.measuredWidth
            val measuredHeight = child.measuredHeight
            node.size.apply {
                width = child.measuredWidth
                height = child.measuredHeight
            }

            maxWidth = max(maxWidth, measuredWidth)
            maxHeight = max(maxHeight, measuredHeight)
            minHeight = min(minHeight, measuredHeight)
        }

        maxChildWidth = maxWidth
        maxChildHeight = maxHeight

        if (isUsingMaxSize) {
            removeAllViewsInLayout()
            for (i in 0 until adapter.count) {
                val child = adapter.getView(i, null, this)

                var params: LayoutParams? = child.layoutParams
                if (params == null) {
                    params = LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT
                    )
                }
                addViewInLayout(child, -1, params, true)

                val widthSpec =
                        MeasureSpec.makeMeasureSpec(maxChildWidth, MeasureSpec.EXACTLY)
                val heightSpec =
                        MeasureSpec.makeMeasureSpec(maxChildHeight, MeasureSpec.EXACTLY)
                child.measure(widthSpec, heightSpec)

                val node = adapter.getItem(i) as Node
                node.size.apply {
                    width = child.measuredWidth
                    height = child.measuredHeight
                }
            }
        }

        adapter.notifyDataSetChanged()
        layout?.run {
            val size = run(adapter.graph, paddingLeft.toFloat(), paddingTop.toFloat())
            setMeasuredDimension(size.width + paddingRight + paddingLeft, size.height + paddingBottom + paddingTop)
        }
    }

    override fun setAdapter(adapter: GraphAdapter<ViewHolder>?) {
        val oldAdapter = this.adapter

        oldAdapter?.setGraphViewObserver(null)
        removeAllViewsInLayout()

        this.adapter = adapter

        this.adapter?.let {
            if (observer == null) {
                observer = GraphViewObserver()
            }
            it.setGraphViewObserver(observer)
            requestLayout()
        }
    }

    override fun getAdapter(): GraphAdapter<ViewHolder>? = adapter

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            clickChildAt(e.x.toInt(), e.y.toInt())
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            longClickChildAt(e.x.toInt(), e.y.toInt())
        }
    }

    private inner class GraphViewObserver : DataSetObserver() {
        override fun onChanged() {
            refresh()
        }

        override fun onInvalidated() {
            refresh()
        }

        private fun refresh() {
            requestLayout()
            invalidate()
        }
    }

    abstract class ViewHolder(val itemView: View)

    companion object {
        const val DEFAULT_USE_MAX_SIZE = false
        const val DEFAULT_LINE_THICKNESS = 5
        const val DEFAULT_LINE_COLOR = Color.BLACK
        const val INVALID_INDEX = -1

        private fun makeMeasureSpec(dimension: Int): Int {
            return if (dimension > 0) {
                MeasureSpec.makeMeasureSpec(dimension, MeasureSpec.EXACTLY)
            } else {
                MeasureSpec.UNSPECIFIED
            }
        }
    }
}
