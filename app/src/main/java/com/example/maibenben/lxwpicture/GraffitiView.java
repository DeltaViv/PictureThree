package com.example.maibenben.lxwpicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by MAIBENBEN on 2020/6/12.
 */
public class GraffitiView extends View
{
    private Paint paint = null;
    /*
     * 源图
     */
    public Bitmap originalBitmap = null;
    /*
     * 需要涂鸦的图片
     */
    public Bitmap new1Bitmap = null;
    /*
     * 涂鸦之前
     */
    public Bitmap new2Bitmap = null;
    /*
     * 触摸时的X、Y坐标
     */
    private float clickX = 0;
    private float clickY = 0;
    /*
     * 每次绘制的起点
     */
    private float startX = 0;
    private float startY = 0;
    /*
     * 是否进行绘制
     */
    private boolean isMove = true;
    /*
     * 是否进行清空
     */
    private boolean isClear = false;
    /*
     * 画笔颜色
     */
    private int color = Color.GREEN;
    /*
     * 笔尖大小
     */
    private float strokeWidth = 2.0f;

    public GraffitiView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bgty).copy(Bitmap.Config.ARGB_8888, true);
        new1Bitmap = Bitmap.createBitmap(originalBitmap);
    }




    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawBitmap(HandWriting(new1Bitmap,isClear), 0, 0,null);

    }


    /**
     *  * 涂鸦的图案
     * @param originalBitmap
     * @param isClear 是否清空
     * @return 涂鸦的图案
     */
    public Bitmap HandWriting(Bitmap originalBitmap,boolean isClear)
    {
        Canvas canvas = null;
        if(isClear){  //清空
            canvas = new Canvas(new2Bitmap);
        }
        else{  //不清空
            canvas = new Canvas(originalBitmap);
        }
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        if(isMove){  //当移动时绘制
            canvas.drawLine(startX, startY, clickX, clickY, paint);
        }
        /*
         * 每次绘制的起点
         */
        startX = clickX;
        startY = clickY;
        if(isClear){  //清空
            return new2Bitmap;
        }
        return originalBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        clickX = event.getX();
        clickY = event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            /*
             * 当按下，并不移动时，刷新，但不绘制，只是记录按下时的坐标点，根据（isMove）  。
             */
            isMove = false;
            invalidate();
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){
        	/*
        	 *当移动时，根据(isMove)进行绘制，从按下的坐标点起。
        	 */
            isMove = true;
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 清空
     */
    public void clear(Bitmap originalBitmap){
        isClear = true;
        this.originalBitmap = originalBitmap;
        new2Bitmap = Bitmap.createBitmap(originalBitmap);
        invalidate();
    }

    /**
     * 设置涂鸦线条的宽度
     * @param strokeWidth 宽度
     */
    public void setstyle(float strokeWidth){
        this.strokeWidth = strokeWidth;
    }

    /**
     * 设置画笔颜色
     * @param color 颜色
     */
    public void setColor(int color){
        this.color=color;
    }

}
