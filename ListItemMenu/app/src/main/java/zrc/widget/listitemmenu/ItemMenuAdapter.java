package zrc.widget.listitemmenu;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;

/**
 * Created by Zaric on 2015/1/29.
 */
public abstract class ItemMenuAdapter extends BaseAdapter {
    private static final int ANIMATION_DURATION = 300;

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        ItemMenuView view = getItemView(position, (ItemMenuView) convertView, parent);
        view.setFocusManager((ItemMenuView.ISlideFocusManager) parent);
        view.hideMenu();
        return view;
    }

    public void deleteItem(final int pos, final ItemMenuView view){
        final int initialHeight = view.getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                }
                else {
                    view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onDeleteItem(pos);
                view.clearAnimation();
                view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        anim.setDuration(ANIMATION_DURATION);
        view.startAnimation(anim);
    }

    public void onDeleteItem(int pos){

    }

    public abstract ItemMenuView getItemView(int position, ItemMenuView convertView, ViewGroup parent);
}
