package de.blox.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class GraphView extends AdapterView<GraphAdapter> implements GestureDetector.OnGestureListener {

    public static final boolean DEFAULT_USE_MAX_SIZE = false;
    private static final int DEFAULT_LINE_THICKNESS = 5;
    private static final int DEFAULT_LINE_COLOR = Color.BLACK;
    private static final int INVALID_INDEX = -1;

    private Paint mLinePaint = new Paint();
    private int mLineThickness;
    private int mLineColor;

    private boolean mUseMaxSize;

    private GraphAdapter mAdapter;
    private int mMaxChildWidth;
    private int mMaxChildHeight;
    private Rect mRect;
    private Rect mBoundaries = new Rect();

    private DataSetObserver mDataSetObserver;

    private GestureDetector mGestureDetector;

    public GraphView(Context context) {
        this(context, null);
    }

    public GraphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GraphView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, de.blox.graphview.R.styleable.GraphView, 0, 0);
        try {
            mLineThickness = a.getDimensionPixelSize(de.blox.graphview.R.styleable.GraphView_lineThickness, DEFAULT_LINE_THICKNESS);
            mLineColor = a.getColor(de.blox.graphview.R.styleable.GraphView_lineColor, DEFAULT_LINE_COLOR);
            mUseMaxSize = a.getBoolean(de.blox.graphview.R.styleable.GraphView_useMaxSize, DEFAULT_USE_MAX_SIZE);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        mGestureDetector = new GestureDetector(context, this);

        if (attrs != null) {
            initAttrs(context, attrs);
        }
        initPaint();
    }

    private void initPaint() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineThickness);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mLinePaint.setPathEffect(new CornerPathEffect(10));   // set the path effect when they join.
    }

    private void positionItems() {
        int maxLeft = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int maxTop = Integer.MAX_VALUE;
        int maxBottom = Integer.MIN_VALUE;

        for (int index = 0; index < mAdapter.getCount(); index++) {
            final View child = mAdapter.getView(index, null, this);
            addAndMeasureChild(child);

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            final Vector screenPosition = mAdapter.getScreenPosition(index);

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

        mBoundaries.set(maxLeft - (getWidth() - Math.abs(maxLeft)) - Math.abs(maxLeft), -getHeight(), maxRight, maxBottom);
    }

    private void addAndMeasureChild(final View child) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        addViewInLayout(child, -1, params, false);
        int widthSpec = makeMeasureSpec(params.width);
        int heightSpec = makeMeasureSpec(params.height);

        if (mUseMaxSize) {
            widthSpec = MeasureSpec.makeMeasureSpec(
                    mMaxChildWidth, MeasureSpec.EXACTLY);
            heightSpec = MeasureSpec.makeMeasureSpec(
                    mMaxChildHeight, MeasureSpec.EXACTLY);
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
        if (mRect == null) {
            mRect = new Rect();
        }
        for (int index = 0; index < getChildCount(); index++) {
            getChildAt(index).getHitRect(mRect);
            if (mRect.contains(x, y)) {
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
        final long id = mAdapter.getItemId(index);
        performItemClick(itemView, index, id);
    }

    private void longClickChildAt(final int x, final int y) {

        final int index = getContainingChildIndex(x, y);
        // no child found at this position
        if (index == INVALID_INDEX) {
            return;
        }

        final View itemView = getChildAt(index);
        final long id = mAdapter.getItemId(index);
        OnItemLongClickListener listener = getOnItemLongClickListener();
        if (listener != null) {
            listener.onItemLongClick(this, itemView, index, id);
        }
    }

    /**
     * @return Returns the value of how thick the lines between the nodes are.
     */
    public int getLineThickness() {
        return mLineThickness;
    }

    /**
     * Sets a new value for the thickness of the lines between the nodes.
     *
     * @param lineThickness new value for the thickness
     */
    public void setLineThickness(int lineThickness) {
        mLineThickness = lineThickness;
        initPaint();
        invalidate();
    }

    /**
     * @return Returns the color of the lines between the nodes.
     */
    @ColorInt
    public int getLineColor() {
        return mLineColor;
    }

    /**
     * Sets a new color for the lines between the nodes.A change to this value
     * invokes a re-drawing of the tree.
     *
     * @param lineColor the new color
     */
    public void setLineColor(@ColorInt int lineColor) {
        mLineColor = lineColor;
        initPaint();
        invalidate();
    }


    /**
     * @return <code>true</code> if using same size for each node, <code>false</code> otherwise.
     */
    public boolean isUsingMaxSize() {
        return mUseMaxSize;
    }

    /**
     * Whether to use the max available size for each node, so all nodes have the same size. A
     * change to this value invokes a re-drawing of the tree.
     *
     * @param useMaxSize <code>true</code> if using same size for each node, <code>false</code> otherwise.
     */
    public void setUseMaxSize(boolean useMaxSize) {
        mUseMaxSize = useMaxSize;
        invalidate();
        requestLayout();
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        clickChildAt((int) e.getX() + getScrollX(), (int) e.getY() + getScrollY());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent downEvent, MotionEvent event, float distanceX, float distanceY) {
        final float newScrollX = getScrollX() + distanceX;
        final float newScrollY = getScrollY() + distanceY;

        if (mBoundaries.contains((int) newScrollX, (int) newScrollY)) {
            scrollBy((int) distanceX, (int) distanceY);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        longClickChildAt((int) event.getX() + getScrollX(), (int) event.getY() + getScrollY());
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public GraphAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(GraphAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;
        mDataSetObserver = new GraphDataSetObserver();
        mAdapter.registerDataSetObserver(mDataSetObserver);

        requestLayout();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right,
                            final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null) {
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
        mAdapter.getAlgorithm().drawEdges(canvas, mAdapter.getGraph(), mLinePaint);

        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mAdapter == null) {
            return;
        }

        int maxWidth = 0;
        int maxHeight = 0;
        int minHeight = Integer.MAX_VALUE;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            View child = mAdapter.getView(i, null, this);

            LayoutParams params = child.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            }
            addViewInLayout(child, -1, params, true);

            int childWidthSpec = makeMeasureSpec(params.width);
            int childHeightSpec = makeMeasureSpec(params.height);

            child.measure(childWidthSpec, childHeightSpec);
            Node node = mAdapter.getNode(i);
            final int measuredWidth = child.getMeasuredWidth();
            final int measuredHeight = child.getMeasuredHeight();
            node.setSize(measuredWidth, measuredHeight);

            maxWidth = Math.max(maxWidth, measuredWidth);
            maxHeight = Math.max(maxHeight, measuredHeight);
            minHeight = Math.min(minHeight, measuredHeight);
        }

        mMaxChildWidth = maxWidth;
        mMaxChildHeight = maxHeight;

        if (mUseMaxSize) {
            removeAllViewsInLayout();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                View child = mAdapter.getView(i, null, this);

                LayoutParams params = child.getLayoutParams();
                if (params == null) {
                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                }
                addViewInLayout(child, -1, params, true);

                final int widthSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth, MeasureSpec.EXACTLY);
                final int heightSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight, MeasureSpec.EXACTLY);
                child.measure(widthSpec, heightSpec);

                Node node = mAdapter.getNode(i);
                node.setSize(child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
        mAdapter.getAlgorithm().setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        mAdapter.notifySizeChanged();
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
