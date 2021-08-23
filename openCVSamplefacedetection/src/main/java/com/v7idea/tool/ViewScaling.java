package com.v7idea.tool;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by mortal on 2017/1/2.
 */
public class ViewScaling
{
    private static final String TAG = "ViewScaling";

    private static final int defaultWidth = 1280;
    private static final int defaultHeight = 720;

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    private static float scaleMin = 0.0f;

    private static float scaleValue = 0.0f;

    public void setScaleTextSize(TextView view)
    {
        setScaleTextSize(view, scaleMin);
    }

    public static View findViewByIdAndScale(Activity activity, int intViewID)
    {
        View view = activity.findViewById(intViewID);

        scaleLayout(view);

        return view;
    }

    public static View findViewByIdAndScale(View parentView, int intViewID)
    {
        View view = parentView.findViewById(intViewID);

        scaleLayout(view);

        return view;
    }

    public static float getScaleMin()
    {
        return scaleMin;
    }

    public static float getScaleValue()
    {
        return scaleValue;
    }

    public static int getScreenWidth()
    {
        return screenWidth;
    }

    public static int getScreenHeight()
    {
        return screenHeight;
    }

    public static void scaleLayout(View view)
    {
        if(view != null)
        {
            if(view.getParent() != null)
            {
                if(view.getParent() instanceof RelativeLayout)
                {
                    setScaleByRelativeLayout(view);
                }
                else if(view.getParent() instanceof LinearLayout)
                {
                    setScaleByLinearLayout(view);
                }
                else if(view.getParent() instanceof FrameLayout)
                {
                    setScaleByFrameLayout(view);
                }
                else if(view.getParent() instanceof Toolbar)
                {
                    setScaleByToolBar(view);
                }
                else if(view.getParent() instanceof AbsListView)
                {
                    setScaleByAbsListView(view);
                }
//                else if(view.getParent() instanceof RecyclerView)
//                {
//                    setScaleByRecyclerView(view);
//                }
                else if(view.getParent() instanceof TableRow)
                {
                    setScaleByTableRow(view);
                }
            }

            setPadding(view);

            view.setMinimumHeight((int)(view.getMinimumHeight() * scaleValue));
            view.setMinimumWidth((int) (view.getMinimumWidth() * scaleValue));

            if(view instanceof TextView)
            {
                setScaleTextSize(((TextView)view), scaleMin);
            }
            else if(view instanceof EditText)
            {
                setScaleTextSize(((EditText)view), scaleMin);
            }
        }
    }

    /**
     * 這個方法是 screenHeight, screenWidth, scalueValue一次處理好
     */
    public static void setScaleValue(Activity ActivityContext)
    {
        //取得螢幕的寛高
        DisplayMetrics dm = new DisplayMetrics();
        ActivityContext.getWindowManager().getDefaultDisplay().getMetrics(dm);

        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        float scaleXValue = (float) screenWidth / (float) defaultWidth;
        float scaleYValue = (float) screenHeight / (float) defaultHeight;

        if (defaultWidth < defaultHeight) {
            scaleValue = scaleXValue;
        } else {
            scaleValue = scaleYValue;
        }

        if (scaleXValue > scaleYValue) {

            scaleMin = scaleYValue;

        } else {

            scaleMin = scaleXValue;
        }

        DebugLog.d(TAG, "scaleValue: "+scaleValue);
        DebugLog.d(TAG, "scaleMin: "+scaleMin);
    }

    public static void setPadding(View view) {
        if (view != null) {
            int paddingLeft = (int) (view.getPaddingLeft() * scaleValue);
            int paddingRight = (int) (view.getPaddingRight() * scaleValue);
            int paddingTop = (int) (view.getPaddingTop() * scaleValue);
            int paddingBottom = (int) (view.getPaddingBottom() * scaleValue);

            view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }
    }

