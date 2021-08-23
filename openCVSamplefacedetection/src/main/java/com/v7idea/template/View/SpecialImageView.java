package com.v7idea.template.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.v7idea.tool.ViewScaling;


public class SpecialImageView extends ImageView {
    private final String tag = "SpecialImageView";
    private boolean isAutoReleaseImage = false;

    public void setAutoReleaseImage(boolean isAutoReleaseImage)
    {
        this.isAutoReleaseImage = isAutoReleaseImage;
    }

    public SpecialImageView(Context context) {
        super(context);
    }

    public SpecialImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SpecialImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    public void setImageRounded()
//    {
//        Drawable currentDrawable = getDrawable();
//
//        if(currentDrawable != null)
//        {
//            Bitmap bitmap = ((BitmapDrawable) currentDrawable).getBitmap();
//
//            Drawable roundedDrawable = ImageCatch.getRoundedDrawable(getContext(), bitmap, Air.convertDpToPixel(5f, getContext()));
//
//            setImageDrawable(roundedDrawable);
//        }
//    }

    public void release() {
        releaseForegroundDrawable();

        Drawable backgroundDrawable = this.getBackground();

        if (backgroundDrawable != null) {
            backgroundDrawable.setCallback(null);
            backgroundDrawable = null;
        }

        this.setBackgroundDrawable(null);
    }

    public void releaseForegroundDrawable()
    {
        Drawable getDrawable = this.getDrawable();

        if (getDrawable != null) {
            getDrawable.setCallback(null);
            getDrawable = null;
        }

        this.setImageDrawable(null);
    }

    private int viewWidth = -1;
    private int viewHeight = -1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //取得實際被繪製的寛
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

//		Log.e(tag, "viewWidth: " + viewWidth);
//		Log.e(tag, "viewHeight: " + viewHeight);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean isDrawCircle = false;
    public boolean isDrawRounded = false;

    public void setDrawCircleFrame(boolean isDrawCircle) {
        this.isDrawCircle = isDrawCircle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDrawCircle)
        {
//            Drawable drawable = getDrawable();
//
//            if (drawable == null) {
//                return;
//            }
//
//            if (getWidth() == 0 || getHeight() == 0) {
//                return;
//            }
//
//            Bitmap b = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
//                    && drawable instanceof VectorDrawable) {
//                ((VectorDrawable) drawable).draw(canvas);
//                b = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
//                Canvas c = new Canvas();
//                c.setBitmap(b);
//                drawable.draw(c);
//            } else {
//                b = ((BitmapDrawable) drawable).getBitmap();
//            }
//
//            if (b != null) {
//                Air thisApp = (Air) getContext().getApplicationContext();
//
//                int offset = (int) (margin * thisApp.getScaleValue());
//
//                Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
//
//                int w = getWidth(), h = getHeight();
//
//                RectF timeCircle = new RectF((offset / 2), (offset / 2), viewWidth - (offset / 2), viewHeight - (offset / 2));
//
//                Paint shellWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                shellWheelPaint.setStyle(Paint.Style.STROKE);
//                shellWheelPaint.setStrokeWidth((offset / 2));
//                shellWheelPaint.setColor(Color.WHITE);
//
//                canvas.drawArc(timeCircle, -90, 360, true, shellWheelPaint);
//
//                Bitmap roundBitmap = getCroppedBitmap(bitmap, w - offset);
//                canvas.drawBitmap(roundBitmap, (offset / 2), (offset / 2), null);
//            }
        }
        else if(isDrawRounded)
        {
            Drawable drawable = getDrawable();

            if (drawable == null) {
                return;
            }

            if (getWidth() == 0 || getHeight() == 0) {
                return;
            }

            Bitmap b = ((BitmapDrawable) drawable).getBitmap();

            BitmapShader shader = new BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(shader);

            float radius = ViewScaling.convertDpToPixel(5f, getContext());

            RectF rect = new RectF(0.0f, 0.0f, viewWidth, viewHeight);

// rect contains the bounds of the shape
// radius is the radius in pixels of the rounded corners
// paint contains the shader that will texture the shape
            canvas.drawRoundRect(rect, radius, radius, paint);
        }
        else
        {
            super.onDraw(canvas);
        }
    }

    private int margin = 14;

//    public Bitmap getCroppedBitmapTest()
//    {
//        Drawable drawable = getDrawable();
//
//        if (drawable == null) {
//            return;
//        }
//
//        if (getWidth() == 0 || getHeight() == 0) {
//            return;
//        }
//
//        Bitmap b = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
//                && drawable instanceof VectorDrawable) {
//            ((VectorDrawable) drawable).draw(canvas);
//            b = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas c = new Canvas();
//            c.setBitmap(b);
//            drawable.draw(c);
//        } else {
//            b = ((BitmapDrawable) drawable).getBitmap();
//        }
//
//        if (b != null) {
//            Air thisApp = (Air) getContext().getApplicationContext();
//
//            int offset = (int) (margin * thisApp.getScaleValue());
//
//            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
//
//            int w = getWidth(), h = getHeight();
//
//            RectF timeCircle = new RectF((offset / 2), (offset / 2), viewWidth - (offset / 2), viewHeight - (offset / 2));
//
//            Paint shellWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            shellWheelPaint.setStyle(Paint.Style.STROKE);
//            shellWheelPaint.setStrokeWidth((offset / 2));
//            shellWheelPaint.setColor(Color.WHITE);
//
//            canvas.drawArc(timeCircle, -90, 360, true, shellWheelPaint);
//
//            Bitmap roundBitmap = getCroppedBitmap(bitmap, w - offset);
//            canvas.drawBitmap(roundBitmap, (offset / 2), (offset / 2), null);
//        }
//    }

    public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(isAutoReleaseImage)
        {
            release();
        }
    }
}
