package de.blox.graphview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import de.blox.graphview.tree.BuchheimWalkerConfiguration


class GraphView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private val recyclerView: RecyclerView = RecyclerViewImpl(context)
    private val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(300)
            .setSubtreeSeparation(300)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()

    //    var layoutManager = GraphLayoutManager(context, BuchheimWalkerAlgorithm(configuration))
//
    // reused in layout(...)
    private val tmpContainerRect: Rect = Rect()
    private val tmpChildRect: Rect = Rect()

    init {
        recyclerView.id = ViewCompat.generateViewId()
        recyclerView.descendantFocusability = FOCUS_BEFORE_DESCENDANTS

        //recyclerView.layoutManager = layoutManager
        //recyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)

        recyclerView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        attachViewToParent(recyclerView, 0, recyclerView.layoutParams)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // TODO(b/70666622): consider margin support
        // TODO(b/70666626): consider delegating all this to RecyclerView
        measureChild(recyclerView, widthMeasureSpec, heightMeasureSpec)
        var width: Int = recyclerView.measuredWidth
        var height: Int = recyclerView.measuredHeight
        val childState: Int = recyclerView.measuredState
        width += paddingLeft + paddingRight
        height += paddingTop + paddingBottom
        width = Math.max(width, suggestedMinimumWidth)
        height = Math.max(height, suggestedMinimumHeight)
        setMeasuredDimension(View.resolveSizeAndState(width, widthMeasureSpec, childState),
                View.resolveSizeAndState(height, heightMeasureSpec,
                        childState shl View.MEASURED_HEIGHT_STATE_SHIFT))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width: Int = recyclerView.measuredWidth
        val height: Int = recyclerView.measuredHeight

        // TODO(b/70666626): consider delegating padding handling to the RecyclerView to avoid
        // an unnatural page transition effect: http://shortn/_Vnug3yZpQT
        tmpContainerRect.left = paddingLeft
        tmpContainerRect.right = r - l - paddingRight
        tmpContainerRect.top = paddingTop
        tmpContainerRect.bottom = b - t - paddingBottom

        Gravity.apply(Gravity.TOP or Gravity.START, width, height, tmpContainerRect, tmpChildRect)
        recyclerView.layout(tmpChildRect.left, tmpChildRect.top, tmpChildRect.right,
                tmpChildRect.bottom)
    }

    /**
     *  @param adapter The adapter to use, or {@code null} to remove the current adapter
     *  @see RecyclerView#setAdapter(Adapter)
     */
    fun setAdapter(@Nullable adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter = adapter
    }

    fun getAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>? {
        return recyclerView.adapter
    }

    /**
     * Slightly modified RecyclerView to disable user scrolling.
     */
    private class RecyclerViewImpl(context: Context) : RecyclerView(context) {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean {
            return false
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            return false
        }
    }

    /**
     * Add an [ItemDecoration] to this ViewPager2. Item decorations can
     * affect both measurement and drawing of individual item views.
     *
     *
     * Item decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.
     *
     * @param decor Decoration to add
     */
    fun addItemDecoration(decor: ItemDecoration) {
        recyclerView.addItemDecoration(decor)
    }

    /**
     * Add an [ItemDecoration] to this ViewPager2. Item decorations can
     * affect both measurement and drawing of individual item views.
     *
     *
     * Item decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.
     *
     * @param decor Decoration to add
     * @param index Position in the decoration chain to insert this decoration at. If this value
     * is negative the decoration will be added at the end.
     * @throws IndexOutOfBoundsException on indexes larger than [.getItemDecorationCount]
     */
    fun addItemDecoration(decor: ItemDecoration, index: Int) {
        recyclerView.addItemDecoration(decor, index)
    }

    /**
     * Returns an [ItemDecoration] previously added to this ViewPager2.
     *
     * @param index The index position of the desired ItemDecoration.
     * @return the ItemDecoration at index position
     * @throws IndexOutOfBoundsException on invalid index
     */
    fun getItemDecorationAt(index: Int): ItemDecoration {
        return recyclerView.getItemDecorationAt(index)
    }

    /**
     * Returns the number of [ItemDecoration] currently added to this ViewPager2.
     *
     * @return number of ItemDecorations currently added added to this ViewPager2.
     */
    fun getItemDecorationCount(): Int {
        return recyclerView.itemDecorationCount
    }

    /**
     * Invalidates all ItemDecorations. If ViewPager2 has item decorations, calling this method
     * will trigger a [.requestLayout] call.
     */
    fun invalidateItemDecorations() {
        recyclerView.invalidateItemDecorations()
    }

    /**
     * Removes the [ItemDecoration] associated with the supplied index position.
     *
     * @param index The index position of the ItemDecoration to be removed.
     * @throws IndexOutOfBoundsException on invalid index
     */
    fun removeItemDecorationAt(index: Int) {
        recyclerView.removeItemDecorationAt(index)
    }

    /**
     * Remove an [ItemDecoration] from this ViewPager2.
     *
     *
     * The given decoration will no longer impact the measurement and drawing of
     * item views.
     *
     * @param decor Decoration to remove
     * @see .addItemDecoration
     */
    fun removeItemDecoration(decor: ItemDecoration) {
        recyclerView.removeItemDecoration(decor)
    }


}