    /**
     * 取得view的 RelativeLayout.LayoutParams
     *
     * @param view
     * @param width
     * @param height
     * @return
     */
    public RelativeLayout.LayoutParams getRelativeLayoutLayoutParams(View view, int width, int height) {
        RelativeLayout.LayoutParams viewUse = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new RelativeLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        return viewUse;
    }

//    public void setScaleByCoordinatorLayout(View view) {
//        CoordinatorLayout.LayoutParams viewUse = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
//
//        if (viewUse != null) {
//            if (viewUse.width != CoordinatorLayout.LayoutParams.MATCH_PARENT && viewUse.width != CoordinatorLayout.LayoutParams.WRAP_CONTENT) {
//                viewUse.width = (int) (viewUse.width * scaleValue);
//
//                if(viewUse.width <= 0)
//                {
//                    viewUse.width = 1;
//                }
//            }
//
//            if (viewUse.height != CoordinatorLayout.LayoutParams.MATCH_PARENT && viewUse.height != CoordinatorLayout.LayoutParams.WRAP_CONTENT) {
//                viewUse.height = (int) (viewUse.height * scaleValue);
//
//                if(viewUse.height <= 0)
//                {
//                    viewUse.height = 1;
//                }
//            }
//
//            viewUse.leftMargin = (int) (viewUse.leftMargin * scaleValue);
//            viewUse.topMargin = (int) (viewUse.topMargin * scaleValue);
//            viewUse.rightMargin = (int) (viewUse.rightMargin * scaleValue);
//            viewUse.bottomMargin = (int) (viewUse.bottomMargin * scaleValue);
//
//            view.setLayoutParams(viewUse);
//        }
//    }

    public static void setScaleByRelativeLayout(View view) {
        RelativeLayout.LayoutParams viewUse = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (viewUse != null) {
            if (viewUse.width != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.width = (int) (viewUse.width * scaleValue);

                if(viewUse.width <= 0)
                {
                    viewUse.width = 1;
                }
            }

            if (viewUse.height != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.height = (int) (viewUse.height * scaleValue);

                if(viewUse.height <= 0)
                {
                    viewUse.height = 1;
                }
            }

            viewUse.leftMargin = (int) (viewUse.leftMargin * scaleValue);
            viewUse.topMargin = (int) (viewUse.topMargin * scaleValue);
            viewUse.rightMargin = (int) (viewUse.rightMargin * scaleValue);
            viewUse.bottomMargin = (int) (viewUse.bottomMargin * scaleValue);

            view.setLayoutParams(viewUse);
        }
    }

    public static void setScaleByFrameLayout(View view) {
        FrameLayout.LayoutParams viewUse = (FrameLayout.LayoutParams) view.getLayoutParams();

        if (viewUse != null) {
            if (viewUse.width != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.width = (int) (viewUse.width * scaleValue);

                if(viewUse.width <= 0)
                {
                    viewUse.width = 1;
                }
            }

            if (viewUse.height != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.height = (int) (viewUse.height * scaleValue);

                if(viewUse.height <= 0)
                {
                    viewUse.height = 1;
                }
            }

            viewUse.leftMargin = (int) (viewUse.leftMargin * scaleValue);
            viewUse.topMargin = (int) (viewUse.topMargin * scaleValue);
            viewUse.rightMargin = (int) (viewUse.rightMargin * scaleValue);
            viewUse.bottomMargin = (int) (viewUse.bottomMargin * scaleValue);

            view.setLayoutParams(viewUse);
        }
    }

//    public void setScaleByRecyclerView(View view)
//    {
//        RecyclerView.LayoutParams viewUse = (RecyclerView.LayoutParams) view.getLayoutParams();
//
//        if (viewUse != null) {
//            if (viewUse.width != RecyclerView.LayoutParams.MATCH_PARENT && viewUse.width != RecyclerView.LayoutParams.WRAP_CONTENT) {
//                viewUse.width = (int) (viewUse.width * scaleValue);
//
//                if(viewUse.width <= 0)
//                {
//                    viewUse.width = 1;
//                }
//            }
//
//            if (viewUse.height != RecyclerView.LayoutParams.MATCH_PARENT && viewUse.height != RecyclerView.LayoutParams.WRAP_CONTENT) {
//                viewUse.height = (int) (viewUse.height * scaleValue);
//
//                if(viewUse.height <= 0)
//                {
//                    viewUse.height = 1;
//                }
//            }
//
//            viewUse.leftMargin = (int) (viewUse.leftMargin * scaleValue);
//            viewUse.topMargin = (int) (viewUse.topMargin * scaleValue);
//            viewUse.rightMargin = (int) (viewUse.rightMargin * scaleValue);
//            viewUse.bottomMargin = (int) (viewUse.bottomMargin * scaleValue);
//
//            view.setLayoutParams(viewUse);
//        }
//    }

