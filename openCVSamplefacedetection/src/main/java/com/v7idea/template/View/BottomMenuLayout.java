package com.v7idea.template.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.v7idea.tool.Constants;
import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;


/**
 * 用於近期活動下方的Menu
 *
 * @author jackyYang
 */
public class BottomMenuLayout extends RelativeLayout {
    private final String tag = "BottomMenuLayout";

    private BottomMenuLayout thisLayout = null;

    private RelativeLayout bottomMenuLayout = null;

    private LinearLayout bottomContent = null;

    private BottomMenuIconLayout[] bottonIcon = null;

    //常數
    private final int defaultHeight = 99;                //LinearLayout的高
    private float scaleValue = 0.0f;

    private final int changeIcon = 1000;

    private int pageIndex = 0;

    public final int CHANGE_TITLE = 2000;

    /**
     * 把layout下捲的元件
     */
    private Scroller mScroller = null;

    /**
     * layout是否己下捲
     */
    public boolean isMoveDown = false;

    /**
     * 按鍵是否己被按下
     */
    public boolean isIconClick = false;

    private OnPressCallBack onPressCallBack = null;

    public BottomMenuLayout(Context context) {
        super(context);
    }

    public BottomMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setting() {
        thisLayout = this;

        int iconSize = Constants.BottomMenu.BUTTON_COUNT;

        bottonIcon = new BottomMenuIconLayout[iconSize];

        defaultSetting();

        int itemWidth = ViewScaling.getScreenWidth() / iconSize;

        for (int i = 0; i < iconSize; i++) {
            bottonIcon[i] = new BottomMenuIconLayout(getContext(), itemWidth);
            bottonIcon[i].setIconId(changeIcon + i);
            bottonIcon[i].setIconClickListener(onIconClick);
            bottomContent.addView(bottonIcon[i]);
        }

        Log.e(tag, tag + "  setting !!");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        Log.e(tag, tag + "  onAttachedToWindow !!");
    }

    private AfterUpDateNoticeCountEventListener UpDateNoticeCountEventListener = null;

    public void setUpDateNoticeCountEventListener(AfterUpDateNoticeCountEventListener UpDateNoticeCountEventListener)
    {
        this.UpDateNoticeCountEventListener = UpDateNoticeCountEventListener;
    }

    public void setOnPressCallBack(OnPressCallBack onPressCallBack)
    {
        this.onPressCallBack = onPressCallBack;
    }

    public interface AfterUpDateNoticeCountEventListener
    {
        public void afterUpDateNoticeCountEventListener(int count);
    }

    private void setNoticeCount(int intCount)
    {
        if(bottomContent != null && bottomContent.getChildCount() > 0)
        {
            for (int i = 0 ; i < bottomContent.getChildCount() ; i++) {
                View childView = bottomContent.getChildAt(i);

                if(childView != null && childView instanceof BottomMenuIconLayout)
                {
                    int intViewId = ((BottomMenuIconLayout)childView).getIconId();

                    if(intViewId == 1002)
                    {
                        ((BottomMenuIconLayout)childView).setNoticeCount(intCount);
                        break;
                    }
                }
            }
        }
    }

    private void defaultSetting() {
        scaleValue = ViewScaling.getScaleValue();

        bottomMenuLayout = (RelativeLayout) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.bottommenulayout, null);
        bottomContent = (LinearLayout) bottomMenuLayout.findViewById(R.id.bottommenulaout_bottomContent);

        //設定該物件的大小位置
        setScale(bottomContent, ViewScaling.getScreenWidth(), (int) (defaultHeight * scaleValue), 0, 0);
        addView(bottomMenuLayout);

