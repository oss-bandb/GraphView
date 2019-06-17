package de.blox.graphview

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.Px
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView

import com.otaliastudios.zoom.ZoomLayout

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ZoomLayout(context, attrs, defStyleAttr) {

    private val graphNodeContainerView: GraphNodeContainerView =
        GraphNodeContainerView(context, attrs, defStyleAttr)

    init {
        super.addView(
            graphNodeContainerView,
            -1,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setHasClickableChildren(true)
    }

    var lineThickness: Int
        get() = graphNodeContainerView.lineThickness
        set(@Px lineThickness) {
            graphNodeContainerView.lineThickness = lineThickness
        }

    var lineColor: Int
        @ColorInt
        get() = graphNodeContainerView.lineColor
        set(@ColorInt lineColor) {
            graphNodeContainerView.lineColor = lineColor
        }

    val isUsingMaxSize: Boolean
        get() = graphNodeContainerView.isUsingMaxSize

    var adapter: GraphAdapter<*>?
        get() = graphNodeContainerView.adapter
        set(adapter) {
            graphNodeContainerView.adapter = adapter
        }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child !is GraphNodeContainerView) {
            throw RuntimeException("GraphView can have only GraphContainer as a child")
        }

        super.addView(child, index, params)
    }

    fun setUseMaxSize(useMaxSize: Boolean) {
        graphNodeContainerView.isUsingMaxSize = useMaxSize
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        graphNodeContainerView.onItemClickListener = onItemClickListener
    }
}
