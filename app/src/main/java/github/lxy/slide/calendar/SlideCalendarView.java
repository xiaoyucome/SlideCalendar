package github.lxy.slide.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

public class SlideCalendarView extends View {
    private Surface surface;
    private Calendar calendar;
    private Date curDate; // 当前日历显示的月
    private Date selectedStartDate;// 选中的起始日期
    private Date selectedEndDate;// 选中的结束日期
    private Date today; // 今天的日期文字显示红色

    public SlideCalendarView(Context context) {
        super(context);
        init();

    }

    public SlideCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        curDate = selectedStartDate = selectedEndDate = today = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        surface = new Surface();
        surface.density = getResources().getDisplayMetrics().density;
        setBackgroundColor(surface.calendarBgColor);
    }

    /**
     * 设置控件宽高
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        surface.width = getResources().getDisplayMetrics().widthPixels;
        surface.height = (int) (getResources().getDisplayMetrics().heightPixels * 2 / 5);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(surface.width,
                MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(surface.height,
                MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        if (changed) {
            surface.init();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画周一到周日
        float weekTextY = surface.monthHeight + surface.weekHeight * 3 / 4f;//指定 周 的baseline
        for (int i = 0; i < surface.weekText.length; i++) {
            float weekTextX = i
                    * surface.cellWidth
                    + (surface.cellWidth - surface.weekPaint
                    .measureText(surface.weekText[i])) / 2f;//指定 周 的X方向坐标

            surface.cellBgPaint.setColor(surface.dateBgColor);//设置画笔颜色 为 周一到周日的背景色
            float left = surface.cellWidth * i;
            float top = 0;

            //画 周 的背景
            canvas.drawRect(left, top, left + surface.cellWidth, top
                    + surface.cellHeight, surface.cellBgPaint);

            //画 一 到 日
            canvas.drawText(surface.weekText[i], weekTextX, weekTextY,
                    surface.weekPaint);
        }
        super.onDraw(canvas);
    }
}
