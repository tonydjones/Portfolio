package com.example.medicationtracker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.ByteArrayOutputStream;

public class SignatureView extends View {

    private Bitmap _Bitmap;
    private Canvas _Canvas;
    private Path _Path;
    private Paint _BitmapPaint;
    private Paint _paint;
    private float _mX;
    private float _mY;
    private float TouchTolerance = 4;
    private float LineThickness = 4;

    public SignatureView(Context context, AttributeSet attr) {
        super(context, attr);
        _Path = new Path();
        _BitmapPaint = new Paint(Paint.DITHER_FLAG);
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setColor(Color.argb(255, 0, 0, 0));
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setStrokeWidth(LineThickness);
    }

    public void refresh_size(ScrollView scroll, LinearLayout.LayoutParams unweighted_params, LinearLayout.LayoutParams weighted_params, int signature_size){
        SignatureView signature = this;
        signature.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!scroll.getLayoutParams().equals(unweighted_params) && signature.getHeight() > signature_size){
                    scroll.setLayoutParams(unweighted_params);
                }
                else if (!scroll.getLayoutParams().equals(weighted_params) && signature.getHeight() < signature_size){
                    scroll.setLayoutParams(weighted_params);
                }
                signature.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, signature_size));
                signature.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        if (scroll.getLayoutParams().equals(weighted_params)){
            this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, signature_size, 1));
        }
        else {
            signature.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        _Bitmap = Bitmap.createBitmap(w, (h > 0 ? h : ((View)(this.getParent())).getHeight()), Bitmap.Config.ARGB_8888);
        _Canvas = new Canvas(_Bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFFF2F2F2);
        canvas.drawBitmap(_Bitmap, 0, 0, _BitmapPaint);
        canvas.drawPath(_Path, _paint);
    }

    private void TouchStart(float x, float y) {
        _Path.reset();
        _Path.moveTo(x, y);
        _mX = x;
        _mY = y;
    }

    private void TouchMove(float x, float y) {
        float dx = Math.abs(x - _mX);
        float dy = Math.abs(y - _mY);

        if (dx >= TouchTolerance || dy >= TouchTolerance) {
            _Path.quadTo(_mX, _mY, (x + _mX) / 2, (y + _mY) / 2);
            _mX = x;
            _mY = y;
        }
    }

    private void TouchUp() {
        if (!_Path.isEmpty()) {
            _Path.lineTo(_mX, _mY);
            _Canvas.drawPath(_Path, _paint);
        } else {
            _Canvas.drawPoint(_mX, _mY, _paint);
        }

        _Path.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                TouchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                TouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                TouchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void ClearCanvas() {
        _Canvas.drawColor(0xFFF2F2F2);
        invalidate();
    }

    public byte[] getBytes() {
        Bitmap b = getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public Bitmap getBitmap() {
        View v = (View) this;
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);

        return b;
    }
}