package de.blox.treeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class TreeView extends AdapterView<TreeAdapter> implements GestureDetector.OnGestureListener {

    private static final int DEFAULT_LINE_LENGTH = 100;
    private static final int DEFAULT_LINE_THICKNESS = 5;
    private static final int DEFAULT_LINE_COLOR = Color.BLACK;
    public static final boolean DEFAULT_USE_MAX_SIZE = false;
    private static final int INVALID_INDEX = -1;

    Path mLinePath = new Path();
    Paint mLinePaint = new Paint();
    private int mLineThickness;
    private int mLineColor;
    private int mLevelSeparation;

    private boolean mUseMaxSize;

    private TreeAdapter mAdapter;
    private int mMaxChildWidth;
    private int mMaxChildHeight;
    private int mMinChildHeight;
    private Rect mRect;
    private Rect mBoundaries = new Rect();

    private DataSetObserver mDataSetObserver;

    private GestureDetector mGestureDetector;

    public TreeView(Context context) {
        this(context, null);
    }

    public TreeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TreeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TreeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TreeView, 0, 0);
        try {
            mLevelSeparation = a.getDimensionPixelSize(R.styleable.TreeView_levelSeparation, DEFAULT_LINE_LENGTH);
            mLineThickness = a.getDimensionPixelSize(R.styleable.TreeView_lineThickness, DEFAULT_LINE_THICKNESS);
            mLineColor = a.getColor(R.styleable.TreeView_lineColor, DEFAULT_LINE_COLOR);
            mUseMaxSize = a.getBoolean(R.styleable.TreeView_useMaxSize, DEFAULT_USE_MAX_SIZE);
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
//        mLinePaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mLinePaint.setPathEffect(new CornerPathEffect(10));   // set the path effect when they join.
    }

    private void positionItems() {
        int maxLeft = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int maxTop = Integer.MAX_VALUE;
        int maxBottom = Integer.MIN_VALUE;

        int globalPadding = 0;
        int localPadding = 0;
        int currentLevel = 0;
        for (int index = 0; index < mAdapter.getCount(); index++) {
            final View child = mAdapter.getView(index, null, this);

            addAndMeasureChild(child);

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            final Point screenPosition = mAdapter.getScreenPosition(index);
            TreeNode node = mAdapter.getNode(index);

            if (height > mMinChildHeight) {
                localPadding = Math.max(localPadding, height - mMinChildHeight);
            }

            if (currentLevel != node.getLevel()) {
                globalPadding += localPadding;
                localPadding = 0;
                currentLevel = node.getLevel();
            }

            // calculate the size and position of this child
            final int left = screenPosition.x + getScreenXCenter();
            final int top = screenPosition.y * mMinChildHeight + (node.getLevel() * mLevelSeparation) + globalPadding;
            final int right = left + width;
            final int bottom = top + height;

            child.layout(left, top, right, bottom);
            node.setX(left);
            node.setY(top);

            maxRight = Math.max(maxRight, right);
            maxLeft = Math.min(maxLeft, left);
            maxBottom = Math.max(maxBottom, bottom);
            maxTop = Math.min(maxTop, top);
        }

        mBoundaries.set(maxLeft - (getWidth() - Math.abs(maxLeft)) - Math.abs(maxLeft), -getHeight(), maxRight, maxBottom);
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

    private void addAndMeasureChild(final View child) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        addViewInLayout(child, -1, params, false);

        int widthSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        if (mUseMaxSize) {
            widthSpec = MeasureSpec.makeMeasureSpec(
                    mMaxChildWidth, MeasureSpec.EXACTLY);
            heightSpec = MeasureSpec.makeMeasureSpec(
                    mMaxChildHeight, MeasureSpec.EXACTLY);
        }

        child.measure(widthSpec, heightSpec);
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

    private void drawLines(Canvas canvas, TreeNode treeNode) {
        if (treeNode.hasChildren()) {
            for (TreeNode child : treeNode.getChildren()) {
                drawLines(canvas, child);
            }
        }

        if (treeNode.hasParent()) {
            mLinePath.reset();

            TreeNode parent = treeNode.getParent();
            mLinePath.moveTo(treeNode.getX() + (treeNode.getWidth() / 2), treeNode.getY());
            mLinePath.lineTo(treeNode.getX() + (treeNode.getWidth() / 2), treeNode.getY() - (mLevelSeparation / 2));
            mLinePath.lineTo(parent.getX() + (parent.getWidth() / 2),
                    treeNode.getY() - mLevelSeparation / 2);

            canvas.drawPath(mLinePath, mLinePaint);
            mLinePath.reset();

            mLinePath.moveTo(parent.getX() + (parent.getWidth() / 2),
                    treeNode.getY() - mLevelSeparation / 2);
            mLinePath.lineTo(parent.getX() + (parent.getWidth() / 2),
                    parent.getY() + parent.getHeight());

            canvas.drawPath(mLinePath, mLinePaint);
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
     * Returns the value of how much space should be used between two levels.
     *
     * @return level separation value
     */
    @Px
    public int getLevelSeparation() {
        return mLevelSeparation;
    }

    /**
     * Sets a new value of how much space should be used between two levels. A change to this value
     * invokes a re-drawing of the tree.
     *
     * @param levelSeparation new value for the level separation
     */
    public void setLevelSeparation(@Px int levelSeparation) {
        mLevelSeparation = levelSeparation;
        invalidate();
        requestLayout();
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
    public TreeAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(TreeAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;
        mDataSetObserver = new TreeDataSetObserver();
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

    private int getScreenXCenter() {
        return (int) getPivotX() - getChildAt(0).getMeasuredWidth() / 2;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        TreeNode rootNode = mAdapter.getNode(0);
        if (rootNode != null) {
            drawLines(canvas, rootNode);
        }
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
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

            TreeNode node = mAdapter.getNode(i);
            final int measuredWidth = child.getMeasuredWidth();
            final int measuredHeight = child.getMeasuredHeight();
            node.setSize(measuredWidth, measuredHeight);

            maxWidth = Math.max(maxWidth, measuredWidth);
            maxHeight = Math.max(maxHeight, measuredHeight);
            minHeight = Math.min(minHeight, measuredHeight);
        }

        mMaxChildWidth = maxWidth;
        mMaxChildHeight = maxHeight;
        mMinChildHeight = minHeight;

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

                TreeNode node = mAdapter.getNode(i);
                node.setSize(child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }

        mAdapter.notifySizeChanged();
    }

    private class TreeDataSetObserver extends DataSetObserver {
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
            invalidate();
            requestLayout();
        }
    }
}
