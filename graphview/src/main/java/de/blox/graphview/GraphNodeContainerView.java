package de.blox.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

class GraphNodeContainerView extends AdapterView<GraphAdapter> {

    public static final boolean DEFAULT_USE_MAX_SIZE = false;
    private static final int DEFAULT_LINE_THICKNESS = 5;
    private static final int DEFAULT_LINE_COLOR = Color.BLACK;
    private static final int INVALID_INDEX = -1;
    private Paint linePaint = new Paint();
    private int lineThickness;
    private int lineColor;
    private boolean useMaxSize;
    private GraphAdapter adapter;
    private int maxChildWidth;
    private int maxChildHeight;
    private Rect rect = new Rect();
    private DataSetObserver dataSetObserver;
    private GestureDetector gestureDetector;

    public GraphNodeContainerView(Context context) {
        this(context, null);
    }

    public GraphNodeContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphNodeContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Create a {@link MeasureSpec} depending on the value of {@code dimension}.
     * If the value is negative ({@link LayoutParams#MATCH_PARENT} or
     * {@link LayoutParams#WRAP_CONTENT}) this returns {@link MeasureSpec#UNSPECIFIED}. If positive
     * it returns a MeasureSpec based on the value with the {@link MeasureSpec#EXACTLY} mode.
     *
     * @param dimension value of the dimension
     * @return {@link MeasureSpec#UNSPECIFIED} or a MeasureSpec based on {@code dimension}
     */
    private static int makeMeasureSpec(int dimension) {
        int spec;
        if (dimension > 0) {
            spec = MeasureSpec.makeMeasureSpec(dimension, MeasureSpec.EXACTLY);
        } else {
            spec = MeasureSpec.UNSPECIFIED;
        }
        return spec;
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            initAttrs(context, attrs);
        }
        initPaint();

        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GraphView, 0, 0);

        lineThickness = a.getDimensionPixelSize(R.styleable.GraphView_lineThickness, DEFAULT_LINE_THICKNESS);
        lineColor = a.getColor(R.styleable.GraphView_lineColor, DEFAULT_LINE_COLOR);
        useMaxSize = a.getBoolean(R.styleable.GraphView_useMaxSize, DEFAULT_USE_MAX_SIZE);

        a.recycle();
    }

    private void initPaint() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(lineThickness);
        linePaint.setColor(lineColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        linePaint.setPathEffect(new CornerPathEffect(10));   // set the path effect when they join.
    }

    private void positionItems() {
        int maxLeft = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int maxTop = Integer.MAX_VALUE;
        int maxBottom = Integer.MIN_VALUE;

        for (int index = 0; index < adapter.getCount(); index++) {
            final View child = adapter.getView(index, null, this);
            addAndMeasureChild(child);

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            final Vector screenPosition = adapter.getScreenPosition(index);

            // calculate the size and position of this child
            final int left = (int) screenPosition.getX();
            final int top = (int) screenPosition.getY();
            final int right = left + width;
            final int bottom = top + height;

            child.layout(left, top, right, bottom);

            maxRight = Math.max(maxRight, right);
            maxLeft = Math.min(maxLeft, left);
            maxBottom = Math.max(maxBottom, bottom);
            maxTop = Math.min(maxTop, top);
        }
    }

    private void addAndMeasureChild(final View child) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        addViewInLayout(child, -1, params, false);
        int widthSpec = makeMeasureSpec(params.width);
        int heightSpec = makeMeasureSpec(params.height);

        if (useMaxSize) {
            widthSpec = MeasureSpec.makeMeasureSpec(
                    maxChildWidth, MeasureSpec.EXACTLY);
            heightSpec = MeasureSpec.makeMeasureSpec(
                    maxChildHeight, MeasureSpec.EXACTLY);
        }

        child.measure(widthSpec, heightSpec);
    }



    /**
     * Returns the index of the child that contains the coordinates given.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The index of the child that contains the coordinates. If no child
     * is found then it returns INVALID_INDEX
     */
    private int getContainingChildIndex(final int x, final int y) {
        if (rect == null) {
            rect = new Rect();
        }
        for (int index = 0; index < getChildCount(); index++) {
            getChildAt(index).getHitRect(rect);
            if (rect.contains(x, y)) {
                return index;
            }
        }
        return INVALID_INDEX;
    }

    private void clickChildAt(final int x, final int y) {
        final int index = getContainingChildIndex(x, y);
        // no child found at this position
        if (index == INVALID_INDEX) {
            return;
        }

        final View itemView = getChildAt(index);
        final long id = adapter.getItemId(index);
        performItemClick(itemView, index, id);
    }

    private void longClickChildAt(final int x, final int y) {
        final int index = getContainingChildIndex(x, y);
        // no child found at this position
        if (index == INVALID_INDEX) {
            return;
        }

        final View itemView = getChildAt(index);
        final long id = adapter.getItemId(index);
        OnItemLongClickListener listener = getOnItemLongClickListener();
        if (listener != null) {
            listener.onItemLongClick(this, itemView, index, id);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    /**
     * @return Returns the value of how thick the lines between the nodes are.
     */
    public int getLineThickness() {
        return lineThickness;
    }

    /**
     * Sets a new value for the thickness of the lines between the nodes.
     *
     * @param lineThickness new value for the thickness
     */
    public void setLineThickness(@Px int lineThickness) {
        this.lineThickness = lineThickness;
        initPaint();
        invalidate();
    }

    /**
     * @return Returns the color of the lines between the nodes.
     */
    @ColorInt
    public int getLineColor() {
        return lineColor;
    }

    /**
     * Sets a new color for the lines between the nodes.A change to this value
     * invokes a re-drawing of the tree.
     *
     * @param lineColor the new color
     */
    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
        initPaint();
        invalidate();
    }

    /**
     * @return <code>true</code> if using same size for each node, <code>false</code> otherwise.
     */
    public boolean isUsingMaxSize() {
        return useMaxSize;
    }

    /**
     * Whether to use the max available size for each node, so all nodes have the same size. A
     * change to this value invokes a re-drawing of the tree.
     *
     * @param useMaxSize <code>true</code> if using same size for each node, <code>false</code> otherwise.
     */
    public void setUseMaxSize(boolean useMaxSize) {
        this.useMaxSize = useMaxSize;
        invalidate();
        requestLayout();
    }

    @Override
    public GraphAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(GraphAdapter adapter) {
        if (this.adapter != null && dataSetObserver != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }

        this.adapter = adapter;
        dataSetObserver = new GraphDataSetObserver();
        this.adapter.registerDataSetObserver(dataSetObserver);

        requestLayout();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
                            final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (adapter == null) {
            return;
        }

        removeAllViewsInLayout();
        positionItems();

        invalidate();
    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int position) {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        GraphAdapter adapter = getAdapter();
        if (adapter != null && adapter.getGraph() != null) {
            adapter.getAlgorithm().drawEdges(canvas, adapter.getGraph(), linePaint);
        }

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (adapter == null) {
            return;
        }

        int maxWidth = 0;
        int maxHeight = 0;
        int minHeight = Integer.MAX_VALUE;

        for (int i = 0; i < adapter.getCount(); i++) {
            View child = adapter.getView(i, null, this);

            LayoutParams params = child.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }
            addViewInLayout(child, -1, params, true);

            int childWidthSpec = makeMeasureSpec(params.width);
            int childHeightSpec = makeMeasureSpec(params.height);

            child.measure(childWidthSpec, childHeightSpec);
            Node node = adapter.getNode(i);
            final int measuredWidth = child.getMeasuredWidth();
            final int measuredHeight = child.getMeasuredHeight();
            node.setSize(measuredWidth, measuredHeight);

            maxWidth = Math.max(maxWidth, measuredWidth);
            maxHeight = Math.max(maxHeight, measuredHeight);
            minHeight = Math.min(minHeight, measuredHeight);
        }

        maxChildWidth = maxWidth;
        maxChildHeight = maxHeight;

        if (useMaxSize) {
            removeAllViewsInLayout();
            for (int i = 0; i < adapter.getCount(); i++) {
                View child = adapter.getView(i, null, this);

                LayoutParams params = child.getLayoutParams();
                if (params == null) {
                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                }
                addViewInLayout(child, -1, params, true);

                final int widthSpec = MeasureSpec.makeMeasureSpec(maxChildWidth, MeasureSpec.EXACTLY);
                final int heightSpec = MeasureSpec.makeMeasureSpec(maxChildHeight, MeasureSpec.EXACTLY);
                child.measure(widthSpec, heightSpec);

                Node node = adapter.getNode(i);
                node.setSize(child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
        adapter.notifySizeChanged();

        Size size = fetchViewSize();
        setMeasuredDimension(size.getWidth(), size.getHeight());
    }

    private Size fetchViewSize() {
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (int i = 0; i < adapter.getCount(); i++) {
            Node node = adapter.getNode(i);
            left = (int) Math.min(left, node.getX());
            top = (int) Math.min(top, node.getY());
            right = (int) Math.max(right, node.getX() + node.getWidth());
            bottom = (int) Math.max(bottom, node.getY() + node.getHeight());
        }

        return new Size(right - left, bottom - top);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            clickChildAt((int) e.getX(), (int) e.getY());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            longClickChildAt((int) e.getX(), (int) e.getY());
        }
    }

    private class GraphDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();

            refresh();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();

            refresh();
        }

        private void refresh() {
            requestLayout();
            invalidate();
        }
    }
}
