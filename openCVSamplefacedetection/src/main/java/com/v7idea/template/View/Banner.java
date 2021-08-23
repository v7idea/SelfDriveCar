package com.v7idea.template.View;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v7idea.tool.ButtonData;
import com.v7idea.tool.IButtonData;
import com.v7idea.tool.ViewScaling;

import org.opencv.samples.facedetect.R;

import java.util.ArrayList;

/**
 * Created by mortal on 15/3/13.
 */
public class Banner extends LinearLayout
{
    private final String tag = "Banner";
    private Banner thisBanner = null;

    public Banner(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
    }

    public Banner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
    }

    public Banner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
    }

    private SpecialImageView headerBackground = null;
    private RelativeLayout contentLayout = null;
    private LinearLayout textContainer = null;

    private TextView hospitalName = null;
    private V7TitleView appName = null;
    private V7TitleView ShowRightText = null;

    private SpecialImageView backButton = null;

    private RelativeLayout IconContainer = null;
    private SpecialImageView ProductOrderListIcon = null;
    private SpecialImageView QrCodeIcon = null;

    private LinearLayout InterNetStatusBarContainer = null;
    private BannerUserBar ThisBannerUerBar = null;

    private RecyclerView buttonContainer = null;


    private ButtonContainerAdapter buttonContainerAdapter = null;
    private OnNetworkStateChange OnNetworkStateChange = null;
    //    private NetworkStateReceiver ThisNetworkStateReceiver = null;


    public void IsCloseBackIcon(boolean booleanIsClose) {
        if (backButton != null) {
            if (booleanIsClose) {
                backButton.setVisibility(View.INVISIBLE);
            } else {
                backButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void initShortBanner() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        contentLayout = (RelativeLayout) inflater.inflate(R.layout.banner_short_style, null);
        textContainer = (LinearLayout) contentLayout.findViewById(R.id.bannerShort_LinearLayout_textContainer);
        headerBackground = (SpecialImageView) contentLayout.findViewById(R.id.bannerShort_SpecialImageView_bannerBackground);
        hospitalName = (TextView) contentLayout.findViewById(R.id.bannerShort_TextView_touchArea);
        appName = (V7TitleView) contentLayout.findViewById(R.id.bannerShort_TextView_headerText);
//        bottomAppName = (V7TitleView) contentLayout.findViewById(R.id.bannerShort_TextView_headerTextHint);
        backButton = (SpecialImageView) contentLayout.findViewById(R.id.bannerShort_SpecialImageView_backIcon);

        IconContainer = (RelativeLayout) contentLayout.findViewById(R.id.bannerShort_RelativeLayout_IconContainer);
        ProductOrderListIcon = (SpecialImageView) contentLayout.findViewById(R.id.bannerShort_SpecialImageView_Icon);
        QrCodeIcon = (SpecialImageView) contentLayout.findViewById(R.id.bannerShort_SpecialImageView_QrIcon);

        InterNetStatusBarContainer = (LinearLayout) contentLayout.findViewById(R.id.bannerShort_LinearLayout_InternetStatusBar);
        ThisBannerUerBar = (BannerUserBar) InterNetStatusBarContainer.findViewById(R.id.bannerShort_BannerUserBar_ShowInternetStatus);
//        ShowUserName = (V7TitleView) InterNetStatusBarContainer.findViewById(R.id.bannerShort_V7TitleView_showUserName);
        ShowRightText = (V7TitleView) contentLayout.findViewById(R.id.bannerShort_V7TitleView_ShowText);

        this.addView(contentLayout);

//        ThisNetworkStateReceiver = new NetworkStateReceiver();
//        ThisNetworkStateReceiver.setWhenReceiverOnNetworkStateChangeAction(WhenNetWorkChangeListener);

//        getContext().registerReceiver(ThisNetworkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void scaleShortStyle(OnClickListener backButtonFunction) {
        ViewScaling.setScaleByRelativeLayout(textContainer);

        hospitalName = (TextView) contentLayout.findViewById(R.id.bannerShort_TextView_touchArea);
        ViewScaling.setScaleByRelativeLayout(hospitalName);

        appName = (V7TitleView) contentLayout.findViewById(R.id.bannerShort_TextView_headerText);
        ViewScaling.setScaleByLinearLayout(appName);
        ViewScaling.setScaleTextSize(appName, ViewScaling.getScaleMin());

        backButton = (SpecialImageView) contentLayout.findViewById(R.id.bannerShort_SpecialImageView_backIcon);
        ViewScaling.setScaleByRelativeLayout(backButton);

        ViewScaling.setScaleByRelativeLayout(IconContainer);
        ViewScaling.setScaleByRelativeLayout(ProductOrderListIcon);
        ViewScaling.setScaleByRelativeLayout(QrCodeIcon);
        ViewScaling.setScaleByRelativeLayout(ShowRightText);

        ViewScaling.setScaleTextSize(ShowRightText, ViewScaling.getScaleMin());

        ViewScaling.setScaleByLinearLayout(ThisBannerUerBar);
        ThisBannerUerBar.setScale();

//        if (titleText != null) {
//            appName.setText(titleText);
//        }

        hospitalName.setOnClickListener(backButtonFunction);
        backButton.setOnClickListener(backButtonFunction);

        isOpenRightIcon(false);
    }

    public void isOpenLeftIcon(boolean booleanIsOpenLeftIcon)
    {
        if(booleanIsOpenLeftIcon)
        {
            backButton.setVisibility(View.VISIBLE);
            hospitalName.setVisibility(View.VISIBLE);
        }
        else
        {
            backButton.setVisibility(View.GONE);
            hospitalName.setVisibility(View.GONE);
        }
    }

    public void initV7rcStyle()
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        contentLayout = (RelativeLayout) inflater.inflate(R.layout.banner_v7rclite_style, null);

        appName = (V7TitleView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.v7rcStyle_V7TitleView_ConnectDevice);

        buttonContainer = (RecyclerView) ViewScaling.findViewByIdAndScale(contentLayout, R.id.v7rcStyle_RecyclerView_ButtonContainer);

        this.addView(contentLayout);

        buttonContainerAdapter = new ButtonContainerAdapter();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        buttonContainer.setLayoutManager(linearLayoutManager);
        buttonContainer.setAdapter(buttonContainerAdapter);
    }

    public void addButton(String strButtonText, int imageResource, OnClickListener onClickListener)
    {
        if(buttonContainerAdapter != null)
        {
            buttonContainerAdapter.addButton(strButtonText, imageResource, onClickListener);
        }
    }



//    public void reRegisterBroadcastReceiver(String strActionName)
//    {
//        if(strActionName != null)
//        {
//            IntentFilter intentFilter = new IntentFilter("com.HotelityIn.CSS_SplendorHotel_user.NoLogin");
//            intentFilter.addAction(strActionName);
//
//            getContext().unregisterReceiver(NoLoginReceiver);
//            getContext().registerReceiver(NoLoginReceiver, intentFilter);
//        }
//    }

    public void setShowRightText(String strText)
    {
        if(ShowRightText != null)
        {
            ShowRightText.setText(strText);
        }
    }

    public void setShowRightTextOnClickListener(OnClickListener onClickListener)
    {
        if(ShowRightText != null)
        {
            ShowRightText.setOnClickListener(onClickListener);
        }
    }

    public void setTitleString(String strTitle) {
        if (TextUtils.isEmpty(strTitle) == false && appName != null) {
            appName.setText(strTitle);
        }
    }

    public void setUserName(String strUserName) {
        if (strUserName != null) {
//            ShowUserName.setVisibility(View.VISIBLE);
//            ShowUserName.setText(strUserName);
        } else {
//            ShowUserName.setVisibility(View.GONE);
//            ShowUserName.setText("");
        }
    }

    public void setPressProductIconListener(OnClickListener OnPressProductIcon) {
        if (OnPressProductIcon != null) {
            IconContainer.setVisibility(View.VISIBLE);
            IconContainer.setOnClickListener(OnPressProductIcon);
        }
    }

    public void isOpenRightIcon(boolean booleanIsOpenRightIcon) {
        if (booleanIsOpenRightIcon) {
            IconContainer.setVisibility(View.VISIBLE);
        } else {
            IconContainer.setVisibility(View.GONE);
        }
    }

    public SpecialImageView getQrCodeIcon()
    {
        return QrCodeIcon;
    }

    public SpecialImageView getRightIcon() {
        return ProductOrderListIcon;
    }

//    public void removeBackgroundImage()
//    {
//        if(backgroundImage != null)
//        {
//            contentLayout.removeView(backgroundImage);
//            headerBackground.setImageResource(R.mipmap.hi_head_new);
//        }
//    }

//    public void setBackgroundImageByUrl(String strImageUrl, int defaultResID)
//    {
//        if(Air.isStringNotNullAndEmpty(strImageUrl))
//        {
//            backgroundImage.setVisibility(View.VISIBLE);
//
//            APIFetch ApiFetch = new APIFetch();
//
//            backgroundImage.setImageDrawableByUrl(ApiFetch.getDownLoadImageApi(strImageUrl), Air.getAppImageFolder(), false, defaultResID);
//        }
//        else
//        {
//            backgroundImage.setImageResource(defaultResID);
//        }
//    }

//    public void setBackgroundImage(String strImage)
//    {
//        if(backgroundImage != null)
//        {
//            backgroundImage.setImageByDataString(strImage);
//
//            if(Air.isStringNotNullAndEmpty(strImage))
//            {
//                backgroundImage.setVisibility(View.VISIBLE);
//            }
//        }
//    }

    private void showNetWorkBar(boolean booleanIsConnectInternet) {
        if (booleanIsConnectInternet) {
            ThisBannerUerBar.setVisibility(View.GONE);
        } else {
            ThisBannerUerBar.setVisibility(View.VISIBLE);
        }
    }

    public interface OnNetworkStateChange {
        void OnNetworkStateChangeListener(boolean booleanIsConnectInternet);
    }

    public void setOnNetworkStateChangeListener(OnNetworkStateChange OnNetworkStateChange) {
        this.OnNetworkStateChange = OnNetworkStateChange;
    }

//    private NetworkStateReceiver.OnNetworkStateChange WhenNetWorkChangeListener = new NetworkStateReceiver.OnNetworkStateChange()
//    {
//        @Override
//        public void OnNetworkStateChangeListener(boolean booleanIsConnectInternet) {
////            SimpleDatabase tmpDataBase = new SimpleDatabase();
////
////            boolean isUpDate = tmpDataBase.getBooleanValueByKey(SimpleDatabase.UnUpDateBible, true);
////
////            if(isUpDate)
////            {
////                showNetWorkBar(booleanIsConnectInternet);
////            }
//
//            Log.e("NetworkStateReceiver", "booleanIsConnectInternet: " + booleanIsConnectInternet);
//
//            if(OnNetworkStateChange != null)
//            {
//                OnNetworkStateChange.OnNetworkStateChangeListener(booleanIsConnectInternet);
//            }
//        }
//    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

//        if(ThisNetworkStateReceiver != null)
//        {
//            getContext().unregisterReceiver(ThisNetworkStateReceiver);
//        }
    }

    private class ButtonContainerAdapter extends RecyclerView.Adapter<ButtonContainerAdapter.ButtonContainerViewHolder>
    {
        private LayoutInflater thisLayoutInflater = null;
        private ArrayList<IButtonData> buttonDataList = null;

        public ButtonContainerAdapter()
        {
            thisLayoutInflater = LayoutInflater.from(getContext());
            buttonDataList = new ArrayList<IButtonData>();
        }

        public void addButton(String strButtonText, int imageResource, OnClickListener onClickListener)
        {
            buttonDataList.add(new ButtonData(strButtonText, imageResource, onClickListener));
            notifyDataSetChanged();
        }

        @Override
        public ButtonContainerViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View contentView = thisLayoutInflater.inflate(R.layout.button_container_list_item, parent, false);

            return new ButtonContainerViewHolder(contentView);
        }

        @Override
        public void onBindViewHolder(ButtonContainerViewHolder holder, int position)
        {
            if(holder != null)
            {
                IButtonData iButtonData = getItem(position);

                if(iButtonData != null)
                {
                    holder.backgroundImage.setImageResource(iButtonData.getButtonImageResourceID());
                    holder.showText.setText(iButtonData.getButtonText());
                    holder.itemView.setOnClickListener(iButtonData.getOnClickListener());
                }
            }
        }

        @Override
        public int getItemCount()
        {
            if(buttonDataList != null)
            {
                return buttonDataList.size();
            }
            else
            {
                return 0;
            }
        }

        public IButtonData getItem(int position)
        {
            if(buttonDataList != null && position > -1 && position < buttonDataList.size())
            {
                return buttonDataList.get(position);
            }
            else
            {
                return null;
            }
        }

        public class ButtonContainerViewHolder extends RecyclerView.ViewHolder
        {
            public SpecialImageView backgroundImage = null;
            public V7TitleView showText = null;

            public ButtonContainerViewHolder(View itemView) {
                super(itemView);

                backgroundImage = (SpecialImageView) ViewScaling.findViewByIdAndScale(itemView, R.id.buttonItem_AppCompatImageView_background);
                showText = (V7TitleView) ViewScaling.findViewByIdAndScale(itemView, R.id.buttonItem_AppCompatTextView_showText);
            }
        }
    }
}
