package github.lxy.slide.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SlideCalendarView extends View {
    private Surface surface;
    private Calendar calendar;
    private int todayIndex = -1;
    private boolean noUp;//true不能上滑
    private Date curDate; // 当前日历显示的月
    private boolean isSelectTime;//true已经选择
    private Date today; // 今天的日期文字显示红色
    private int downIndex = -1; // 按下的格子索引
    private int moveIndex = -1; // 抬起的格子索引
    private Date selectedEndDate;// 选中的结束日期
    private Date selectedStartDate;// 选中的起始日期
    private Date downDate; // 手指按下日期
    private Date moveDate; // 手指移动日期
    private Date mLastMoveDate; // 手指上次移动日期
    private int[] date = new int[42]; // 日历显示数字
    private int curStartIndex, curEndIndex; // 当前显示的日历起始的索引
    private boolean completed = false; // 为false表示只选择了开始日期，true表示结束日期也选择了
    private OnItemClickListener onItemClickListener;// 给控件设置监听事件

    // 给控件设置监听事件
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    // 监听接口
    public interface OnItemClickListener {
        void OnItemClick(Date selectedStartDate, Date selectedEndDate,
                         Date downDate);
    }

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
     *
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
        drawWeek(canvas);

        //日历中间区域的背景色
        drawMiddleBg(canvas);

        // 计算日期,主要对42个日期的位置上设置值,这个日期值包括上一月、本月、下一月,和正常的日历是对应的
        calculateDate();

        // 画日期
        for (int i = 0; i < 42; i++) {// 0-41
            int color;
            if (i < curStartIndex || i >= curEndIndex) {
                color = surface.noneMonthDateColor;
            } else {
                color = surface.monthColor;//本月日期的颜色
            }

            //选中的时候,被选中日期为白色
            //下滑、上滑
            if (i >= todayIndex && downIndex >= todayIndex) {
                if ((i >= downIndex && i <= moveIndex) || (i >= moveIndex && i <= downIndex)) {
                    color = surface.selectDateColor;
                }
            }

            if (todayIndex != -1 && i == todayIndex) {
                color = surface.todayNumberColor;// 今天的颜色
            }
            drawCellText(canvas, i, date[i] + "", color);
        }

        super.onDraw(canvas);
    }

    private void drawWeek(Canvas canvas) {
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
    }

    private void drawMiddleBg(Canvas canvas) {
        surface.dateMiddlePaint.setColor(surface.dateMiddleColor);
        float middleTop = surface.weekHeight + 2 * surface.cellHeight;
        canvas.drawRect(0, middleTop, surface.width, middleTop + surface.cellHeight * 2, surface.dateMiddlePaint);
    }

    private void calculateDate() {
        calendar.setTime(curDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1);//将今天设为本月第一天

        int monthStart = calendar.get(Calendar.DAY_OF_WEEK);//获取今天是周几
        if (monthStart == 1) {
            monthStart = 8;
        }
        monthStart -= 1;//美国的第一天是sunday,所以减1得到中国的日期
        curStartIndex = monthStart;
        date[monthStart] = 1;//设置本月第一天为1

        /**
         * 为上一个月日期赋值
         */
        calendar.set(Calendar.DAY_OF_MONTH, 0);//设置今天为这个月第0天，即上个月最后一天
        int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);//获取到上个月最后一天
        for (int i = monthStart - 1; i >= 0; i--) {
            date[i] = dayInmonth;
            dayInmonth--;
        }

        calendar.setTime(curDate);
        calendar.add(Calendar.MONTH, 1);//当前月份加1个月,下个月的15号
        calendar.set(Calendar.DAY_OF_MONTH, 0);//设置下个月15号为下个月的第0天，即这个月的最后一天
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        /**
         * 为本月日期赋值
         */
        for (int i = 1; i < monthDay; i++) {
            date[monthStart + i] = i + 1;
        }

        curEndIndex = monthStart + monthDay;//下个月第一个角标

        /**
         * 为下个月日期赋值
         */
        for (int i = monthStart + monthDay; i < 42; i++) {
            date[i] = i - (monthStart + monthDay) + 1;
        }
    }

    // 根据按下的位置,得到横向按到的位置
    private int getXByIndex(int i) {
        return i % 7 + 1; // 1 2 3 4 5 6 7
    }

    // 根据按下的位置,得到纵向按到的位置
    private int getYByIndex(int i) {
        return i / 7 + 1; // 1 2 3 4 5 6
    }

    /**
     * 画日期
     */
    private void drawCellText(Canvas canvas, int index, String text, int color) {
        int x = getXByIndex(index);// 根据按下的位置,得到横向按到的位置
        int y = getYByIndex(index);
        surface.monthPaint.setColor(color);
        float cellY = surface.monthHeight + surface.weekHeight + (y - 1)
                * surface.cellHeight + surface.cellHeight * 3 / 4f;
        float cellX = (surface.cellWidth * (x - 1))
                + (surface.cellWidth - surface.monthPaint.measureText(text))
                / 2f;
        canvas.drawText(text, cellX, cellY, surface.monthPaint);
    }
}
