/*
 * Copyright (C) 2016 LuckyJayce
 * Copyright (C) 2016 liaohuqiu
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shizhefei.view.coolrefreshview;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.shizhefei.view.coolrefreshview.header.DefaultHeader;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

public class CoolRefreshView extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild {
    private static final String LOG_TAG = "CoolRefreshView";
    private ProxyPullHeader mPullHandler;
    private View mHeaderView;
    private View mContentView;

    private static IPullHeaderFactory HEADER_FACTORY = new IPullHeaderFactory() {
        @Override
        public PullHeader made(Context context) {
            return new DefaultHeader();
        }

        @Override
        public boolean isPinContent() {
            return false;
        }
    };
    private static boolean DEBUG = false;

    private boolean mIsPinContent = true;
    private ScrollerHelper scrollerHelper;
    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private int mTouchSlop;
    private boolean mNestedScrollInProgress;
    private float mInitialDownY;
    private float mInitialMotionY;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private float mLastMotionY;

    private byte mStatus = PULL_STATUS_INIT;

    //没有任何操作
    public final static byte PULL_STATUS_INIT = 1;
    //开始下拉
    public final static byte PULL_STATUS_TOUCH_MOVE = 2;
    //回到原始位置
    public final static byte PULL_STATUS_RESET = 3;
    //刷新中
    public final static byte PULL_STATUS_REFRESHING = 4;
    //刷新完成
    public final static byte PULL_STATUS_COMPLETE = 5;

    public CoolRefreshView(Context context) {
        super(context);
        init();
    }

    public CoolRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoolRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPullHandler = new ProxyPullHeader(HEADER_FACTORY.made(getContext()));
        mIsPinContent = HEADER_FACTORY.isPinContent();
        if (mIsPinContent) {
            scrollerHelper = new PinContentScroller();
        } else {
            scrollerHelper = new AllScroller();
        }
        setWillNotDraw(false);
        addHeadView();
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    /**
     * 静态设置全局的IPullHeaderFactory，一次配置，所有默认使用这个factory生成的PullHeader
     * 该代码可以放在Application onCreate
     *
     * @param factory
     */
    public static void setPullHeaderFactory(IPullHeaderFactory factory) {
        HEADER_FACTORY = factory;
    }

    /**
     * 设置刷新的头部
     *
     * @param pullHeader
     */
    public void setPullHeader(PullHeader pullHeader) {
        setPullHeader(pullHeader, false);
    }

    /**
     * @param pullHeader   刷新的头部
     * @param isPinContent true滚定内容不下拉，只下拉头部， false 一起下拉
     */
    public void setPullHeader(PullHeader pullHeader, boolean isPinContent) {
        if (!scrollerHelper.isFinished()) {
            scrollerHelper.abortAnimation();
        }
        if (mIsPinContent != isPinContent) {
            if (isPinContent) {
                scrollerHelper = new PinContentScroller();
            } else {
                scrollerHelper = new AllScroller();
            }
            mIsPinContent = isPinContent;
        }
        mPullHandler.setPullHandler(pullHeader);
        removeView(mHeaderView);
        addHeadView();
    }

    private void addHeadView() {
        mHeaderView = mPullHandler.createHeaderView(this);
        LayoutParams layoutParams = mHeaderView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        addView(mHeaderView, layoutParams);
    }

    public void addOnPullListener(OnPullListener onPullListener) {
        mPullHandler.addListener(onPullListener);
    }

    public void removeOnPullListener(OnPullListener onPullListener) {
        mPullHandler.removeListener(onPullListener);
    }

    public void setRefreshing(final boolean refreshing) {
        if (getWidth() == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    postSetRefreshing(refreshing);
                }
            });
        } else {
            postSetRefreshing(refreshing);
        }
    }

    private void postSetRefreshing(boolean refreshing) {
        if (refreshing) {
            if (!mRefreshing) {
                mStatus = PULL_STATUS_REFRESHING;
                mRefreshing = refreshing;
                mPullHandler.onRefreshing(CoolRefreshView.this);
                int offsetToKeepHeaderWhileLoading = mPullHandler.getConfig().offsetToKeepHeaderWhileLoading(this, mHeaderView);
                int dy = -offsetToKeepHeaderWhileLoading - scrollerHelper.getOffsetY();
                scrollerHelper.startScroll(scrollerHelper.getOffsetX(), scrollerHelper.getOffsetY(), 0, dy);
            }
        } else {
            if (mRefreshing) {
                mStatus = PULL_STATUS_COMPLETE;
                mRefreshing = refreshing;
                mPullHandler.onPullRefreshComplete(CoolRefreshView.this);
                int dy = -scrollerHelper.getOffsetY();
                scrollerHelper.startScroll(scrollerHelper.getOffsetX(), scrollerHelper.getOffsetY(), 0, dy);
            }
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            reset(false);
        }
    }

    private void reset(boolean pullRelease) {
        mRefreshing = false;
        mStatus = PULL_STATUS_RESET;
        mPullHandler.onReset(this, pullRelease);
        int dy = -scrollerHelper.getOffsetY();
        scrollerHelper.startScroll(scrollerHelper.getOffsetX(), scrollerHelper.getOffsetY(), 0, dy);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset(false);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 1) {
            throw new IllegalStateException("CoolRefreshView can host only one direct child");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 1) {
            throw new IllegalStateException("CoolRefreshView can host only one direct child");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 1) {
            throw new IllegalStateException("CoolRefreshView can host only one direct child");
        }
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 1) {
            throw new IllegalStateException("CoolRefreshView can host only one direct child");
        }
        super.addView(child, index, params);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        View contentView = getContentView();
        if ((android.os.Build.VERSION.SDK_INT < 21 && contentView instanceof AbsListView)
                || (contentView != null && !ViewCompat.isNestedScrollingEnabled(contentView))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && !mRefreshing
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mNestedScrollInProgress = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // If we are in the middle of consuming, a scroll, then we want to touchMove the spinner back up
        // before allowing the list to scroll
        int offsetY = scrollerHelper.getOffsetY();
        if (dy > 0 && offsetY < 0) {
            int absOffsetY = Math.abs(offsetY);
            if (dy > absOffsetY) {
                consumed[1] = dy - absOffsetY;
            } else {
                consumed[1] = dy;
            }
            touchMove(consumed[1]);
        }

        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll

//        if (mTotalUnconsumed > 0) {
        finishSpinner();
//        }
        // Dispatch up our nested parent
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canChildScrollUp()) {
            touchMove(dy);
        }
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (!isEnabled() || canChildScrollUp()
                || mRefreshing || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                mLastMotionY = getMotionEventY(ev, pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;

        if (!isEnabled() || canChildScrollUp()
                || mRefreshing || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                mLastMotionY = getMotionEventY(ev, mActivePointerId);
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    float dy = mLastMotionY - y;
                    touchMove((int) dy);
                    mLastMotionY = y;
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG,
                            "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
//                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishSpinner();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }


    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private void finishSpinner() {
        int offsetToRefresh = mPullHandler.getConfig().offsetToRefresh(CoolRefreshView.this, mHeaderView);
        int scrollY = scrollerHelper.getOffsetY();
        int currentDistance = -scrollY;
        if (currentDistance > offsetToRefresh) {
            mStatus = PULL_STATUS_REFRESHING;
            setRefreshing(true);
        } else {
            reset(true);
        }
    }


    private void touchMove(int dy) {
        if (!scrollerHelper.isFinished()) {
            scrollerHelper.abortAnimation();
        }
        int scrollX = scrollerHelper.getOffsetX();
        int scrollY = scrollerHelper.getOffsetY();
        int totalDistance = mPullHandler.getConfig().totalDistance(CoolRefreshView.this, mHeaderView);
        int currentDistance = -scrollY;
        if (currentDistance + (-dy) < totalDistance) {
            if (mStatus != PULL_STATUS_TOUCH_MOVE) {
                mStatus = PULL_STATUS_TOUCH_MOVE;
                mPullHandler.onPullBegin(this);
            }
            dy = mPullHandler.getConfig().dispatchTouchMove(CoolRefreshView.this, dy, currentDistance, totalDistance);
            scrollerHelper.overScrollByCompat(0, dy, scrollX, scrollY, 0, 0, 0, totalDistance);
        }
    }

    private boolean canChildScrollUp() {
        return mPullHandler.getConfig().contentCanScrollUp(this, getContentView());
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        scrollerHelper.computeScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                childState = ViewCompat.combineMeasuredStates(childState, ViewCompat.getMeasuredState(child));
            }
        }
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        if (DEBUG)
            Log.d("wsx", "onMeasure: " + MeasureSpec.getSize(heightMeasureSpec) + " maxHeight:" + maxHeight + " mHeaderView：" + mHeaderView.getMeasuredHeight());

        setMeasuredDimension(ViewCompat.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                ViewCompat.resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b) {
        View contentView = getContentView();
        //每次layout 之前ViewCompat.offsetTopAndBottom 的值都会被重置，所以滚动的时候如果布局变化会导致调用onMeasure-》onLayout，滚动位置不对了。
        //要加上之前的offset
        int alreadyOffset = scrollerHelper.getAlreadyOffset();

        int headerViewLayoutOffset = mPullHandler.getConfig().headerViewLayoutOffset(this, mHeaderView);
        MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
        int childLeft = getPaddingLeft() + lp.leftMargin;
        int childTop = lp.topMargin - headerViewLayoutOffset - alreadyOffset;
        int measureHeight = mHeaderView.getMeasuredHeight();
        int measuredWidth = mHeaderView.getMeasuredWidth();
        mHeaderView.layout(childLeft, childTop, measuredWidth + childLeft, childTop + measureHeight);

        mTopOffset = -lp.topMargin - measureHeight;

        lp = (MarginLayoutParams) contentView.getLayoutParams();
        childLeft = getPaddingLeft() + lp.leftMargin;
        childTop = getPaddingTop() + lp.topMargin;
        measureHeight = contentView.getMeasuredHeight();
        measuredWidth = contentView.getMeasuredWidth();
        contentView.layout(childLeft, childTop, measuredWidth + childLeft, childTop + measureHeight);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    private int mTopOffset = -1;

    @Override
    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        super.scrollTo(scrollX, scrollY);
    }

    /**
     * {@inheritDoc}
     * This version also clamps the scrolling to the bounds of our child.
     */
    @Override
    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
            y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight());
            if (x != getScrollX() || y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }
    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- mScrollX --|
             */
            return 0;
        }
        if ((my + n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- mScrollX --|
             */
            return child - my;
        }
        return n;
    }

    public View getContentView() {
        if (mContentView == null) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child != mHeaderView) {
                    mContentView = child;
                    break;
                }
            }
        }
        return mContentView;
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    private boolean mRefreshing = false;

    /**
     * header 和 content 一起滚动
     */
    private class AllScroller extends ScrollerHelper {

        @Override
        public int getOffsetX() {
            return getScrollX();
        }

        @Override
        public int getOffsetY() {
            return getScrollY();
        }

        @Override
        public void abortAnimation() {
            mScroller.abortAnimation();
        }

        @Override
        public void computeScroll() {
            if (mScroller.computeScrollOffset()) {
                int cx = mScroller.getCurrY();
                int cy = mScroller.getCurrY();
                int scrollX = getOffsetX();
                int scrollY = getOffsetY();
                overScrollByCompat(cx - scrollX, cy - scrollY, scrollX, scrollY, 0, 0, 0, mHeaderView.getMeasuredHeight());
            }
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            mScroller.startScroll(startX, startY, dx, dy, 800);
            ViewCompat.postInvalidateOnAnimation(CoolRefreshView.this);
        }

        @Override
        void overScrollByCompat(int deltaX, int deltaY,
                                int scrollX, int scrollY,
                                int scrollRangeX, int scrollRangeY,
                                int maxOverScrollX, int maxOverScrollY) {
            int newScrollX = scrollX;
            newScrollX += deltaX;

            int newScrollY = scrollY;
            newScrollY += deltaY;

// Clamp values if at the limits and record
            final int left = -maxOverScrollX;
            final int right = maxOverScrollX + scrollRangeX;
            final int top = -maxOverScrollY;
            final int bottom = maxOverScrollY + scrollRangeY;

            boolean clampedX = false;
            if (newScrollX > right) {
                newScrollX = right;
                clampedX = true;
            } else if (newScrollX < left) {
                newScrollX = left;
                clampedX = true;
            }

            boolean clampedY = false;
            if (newScrollY > bottom) {
                newScrollY = bottom;
                clampedY = true;
            } else if (newScrollY < top) {
                newScrollY = top;
                clampedY = true;
            }

            if (newScrollY > 0) {
                newScrollY = 0;
            }

            deltaY = newScrollY - getOffsetY();
            int currentDistance = -newScrollY;

            if (deltaY != 0) {
                onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);
                mPullHandler.onPositionChange(CoolRefreshView.this, mStatus, deltaY, currentDistance);
            }
            if (!mScroller.isFinished() && mScroller.getFinalY() != mScroller.getCurrY()) {
                ViewCompat.postInvalidateOnAnimation(CoolRefreshView.this);
            }
            if (DEBUG)
                Log.d("zzzz", " bottom:" + mHeaderView.getBottom() + " mHeaderView.getTop:" + mHeaderView.getTop() + " scrollY:" + getScrollY() + " newScrollY:" + newScrollY + " deltaY:" + deltaY + " finalY:" + mScroller.getFinalY());
        }
    }

    /**
     * header滚动，但是Content不滚动
     */
    private class PinContentScroller extends ScrollerHelper {

        @Override
        public int getOffsetX() {
            return mHeaderView.getLeft();
        }

        @Override
        public int getOffsetY() {
            return -mHeaderView.getBottom();
        }

        @Override
        public void abortAnimation() {
            mScroller.abortAnimation();
            removeCallbacks(runnable);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            removeCallbacks(runnable);
            mScroller.startScroll(startX, startY, dx, dy, 800);
            post(runnable);
        }

        private Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mScroller.computeScrollOffset()) {
                    int cx = mScroller.getCurrY();
                    int cy = mScroller.getCurrY();
                    int scrollX = getOffsetX();
                    int scrollY = getOffsetY();
                    overScrollByCompat(cx - scrollX, cy - scrollY, scrollX, scrollY, 0, 0, 0, mHeaderView.getMeasuredHeight());
                    if (!mScroller.isFinished() && mScroller.getFinalY() != mScroller.getCurrY()) {
                        post(this);
                    }
                }
            }
        };

        public int getAlreadyOffset() {
            return -mHeaderView.getBottom();
        }

        @Override
        void overScrollByCompat(int deltaX, int deltaY,
                                int scrollX, int scrollY,
                                int scrollRangeX, int scrollRangeY,
                                int maxOverScrollX, int maxOverScrollY) {
            int newScrollX = scrollX;
            newScrollX += deltaX;

            int newScrollY = scrollY;
            newScrollY += deltaY;

// Clamp values if at the limits and record
            final int left = -maxOverScrollX;
            final int right = maxOverScrollX + scrollRangeX;
            final int top = -maxOverScrollY;
            final int bottom = maxOverScrollY + scrollRangeY;

            if (newScrollX > right) {
                newScrollX = right;
            } else if (newScrollX < left) {
                newScrollX = left;
            }

            if (newScrollY > bottom) {
                newScrollY = bottom;
            } else if (newScrollY < top) {
                newScrollY = top;
            }
            if (newScrollY > 0) {
                newScrollY = 0;
            }

            deltaY = newScrollY - getOffsetY();
            int currentDistance = -newScrollY;

            if (indexOfChild(mHeaderView) != getChildCount() - 1) {
                bringChildToFront(mHeaderView);
                if (DEBUG)
                    Log.d("zzzz", "bringChildToFront:");
            }
            if (deltaY != 0) {
                ViewCompat.offsetTopAndBottom(mHeaderView, -deltaY);
                mPullHandler.onPositionChange(CoolRefreshView.this, mStatus, deltaY, currentDistance);
            }
            if (android.os.Build.VERSION.SDK_INT < 11) {
                ViewCompat.postInvalidateOnAnimation(CoolRefreshView.this);
            }
            if (DEBUG)
                Log.d("zzzz", " bottom:" + mHeaderView.getBottom() + " mHeaderView.getTop:" + mHeaderView.getTop() + " scrollY:" + getScrollY() + " newScrollY:" + newScrollY + " deltaY:" + deltaY + " finalY:" + mScroller.getFinalY());
        }
    }

    private abstract class ScrollerHelper {

        protected final ScrollerCompat mScroller;

        public ScrollerHelper() {
            mScroller = ScrollerCompat.create(getContext(), null);
        }

        public abstract int getOffsetX();

        public abstract int getOffsetY();

        public abstract void abortAnimation();

        public void computeScroll() {
        }

        public int getAlreadyOffset() {
            return 0;
        }

        public abstract void startScroll(int startX, int startY, int dx, int dy);

        abstract void overScrollByCompat(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY);

        public boolean isFinished() {
            return mScroller.isFinished();
        }
    }
}
