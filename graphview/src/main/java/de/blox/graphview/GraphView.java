package de.blox.graphview;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.otaliastudios.zoom.ZoomLayout;

public class GraphView extends ZoomLayout {

    private GraphNodeContainerView graphNodeContainerView;

    public GraphView(Context context) {
        this(context, null);
    }

    public GraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        graphNodeContainerView = new GraphNodeContainerView(context, attrs, defStyleAttr);
        addView(graphNodeContainerView, -1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof GraphNodeContainerView)) {
            throw new RuntimeException("GraphView can have only GraphContainer as a child");
        }

        super.addView(child, index, params);
    }

    /**
     * @return Returns the value of how thick the lines between the nodes are.
     */
    public int getLineThickness() {
        return graphNodeContainerView.getLineThickness();
    }

    /**
     * Sets a new value for the thickness of the lines between the nodes.
     *
     * @param lineThickness new value for the thickness
     */
    public void setLineThickness(@Px int lineThickness) {
        graphNodeContainerView.setLineThickness(lineThickness);
    }

    /**
     * @return Returns the color of the lines between the nodes.
     */
    @ColorInt
    public int getLineColor() {
        return graphNodeContainerView.getLineColor();
    }

    /**
     * Sets a new color for the lines between the nodes.A change to this value
     * invokes a re-drawing of the tree.
     *
     * @param lineColor the new color
     */
    public void setLineColor(@ColorInt int lineColor) {
        graphNodeContainerView.setLineColor(lineColor);
    }

    /**
     * @return <code>true</code> if using same size for each node, <code>false</code> otherwise.
     */
    public boolean isUsingMaxSize() {
        return graphNodeContainerView.isUsingMaxSize();
    }

    /**
     * Whether to use the max available size for each node, so all nodes have the same size. A
     * change to this value invokes a re-drawing of the tree.
     *
     * @param useMaxSize <code>true</code> if using same size for each node, <code>false</code> otherwise.
     */
    public void setUseMaxSize(boolean useMaxSize) {
        graphNodeContainerView.setUseMaxSize(useMaxSize);
    }

    public void setAdapter(GraphAdapter adapter) {
        graphNodeContainerView.setAdapter(adapter);
    }

    public GraphAdapter getAdapter() {
        return graphNodeContainerView.getAdapter();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        graphNodeContainerView.setOnItemClickListener(onItemClickListener);
    }
}
