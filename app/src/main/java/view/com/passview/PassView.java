package view.com.passview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

/**
 * 简化版的密码输入框
 */

public class PassView extends View {

    private static final int borderSize=6;

    private int saveResult;

    private static boolean iscleartextNum=false;

    private static boolean isinvalidate=false;

    private int borderwidth;

    private int borderheight;

    private int spacingwidth;

    private int measureWidth;

    private int measureHeight;

    private Paint broaderPaint;

    private Paint numPaint;

    private Paint circlePaint;

    private InputMethodManager input;

    private ArrayList<Integer> results=new ArrayList<>();



    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            isinvalidate=true;

            invalidate();
        }
    };



    public PassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    private void initView() {

        this.setFocusable(true);
        this.setFocusableInTouchMode(true);

        input = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        this.setOnKeyListener(new NumInputKeyListener());

        broaderPaint=new Paint();
        broaderPaint.setColor(Color.GRAY);
        broaderPaint.setStyle(Paint.Style.STROKE);
        broaderPaint.setStrokeWidth(3);

        numPaint=new Paint();
        numPaint.setColor(Color.GRAY);
        numPaint.setStyle(Paint.Style.STROKE);
        numPaint.setTextSize(30);

        circlePaint=new Paint();

        circlePaint.setColor(Color.GRAY);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureWidth=MeasureSpec.getSize(widthMeasureSpec);
        measureHeight=MeasureSpec.getSize(heightMeasureSpec);

        spacingwidth=10;
        borderwidth=((measureWidth-70)/6);
        borderheight=borderwidth;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(0,measureHeight/2);

        //绘制边界
        drawBroader(canvas);

        //延迟一秒绘制圆点
        if(isinvalidate){

            isinvalidate=false;

            drawCircle(canvas);

            return;
        }

        //清除num只绘制原点
        if(iscleartextNum==true){

            for(int i=0;i<results.size();i++){

                int x=i*(borderwidth+spacingwidth)+spacingwidth+borderwidth/2;

                int y=borderheight/2;

                canvas.drawCircle(x,y,15,circlePaint);
            }


        }else{
            //绘制文本圆点
            for(int i=0;i<results.size();i++){

                RectF rectF=new RectF(i*(borderwidth+spacingwidth)+spacingwidth,0,i*(borderwidth+spacingwidth)+(borderwidth+spacingwidth),borderheight);

                if(results.size()>0&&results.size()-1>=i){


                    drawText(canvas,rectF,results.get(i));


                    if(i+1==results.size()){

                        mHandler.sendMessageDelayed(new Message(),1000);
                    }

                    if(i>=1){

                        int x=(i-1)*(borderwidth+spacingwidth)+spacingwidth+borderwidth/2;

                        int y=borderheight/2;

                        canvas.drawCircle(x,y,15,circlePaint);
                    }
                }
            }


        }


    }

    private void drawBroader(Canvas canvas) {


        for(int i=0;i<borderSize;i++){

            RectF rectF=new RectF(i*(borderwidth+spacingwidth)+spacingwidth,0,i*(borderwidth+spacingwidth)+(borderwidth+spacingwidth),borderheight);

            canvas.drawRoundRect(rectF,10,10,broaderPaint);

        }

    }

    public void drawCircle(Canvas canvas){

        isinvalidate=false;

        for(int i=0;i<results.size();i++){

            int x=i*(borderwidth+spacingwidth)+spacingwidth+borderwidth/2;

            int y=borderheight/2;

            canvas.drawCircle(x,y,15,circlePaint);

        }
    }


    public void drawText(Canvas canvas,RectF rectF,int num){

        numPaint.setColor(Color.TRANSPARENT);
        canvas.drawRect(rectF,numPaint);

        numPaint.setColor(Color.GRAY);

        Paint.FontMetrics fontMetrics=numPaint.getFontMetrics();
        int baseline= (int) ((rectF.bottom+rectF.top-fontMetrics.bottom-fontMetrics.top)/2);
        numPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(num),rectF.centerX(),baseline,numPaint);
    }

    /** 输入类型为数字*/
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//inputType is number
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new NumInputConnection(this, false);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    class NumInputConnection extends BaseInputConnection {

        public NumInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            //Here is to accept the input method of the text, we only deal with the number, so what operations do not do
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            //Soft keyboard delete key DEL can not directly monitor, send their own del event
            if (beforeLength == 1 && afterLength == 0) {
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_DOWN){

            requestFocus();

            input.showSoftInput(this,InputMethodManager.SHOW_FORCED);

            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        if(!hasWindowFocus){
            input.hideSoftInputFromWindow(this.getWindowToken(),0);

        }
        super.onWindowFocusChanged(hasWindowFocus);
    }


    //键盘处理

    public class NumInputKeyListener implements OnKeyListener{

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {


            if(keyEvent.getAction()==KeyEvent.ACTION_DOWN){

                if(keyEvent.isShiftPressed()){

                    return  false;
                }

                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {//Only deal with numbers
                    if (results.size() < borderSize) {
                        iscleartextNum=false;
                        results.add(keyCode - 7);
                        saveResult=results.size();
                        invalidate();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    if (!results.isEmpty()) {
                        saveResult=results.size();
                        iscleartextNum=true;
                        results.remove(results.size() - 1);
                        invalidate();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
            }

            return false;
        }
    }

}