        mScroller = new Scroller(getContext());
        bottomContent.setOnClickListener(emptyClick);//按鍵列的容器若被按到擋住不動作
    }

    /**
     * layout下卷的功能
     */
    private OnClickListener onMoveAnimationClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            scrollBottomMenuDown();
        }
    };

    /**
     * layout上卷的功能
     */
    private OnClickListener onMoveAnimationUpClick = new OnClickListener() {
        @Override
        public void onClick(View v) {

            scrollBottomMenuUp();
        }
    };

    /**
     * 上下捲動的方法
     *
     * @param startY 起始的Y軸
     * @param endY   結束的Y軸
     */
    private void scrollBottomContent(int startY, int endY) {
        mScroller.startScroll(0, startY, 0, endY, 1000);
        invalidate();
    }

    private int offSet = 0;

    public void setOffset(int intOffset)
    {
        offSet = intOffset;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * layout下卷的功能
     */
    public void scrollBottomMenuDown() {
        if (isMoveDown == false) {
            setVisibility(View.VISIBLE);
            isMoveDown = true;
            scrollBottomContent(0, -(int) ((135 * scaleValue) + offSet));
        }
    }

    /**
     * layout上卷的功能
     */
    public void scrollBottomMenuUp() {
        if (isMoveDown == true) {
            setVisibility(View.VISIBLE);
            isMoveDown = false;
            scrollBottomContent(-(int) ((135 * scaleValue) + offSet), (int) ((135 * scaleValue) + offSet));
        }
    }

    private boolean endTeachMode = false;

    private Dialog progressDialog = null;
    private WhenComputeScroll whenComputeScroll = null;

    public void setWhenComputeScroll(WhenComputeScroll whenComputeScroll)
    {
        this.whenComputeScroll = whenComputeScroll;
    }

    public interface WhenComputeScroll
    {
        void computeScroll(int intScrollX, int intScrollY);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.computeScrollOffset()) {
            thisLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            if(whenComputeScroll != null)
            {
                whenComputeScroll.computeScroll(mScroller.getCurrX(), mScroller.getCurrY());
            }

            //必须调用该方法，否则不一定能看到滚动效果
            invalidate();

        } else {
//			Log.e(tag,"scroll is finish? "+mScroller.isFinished());
            mScroller.abortAnimation();

            if (isMoveDown) {
                //下卷完成
//                setVisibility(View.GONE);
            } else {
                //上卷完成
//                setVisibility(View.VISIBLE);
            }

            if (isIconClick == true && mScroller.isFinished())//若按鍵己被按下，則isIconClick會變成true
            {
                isIconClick = false;

                //這裡一定要用Runnable換頁，推測是因為上下移的按鍵在讀圖，這時再做BannerPager的換頁動作（有加上動畫）
                //就會出現nullPointException 錯在dispatchgetdisplaylist地方，而且在Log的錯誤完全找不到相關的訊息
//				handler.post(new Runnable(){
//
//					@Override
//					public void run() {
//						changePage(pageIndex);
//					}});
            }
        }
    }

    /**
     * 空的OnClickListener
     */
    private OnClickListener emptyClick = new OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    int count = 0;

    /**
     * 按鍵列共用OnClickListener
     */
    private OnClickListener onIconClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(tag, "onIconClick !!");

//			thisApp.playSoundEffect();

            int iconId = v.getId() - changeIcon;

            cancelOnClickListenerByIndex(iconId);

            pageIndex = iconId;

            if (iconId == 0 || iconId == 1) {
                count = 0;
            }

            Intent intent = null;

            if(onPressCallBack != null)
            {
                onPressCallBack.onPressChildViewID(pageIndex);
            }

            if (isMoveDown == false && isIconClick == false)//表示按鍵列己浮上來,按鍵鎖未鎖
            {
//				int iconId = v.getId() - changeIcon;
//
//				pageIndex = iconId;
//
//				//把按鍵列沉下去
//				isMoveDown = true;
//
//				//給一個tag, 這個tag在按鍵列沉下去後才會改變
//				isIconClick = true;
//
////				scrollBottomContent(0, -(int)(135 * scaleValue));
            }
        }
    };

    /**
     * 切換功能頁
     *
     * @param iconId
     */
    private void changePage(int iconId) {
        cancelOnClickListenerByIndex(iconId);

//        if (handler != null) {
//            //通知改變按鍵的狀態（按下或未按下）
//            Message message = Message.obtain();
//
//            message.obj = bottonIcon[iconId].getIconText();
//            message.arg1 = iconId;
//            message.what = CHANGE_TITLE;
//
//            handler.sendMessage(message);
//        }
    }

    /**
     * 取消被按下的按鍵的OnClickListner，其他的按鍵掛上去
     *
     * @param pageIndex
     */
    public void cancelOnClickListenerByIndex(int pageIndex) {
        for (int i = 0; i < bottonIcon.length; i++) {
            int iconId = bottonIcon[i].getIconId() - changeIcon;

            if (iconId == pageIndex) {
//                bottonIcon[iconId].setIconBackground(R.mipmap.page_register_bottom_menu_over);
                bottonIcon[iconId].setSelected(true);
                bottonIcon[i].setIconClickListener(null);
//				bottonIcon[i].setTextColor(Color.BLACK);

            } else {
                bottonIcon[iconId].setReleaseIconBackground();
                bottonIcon[iconId].setSelected(false);
                bottonIcon[i].setIconClickListener(onIconClick);
//				bottonIcon[i].setTextColor(Color.WHITE);
            }
        }
    }

    public String getIconTitleByIndex(int index) {
        return bottonIcon[index].getIconText();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
    }

    private void setScale(View view, int width, int height, int top, int left) {
        LayoutParams viewUse = (LayoutParams) view.getLayoutParams();

        if (viewUse == null) {
            viewUse = new LayoutParams(width, height);
        } else {
            viewUse.width = width;
            viewUse.height = height;
        }

        viewUse.topMargin = top;
        viewUse.leftMargin = left;
        view.setLayoutParams(viewUse);
    }

    public void release() {
        if (bottonIcon != null) {
            for (int i = 0; i < bottonIcon.length; i++) {
                if (bottonIcon[i] != null) {
                    bottonIcon[i].release();
                }
            }
        }

        if (bottomContent != null) {
            bottomContent.removeAllViews();
            bottomContent.setBackgroundDrawable(null);
        }

        if (bottomMenuLayout != null) {
            bottomMenuLayout.removeAllViews();
            bottomMenuLayout.setBackgroundDrawable(null);
        }

        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }

        bottonIcon = null;
        bottomContent = null;
        bottomMenuLayout = null;

        thisLayout = null;
    }

    public RelativeLayout getBottomMenuLayout() {
        return bottomMenuLayout;
    }

    public interface OnPressCallBack
    {
        public void onPressChildViewID(int viewID);
    }
}
