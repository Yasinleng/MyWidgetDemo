package com.yclenga.utils.mywidgetdemo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

import com.yclenga.utils.mywidgetdemo.R;

/**
 * 作者：Created by yclenga on 2017/8/3.
 * 邮箱:  yclenga@isoftstone.com
 */

public class SlidingMenu extends HorizontalScrollView {

    private static final String TAG = "SlidingMenu";

    private static final int LEFT=0;
    private static final int RIGHT=1;

    private int menuType;
    private int mMenuWidth;
    private int mScreenWidth;
    private View contentView;
    private View menuView;

    //是否拦截
    private boolean mIsIntercept = false;

    //菜单是否打开
    private boolean mMenuIsOpen = false;

    //手势滑动处理
    private GestureDetector mGestureDetector;

    private GestureDetector.OnGestureListener mOnGestureListener=new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            switch (menuType){
                case LEFT:
                    if (mMenuIsOpen) {
                        //打开往右边快速滑动就去切换(关闭)
                        if (velocityX < 0) {
                            closeMenu();
                            return true;
                        }
                    } else {
                        //关闭的时候往左边快速滑动切换(打开)
                        if (velocityX > 0) {
                            openMenu();
                            return true;
                        }
                    }
                    break;
                case RIGHT:
                    if (mMenuIsOpen) {
                        //打开往左边快速滑动就去切换(打开)
                        if (velocityX < 0) {
                            openMenu();
                            return true;
                        }
                    } else {
                        //关闭的时候往右边快速滑动切换(关闭)
                        if (velocityX > 0) {
                            closeMenu();
                            return true;
                        }
                    }
                    break;
            }

            return super.onFling(e1, e2, velocityX, velocityY);

        }
    };


    public SlidingMenu(Context context) {
        this(context,null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth=getScreenWidth(context);
        TypedArray mTypedArray=context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);
        if (mTypedArray != null){
            menuType=mTypedArray.getInt(R.styleable.SlidingMenu_menutype,LEFT);
            mMenuWidth= (int) mTypedArray.getDimension(R.styleable.SlidingMenu_menuWidth,dp2px(50));
            mTypedArray.recycle();
        }
        mGestureDetector=new GestureDetector(context,mOnGestureListener);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //布局解析完毕会回调  也就是xml文件布局解析完毕  在onCreate方法中调用
        //指定宽高
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        if (childCount != 2) {
            throw new RuntimeException("只能放置两个子view!");
        }


        switch (menuType){
            case LEFT:
                //菜单页的宽度是屏幕的宽度 - 右边的一小部分距离(自定义属性)
                menuView = container.getChildAt(0);
                //内容页的宽度是屏幕的宽度
                contentView = container.getChildAt(1);
                break;
            case RIGHT:
                //内容页的宽度是屏幕的宽度
                contentView = container.getChildAt(0);
                //菜单页的宽度是屏幕的宽度 - 右边的一小部分距离(自定义属性)
                menuView = container.getChildAt(1);
                break;
        }


        ViewGroup.LayoutParams menuParams = menuView.getLayoutParams();
        menuParams.width = mMenuWidth;
        menuView.setLayoutParams(menuParams);

        ViewGroup.LayoutParams contentParams = contentView.getLayoutParams();
        contentParams.width = getScreenWidth(getContext());
        contentView.setLayoutParams(contentParams);

    }

    //处理内容的缩放，菜单的缩放和透明度，需要不断的获取当前滚动的位置
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        //缩放的中心点位置
        int pivotX=0;
        int pivotY=contentView.getMeasuredHeight() / 2;


        if (menuType==RIGHT) {
            l = mMenuWidth - l;
            pivotX=contentView.getWidth();
        }

        //设置缩放的中心点位置
        ViewCompat.setPivotX(contentView, pivotX);
        ViewCompat.setPivotY(contentView, pivotY);

//      计算一个梯度值  1f - 0f
        float scale = 1f * l / mMenuWidth;
        //右边的缩放最小  0.7f  最大是1f  默认是以中心点缩放
        float contentScale = 0.7f + 0.3f * scale;

        //设置右边的缩放
        ViewCompat.setScaleX(contentView, contentScale);
        ViewCompat.setScaleY(contentView, contentScale);

        //设置菜单缩放和透明度
        //透明度是由半透明到全部透明  0.7 - 1.0f
        float menuAlpha = 0.5f + (1 - scale) * 0.5f;
        ViewCompat.setAlpha(menuView, menuAlpha);

        //缩放  0.7f - 1.0f
        float menuScale = 0.7f + (1 - scale) * 0.3f;
        ViewCompat.setScaleX(menuView, menuScale);
        ViewCompat.setScaleY(menuView, menuScale);
        //设置文字平移
        if (menuType==RIGHT){
            ViewCompat.setTranslationX(menuView, -l * 0.15f);
        }else {
            ViewCompat.setTranslationX(menuView, l * 0.15f);
        }


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //初始化进入的时候是关闭的，移动一段距离就可以了  在onResume之后调用
        if (menuType==LEFT){
            scrollTo(mMenuWidth, 0);
        }else {
            scrollTo(0, 0);
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsIntercept = false;
        //当菜单打开的时候，手指触摸右边内容部分需要关闭菜单，还需要拦截事件，
        if (mMenuIsOpen) {
            float currentX = ev.getX();
            if (currentX > mMenuWidth) {
                //关闭菜单
                closeMenu();
                //子view不需要相应任何的事件  拦截子view的事件
                mIsIntercept = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果有拦截就不执行自己的onTouchEvent
        if (mIsIntercept) {
            return true;
        }
        //快速滑动触发了下面的就不要执行了
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            //  只需要处理手指抬起  根据当前滚动的距离来判断

            int currentScrollX =getScrollX();

            if (menuType==RIGHT){
                currentScrollX=mMenuWidth-getScrollX();
            }
            if (currentScrollX > mMenuWidth / 2) {
                closeMenu();
            } else {
                openMenu();
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }



    /**
     * 关闭菜单
     */
    private void closeMenu() {
        if (menuType==LEFT) {
            smoothScrollTo(mMenuWidth, 0);
        }else {
            smoothScrollTo(0, 0);
        }
        mMenuIsOpen = false;
    }

    /**
     * 打开菜单
     */
    private void openMenu() {
        if (menuType==LEFT) {
            smoothScrollTo(0, 0);
        }else {
            smoothScrollTo(mScreenWidth, 0);
        }
        mMenuIsOpen = true;
    }


    /**
     * dp转成px
     *
     * @param dp
     * @return
     */
    private float dp2px(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }


    /**
     * 获取屏幕的宽度
     */
    private int getScreenWidth(Context context) {
        WindowManager systemService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }


}
