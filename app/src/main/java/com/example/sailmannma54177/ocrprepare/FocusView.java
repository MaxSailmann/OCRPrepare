package com.example.sailmannma54177.ocrprepare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by sailmannma54177 on 02.12.2016.
 */

public class FocusView extends View {
    private Paint paint;

    private int top;
    private int bottom;
    private int left;
    private int right;

    public FocusView(Context context, int top, int bottom, int left, int right) {
        super(context);
        paint = new Paint();
        paint.setAntiAlias(true);
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRect(canvas);
        invalidate();
    }
    private void drawRect(Canvas canvas) {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(left,top, right, bottom,paint);

    }
}