    public static void setScaleByAbsListView(View view)
    {
        AbsListView.LayoutParams viewUse = (AbsListView.LayoutParams) view.getLayoutParams();

        if (viewUse != null) {
            if (viewUse.width != AbsListView.LayoutParams.MATCH_PARENT && viewUse.width != AbsListView.LayoutParams.WRAP_CONTENT) {
                viewUse.width = (int) (viewUse.width * scaleValue);

                if(viewUse.width <= 0)
                {
                    viewUse.width = 1;
                }
            }

            if (viewUse.height != AbsListView.LayoutParams.MATCH_PARENT && viewUse.height != AbsListView.LayoutParams.WRAP_CONTENT) {
                viewUse.height = (int) (viewUse.height * scaleValue);

                if(viewUse.height <= 0)
                {
                    viewUse.height = 1;
                }
            }

            view.setLayoutParams(viewUse);
        }
    }

    public static void setScaleByToolBar(View view)
    {
        Toolbar.LayoutParams viewUse = (Toolbar.LayoutParams) view.getLayoutParams();

        if (viewUse != null) {
            if (viewUse.width != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.width = (int) (viewUse.width * scaleValue);

                if(viewUse.width <= 0)
                {
                    viewUse.width = 1;
                }
            }

            if (viewUse.height != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.height = (int) (viewUse.height * scaleValue);

                if(viewUse.height <= 0)
                {
                    viewUse.height = 1;
                }
            }

            viewUse.leftMargin = (int) (viewUse.leftMargin * scaleValue);
            viewUse.topMargin = (int) (viewUse.topMargin * scaleValue);
            viewUse.rightMargin = (int) (viewUse.rightMargin * scaleValue);
            viewUse.bottomMargin = (int) (viewUse.bottomMargin * scaleValue);

            view.setLayoutParams(viewUse);
        }
    }

