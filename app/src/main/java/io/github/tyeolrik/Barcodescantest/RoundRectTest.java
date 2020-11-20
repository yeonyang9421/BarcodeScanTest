package io.github.tyeolrik.Barcodescantest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

public class RoundRectTest extends AppCompatActivity {

    private Canvas canvas;
    private Paint paint, myPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        SmallCircleView mv = new SmallCircleView(this);
//        setContentView(mv);


        setContentView(R.layout.activity_round_rect_test);
//        Path path = RoundedRect(0, 0, fwidth , fheight , 5,5,
//                false, true, true, false);
//        canvas.drawPath(path,myPaint);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        canvas.drawArc(new RectF(0,100,100,300), 0, 60, true, paint);
        canvas.drawRoundRect(new RectF(0, 100, 100, 300), 6, 6, paint);
//        canvas.drawTopRoundRect(rect, paint, radius)

    }

  public static   class MyView extends View {

        public MyView(Context context){

            super(context);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint p = new Paint();
            p.setColor(Color.WHITE);

            // 설정된 컬러값으로 캔버스 바탕 그리기

            canvas.drawPaint(p);

            // 사각형의 도형을 관리하는 RectF 객체 사용해서 사각형 그리기

            RectF r = new RectF(0,100,100,300);

            p.setColor(Color.YELLOW);

            canvas.drawRoundRect(r, 10, 10, p);

        canvas.drawRoundRect(r, 10, 10, p);

            r.set(110, 10, 150, 100);

            // 사각형을 기준으로 그 안에 원을 그리라는 뜻

            canvas.drawOval(r,  p);
            p.setColor(Color.GREEN);
            r.set(10, 110, 100, 200);
            // drawArc는  startAngle 부터 sweepAngle 까지 각도분만큼 원호를

            // 그리는 메서드 입니다.

            // 각도는 360 기준이며 세시 방향부터 0도로 인식 합니다.

            // 각도가 커지는 방향은 시계방향이고 userCenter 변수는 원호의 끝과 중심을

            // 연결할 것인지를 지정합니다. true 값을 주면 피자 모양의 조각 원호가 그려지고

            // false 이면 원호의 끝끼리 연결하여 반달 모양을 만듭니다.

            canvas.drawArc(r, 10, 150, true, p);

            p.setColor(Color.BLUE);

            float[] pts = {10, 210, 50, 250, 50, 250, 110, 220};

            canvas.drawLines(pts,  p);

        }

    }

    public static Path RoundedRect(
            float left, float top, float right, float bottom, float rx, float ry,
            boolean tl, boolean tr, boolean br, boolean bl
    ){
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        if (tr)
            path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        else{
            path.rLineTo(0, -ry);
            path.rLineTo(-rx,0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (tl)
            path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        else{
            path.rLineTo(-rx, 0);
            path.rLineTo(0,ry);
        }
        path.rLineTo(0, heightMinusCorners);

        if (bl)
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
        else{
            path.rLineTo(0, ry);
            path.rLineTo(rx,0);
        }

        path.rLineTo(widthMinusCorners, 0);
        if (br)
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        else{
            path.rLineTo(rx,0);
            path.rLineTo(0, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

    static public Path RoundedRect(float left, float top, float right, float bottom, float rx, float ry, boolean conformToOriginalPost) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width/2) rx = width/2;
        if (ry > height/2) ry = height/2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);

        if (conformToOriginalPost) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        }
        else {
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }
}