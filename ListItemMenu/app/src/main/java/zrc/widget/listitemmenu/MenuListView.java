package zrc.widget.listitemmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Created by Zaric on 2015/1/29.
 */
public class MenuListView extends ListView implements ItemMenuView.ISlideFocusManager{
    private static final int INVALID = -1, NORMAL = 0, FLING = 1;
    private int mTouchMode = 0;
    private float mMotionX;
    private float mMotionY;
    private int mTouchSlop = 0;

    private ItemMenuView mCurrentView;

    public MenuListView(Context context) {
        super(context);
        init();
    }

    public MenuListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mTouchSlop =
                (int) (ViewConfiguration.getTouchSlop() * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            mMotionX = ev.getX();
            mMotionY = ev.getY();
            mTouchMode = NORMAL;
            if(mCurrentView!=null) {
                if (mMotionY < mCurrentView.getTop() || mMotionY > mCurrentView.getBottom()){
                    mCurrentView.hideMenu();
                    mCurrentView = null;
                }
            }
            return super.onInterceptTouchEvent(ev);
        }
        if(mTouchMode == INVALID){
            return false;
        } else if(mTouchMode == FLING){
            return super.onInterceptTouchEvent(ev);
        }
        if(ev.getAction()==MotionEvent.ACTION_MOVE){
            float diffX = Math.abs(ev.getX() - mMotionX);
            float diffY = Math.abs(ev.getY() - mMotionY);
            if(diffX > mTouchSlop || diffY > mTouchSlop){
                if(diffX > diffY){
                    mTouchMode = INVALID;
                    return false;
                } else {
                    mTouchMode = FLING;
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onFocus(ItemMenuView view) {
        if(mCurrentView!=null && mCurrentView!=view){
            mCurrentView.hideMenu();
        }
        mCurrentView = view;
    }
}