    public static void setScaleByLinearLayout(View view) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse != null) {
            if (viewUse.width != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.width = (int) (viewUse.width * scaleValue);

                if(viewUse.width <= 0)
                {
                    viewUse.width = 1;
                }
            }

            if (viewUse.height != ViewGroup.LayoutParams.MATCH_PARENT && viewUse.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewUse.height = (int) (viewUse.height * scaleValue);

                if(viewUse.height <= 0)
                {
                    viewUse.height = 1;
                }
            }

            viewUse.leftMargin = (int) (viewUse.leftMargin * scaleValue);
            viewUse.topMargin = (int) (viewUse.topMargin * scaleValue);
            viewUse.rightMargin = (int) (viewUse.rightMargin * scaleValue);
            viewUse.bottomMargin = (int) (viewUse.bottomMargin * scaleValue);

            view.setLayoutParams(viewUse);
        }
    }

    /**
     * 縮放在RelativeLayout內的View，只調整 leftMargin，bottomMargin
     *
     * @param view
     * @param width
     * @param height
     * @param left
     * @param bottom
     */
    public void setScaleByLeftAndBottomForRelativeLayout(View view, int width, int height, int left, int bottom) {
        RelativeLayout.LayoutParams viewUse = getRelativeLayoutLayoutParams(view, width, height);
        viewUse.leftMargin = left;
        viewUse.bottomMargin = bottom;

        view.setLayoutParams(viewUse);
    }

    /**
     * 縮放在RelativeLayout內的View，只調整 rightMargin，bottomMargin
     *
     * @param view
     * @param width
     * @param height
     * @param right
     * @param bottom
     */
    public void setScaleByRightAndBottomForRelativeLayout(View view, int width, int height, int right, int bottom) {
        RelativeLayout.LayoutParams viewUse = getRelativeLayoutLayoutParams(view, width, height);
        viewUse.rightMargin = right;
        viewUse.bottomMargin = bottom;

        view.setLayoutParams(viewUse);
    }

    /**
     * 縮放在RelativeLayout內的View，只調整所有的margin
     *
     * @param view
     * @param width
     * @param height
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setScaleByAllForRelativeLayout(View view, int width, int height, int left, int right, int top, int bottom) {
        RelativeLayout.LayoutParams viewUse = getRelativeLayoutLayoutParams(view, width, height);
        viewUse.leftMargin = left;
        viewUse.rightMargin = right;
        viewUse.topMargin = top;
        viewUse.bottomMargin = bottom;

        view.setLayoutParams(viewUse);
    }



    public static void setScale(View view, int width, int height) {
        RelativeLayout.LayoutParams viewUse = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new RelativeLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        view.setLayoutParams(viewUse);
    }

    /**
     * 針對PatentView是LinearLayout的設定縮放Method
     *
     * @param view   要縮放的View
     * @param width  寬度
     * @param height 高度
     */
    public void setScaleForLinearLayout(View view, int width, int height) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LinearLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        view.setLayoutParams(viewUse);
    }

    public void setScaleForLinearLayoutByAll(View view, int width, int height, int left, int right, int top, int bottom) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LinearLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.leftMargin = left;
        viewUse.rightMargin = right;
        viewUse.topMargin = top;
        viewUse.bottomMargin = bottom;

        view.setLayoutParams(viewUse);
    }

    public void setScaleForLinearLayoutByTopAndBottom(View view, int width, int height, int top, int bottom) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LinearLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.topMargin = top;
        viewUse.bottomMargin = bottom;

        view.setLayoutParams(viewUse);
    }

    public void setScaleForLinearLayout(View view, int width, int height, int top, int left) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LinearLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.leftMargin = left;
        viewUse.topMargin = top;

        view.setLayoutParams(viewUse);
    }

    public void setScaleForLinearLayoutByRight(View view, int width, int height, int top, int right) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LinearLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.rightMargin = right;
        viewUse.topMargin = top;

        view.setLayoutParams(viewUse);
    }

    public void setScaleForLinearLayoutByLeftAndBottom(View view, int width, int height, int left, int bottom) {
        LinearLayout.LayoutParams viewUse = (LinearLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LinearLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.leftMargin = left;
        viewUse.bottomMargin = bottom;

        view.setLayoutParams(viewUse);
    }

    public static void changeLeftMarginAndTopMargin(View view, int left, int top)
    {
        RelativeLayout.LayoutParams viewUse = (RelativeLayout.LayoutParams) view.getLayoutParams();

        viewUse.topMargin = top;
        viewUse.leftMargin = left;
        view.setLayoutParams(viewUse);
    }

    public void setScale(View view, int width, int height, int top, int left) {
        RelativeLayout.LayoutParams viewUse = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new RelativeLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.topMargin = top;
        viewUse.leftMargin = left;
        view.setLayoutParams(viewUse);
    }

    public void setScaleTopAndRight(View view, int width, int height, int top, int right) {
        RelativeLayout.LayoutParams viewUse = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new RelativeLayout.LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.topMargin = top;
        viewUse.rightMargin = right;
        view.setLayoutParams(viewUse);
    }

    public void setLayoutScaleByFrameLayout(View view, float scaleValue) {
        FrameLayout.LayoutParams viewParams = (FrameLayout.LayoutParams) view.getLayoutParams();

        if (viewParams != null) {
            viewParams.width = (int) (viewParams.width * scaleValue);
            viewParams.height = (int) (viewParams.height * scaleValue);
            viewParams.leftMargin = (int) (viewParams.leftMargin * scaleValue);
            viewParams.topMargin = (int) (viewParams.topMargin * scaleValue);

            view.setLayoutParams(viewParams);
        }
    }

    public void setLayoutScaleByFrameLayout(View view, int width, int height, int top, int left) {
        FrameLayout.LayoutParams viewParams = (FrameLayout.LayoutParams) view.getLayoutParams();

        if (viewParams != null) {
            viewParams.width = width;
            viewParams.height = height;

        } else {
            viewParams = new FrameLayout.LayoutParams(width, height);
        }

        viewParams.leftMargin = left;
        viewParams.topMargin = top;

        view.setLayoutParams(viewParams);
    }

    public boolean setLayoutScale(View view, float scaleValue) {
        boolean itSuccess = false;

        RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (viewParams != null) {
            viewParams.width = (int) (viewParams.width * scaleValue);
            viewParams.height = (int) (viewParams.height * scaleValue);
            viewParams.leftMargin = (int) (viewParams.leftMargin * scaleValue);
            viewParams.topMargin = (int) (viewParams.topMargin * scaleValue);

            view.setLayoutParams(viewParams);

            itSuccess = true;
        }

        return itSuccess;
    }

    /**
     * 對TableRow內的View調整大小
     *
     * @param view
     */
    public static void setScaleByTableRow(View view) {
        TableRow.LayoutParams viewParams = (TableRow.LayoutParams) view.getLayoutParams();

        if (viewParams != null) {
            if (viewParams.width != ViewGroup.LayoutParams.MATCH_PARENT && viewParams.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewParams.width = (int) (viewParams.width * scaleValue);

                if(viewParams.width <= 0)
                {
                    viewParams.width = 1;
                }
            }

            if (viewParams.height != ViewGroup.LayoutParams.MATCH_PARENT && viewParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                viewParams.height = (int) (viewParams.height * scaleValue);

                if(viewParams.height <= 0)
                {
                    viewParams.height = 1;
                }
            }

            viewParams.leftMargin = (int) (viewParams.leftMargin * scaleValue);
            viewParams.topMargin = (int) (viewParams.topMargin * scaleValue);
            viewParams.rightMargin = (int) (viewParams.rightMargin * scaleValue);
            viewParams.bottomMargin = (int) (viewParams.bottomMargin * scaleValue);
        }

        view.setLayoutParams(viewParams);
    }

    /**
     * 將TextView字體放大(用這個方法在XML一定要先設好字體大小)
     *
     * @param view     TextView
     * @param scaleMin 縮放倍數
     */
    public static void setScaleTextSize(TextView view, float scaleMin) {
        if (view != null) {
            setScaleTextSize(view, view.getTextSize(), scaleMin);
        }
    }

    /**
     * 將TextView字體放大
     *
     * @param view       TextView
     * @param textSize   要設定的字體大小（PX）
     * @param scaleValue 縮放倍數
     */
    public static void setScaleTextSize(TextView view, float textSize, float scaleValue) {
        if (view != null) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (textSize * scaleValue));
        }
    }

    /**
     * Covert dp to px
     * @param dp
     * @param context
     * @return pixel
     */
    public static float convertDpToPixel(float dp, Context context){
        float px = dp * getDensity(context);
        return px;
    }
    /**
     * Covert px to dp
     * @param px
     * @param context
     * @return dp
     */
    public static float convertPixelToDp(float px, Context context){
        float dp = px / getDensity(context);
        return dp;
    }
    /**
     * 取得螢幕密度
     * 120dpi = 0.75
     * 160dpi = 1 (default)
     * 240dpi = 1.5
     * @param context
     * @return
     */
    public static float getDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }
}
