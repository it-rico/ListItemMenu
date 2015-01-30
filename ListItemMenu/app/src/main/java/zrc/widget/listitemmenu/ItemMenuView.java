package zrc.widget.listitemmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by Zaric on 2015/1/29.
 */
public class ItemMenuView extends ViewGroup {
    private static final int INVALID = -1, NORMAL = 0, TAP = 1, FLING = 2;
    private View mContent;

    private boolean isOpen = false;
    private int mMove;
    private float mMotionX;
    private float mMotionY;
    private int mTouchMode = NORMAL;
    private FlingHelper mFlingHelper;

    private float mDensity;
    private int mTouchSlop;
    private int mMaxOut;

    private int mMenuWidth;

    private ISlideFocusManager mFocusManager;

    public ItemMenuView(Context context) {
        super(context);
        init(null, 0);
    }

    public ItemMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ItemMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mFlingHelper = new FlingHelper();
        mDensity = getResources().getDisplayMetrics().density;
        mTouchSlop = (int) (ViewConfiguration.getTouchSlop() * mDensity);
        mMaxOut = (int) (50 * mDensity);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;
        int childCount = getChildCount();
        if(childCount > 0){
            mContent = getChildAt(childCount-1);
        }
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, child.getMeasuredWidth());
            height = Math.max(height, child.getMeasuredHeight());
        }
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = r - l;
        int childCount = getChildCount();
        mMenuWidth = 0;
        for(int i=0; i<childCount; i++) {
            View child = getChildAt(i);
            int childTop = getPaddingTop();
            if(i == childCount-1){
                int childLeft = getPaddingLeft();
                child.layout(childLeft, childTop, childLeft+child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            } else {
                int childWidth = child.getMeasuredWidth();
                mMenuWidth += childWidth;
                int childLeft = w - getPaddingRight() - mMenuWidth;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + child.getMeasuredHeight());
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(mFocusManager!=null){
                    mFocusManager.onFocus(this);
                }
                float ex = ev.getX();
                if(ex > getWidth() + mMove){
                    mTouchMode = INVALID;
                    break;
                } else {
                    mTouchMode = isOpen ? TAP : NORMAL;
                }
                if(!mFlingHelper.isFinished()){
                    mTouchMode = FLING;
                    break;
                }
                mFlingHelper.abort();
                mMotionX = ex;
                mMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchMode == NORMAL){
                    float diffX = Math.abs(mMotionX - ev.getX());
                    float diffY = Math.abs(mMotionY - ev.getY());
                    if(diffX > diffY && diffX >= mTouchSlop){
                        mTouchMode = FLING;
                    }
                }
                break;
        }
        return mTouchMode > NORMAL;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mTouchMode == INVALID){
            return false;
        }
        float ex = event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(ex > getWidth() + mMove){
                    mTouchMode = INVALID;
                    return false;
                }
                mFlingHelper.abort();
                mMotionX = ex;
                mMotionY = event.getY();
                mTouchMode = TAP;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchMode != FLING){
                    float diffX = Math.abs(mMotionX - ex);
                    float diffY = Math.abs(mMotionY - event.getY());
                    if(diffX > diffY && diffX >= mTouchSlop){
                        mTouchMode = FLING;
                    }
                } else {
                    moveWithModerate((int) (ex - mMotionX));
                    mMotionX = ex;
                    mMotionY = event.getY();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if(mTouchMode == TAP){
                    hideMenu();
                } else if(mMove < -mMenuWidth/2){
                    showMenu();
                } else{
                    hideMenu();
                }
                break;
        }
        return true;
    }

    public void setFocusManager(ISlideFocusManager manager){
        mFocusManager = manager;
    }

    public void showMenu(){
        isOpen = true;
        mFlingHelper.scrollTo(-mMenuWidth);
    }

    public void hideMenu(){
        isOpen = false;
        mFlingHelper.scrollTo(0);
    }

    private void moveWithModerate(int dx){
        int t = mMove + dx;
        int max = mMaxOut;
        if(t < -mMenuWidth && dx < 0){
            dx = Math.max(dx*(mMenuWidth + max + mMove) / max / 2, -mMenuWidth - max - mMove);
        } else if(t>0 && dx > 0){
            dx = Math.min(dx*2*(max - mMove) / max / 6, max / 3 - mMove);
        }
        moveBy(dx);
    }

    private void moveBy(int dx){
        mMove += dx;
        mContent.offsetLeftAndRight(dx);
    }

    private void moveTo(int x){
        moveBy(x - mMove);
    }

    private class FlingHelper implements Runnable{
        private Scroller mScroller;

        private FlingHelper(){
            mScroller = new Scroller(getContext(), new Interpolator() {
                @Override
                public float getInterpolation(float t) {
                    t-=1;
                    return t*t*t*t*t+1;
                }
            });
        }

        private void scrollTo(int x){
            abort();
            int dx = x - mMove;
            int diffX = Math.abs(dx);
            mScroller.startScroll(mMove, 0, dx, 0, Math.min((int) (diffX * 4 / mDensity), 200));
            postOnAnimation(this);
        }

        private boolean isFinished(){
            return mScroller.isFinished();
        }

        private void abort(){
            if(!mScroller.isFinished()){
                mScroller.abortAnimation();
            }
        }

        @Override
        public void run() {
            if(mScroller.computeScrollOffset()){
                int x = mScroller.getCurrX();
                if(!mScroller.isFinished()) {
                    moveTo(x);
                    postOnAnimation(this);
                } else {
                    moveTo(mScroller.getFinalX());
                    abort();
                }
            }
        }
    }

    public static interface ISlideFocusManager{
        void onFocus(ItemMenuView view);
    }
}
