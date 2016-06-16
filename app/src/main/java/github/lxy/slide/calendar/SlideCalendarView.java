package github.lxy.slide.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SlideCalendarView extends View implements View.OnTouchListener {
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
        setOnTouchListener(this);
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

        // 设置按下和滑动时的背景
        drawDownOrSlidedBg(canvas);

        //计算今天在date[]中的角标
        calendar.setTime(curDate);
        int todayNumber = calendar.get(Calendar.DAY_OF_MONTH);
        todayIndex = curStartIndex + todayNumber - 1;
        Log.d("111111111111","-----22222----"+todayIndex);
        Log.d("111111111111","-----22222--todayNumber--"+todayNumber);
        Log.d("111111111111","-----22222--curStartIndex--"+curStartIndex);
        // 画日期
        darwDate(canvas);

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setSelectedDateByCoor(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                setMoveDateByCoor(event.getX(), event.getY());
                //只能选择今天之后 并且 不超过最后一个日期的角标
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
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

    private void darwDate(Canvas canvas) {
        for (int i = 0; i < 42; i++) {// 0-41
            int color;
            if (i < curStartIndex || i >= curEndIndex) {
                color = surface.noneMonthDateColor;
            } else {
                color = surface.monthColor;//本月日期的颜色
            }

            //选中日期的首尾之间的字体颜色
            if ((i >= downIndex && i <= moveIndex) || (i >= moveIndex && i <= downIndex)) {
                color = surface.selectDateColor;
            }

            //今天日期的字体颜色
            if (todayIndex != -1 && i == todayIndex) {
                color = surface.todayNumberColor;
            }

            drawCellText(canvas, i, date[i] + "", color);
        }
    }

    /**
     * 画日期
     */
    private void drawCellText(Canvas canvas, int index, String text, int color) {
        int x = getXByIndex(index);// 根据index,得到 横 向按到的位置
        int y = getYByIndex(index);// 根据index,得到 纵 向按到的位置
        surface.monthPaint.setColor(color);
        float cellY = surface.monthHeight + surface.weekHeight + (y - 1) * surface.cellHeight + surface.cellHeight * 3 / 4f;//指定每行baseline
        float cellX = (surface.cellWidth * (x - 1)) + (surface.cellWidth - surface.monthPaint.measureText(text)) / 2f;
        canvas.drawText(text, cellX, cellY, surface.monthPaint);
    }

    // 根据按下的位置,得到横向按到的位置
    private int getXByIndex(int i) {
        return i % 7 + 1; // 1 2 3 4 5 6 7
    }

    // 根据按下的位置,得到纵向按到的位置
    private int getYByIndex(int i) {
        return i / 7 + 1; // 1 2 3 4 5 6
    }

    private void setSelectedDateByCoor(float x, float y) {
        /**
         * 按在了切换月份那行
         */
        if (y < surface.monthHeight) {
            // pre month
            if (x < surface.monthChangeWidth) {// 上一月的按钮
                calendar.setTime(curDate);
                calendar.add(Calendar.MONTH, -1);
                curDate = calendar.getTime();
            }

            // next month
            else if (x > surface.width - surface.monthChangeWidth) {// 下一月的按钮
                calendar.setTime(curDate);
                calendar.add(Calendar.MONTH, 1);
                curDate = calendar.getTime();
            }
        }

        /**
         * 按在了日期上面
         */
        if (y > surface.monthHeight + surface.weekHeight) {// 按在了日期上面
            int m = (int) (Math.floor(x / surface.cellWidth) + 1);// 得到横向按下的框的位置
            int n = (int) (Math
                    .floor((y - (surface.monthHeight + surface.weekHeight))
                            / Float.valueOf(surface.cellHeight)) + 1);// 得到纵向按下的框的位置
            downIndex = (n - 1) * 7 + m - 1;// 得到按下的位置在42(0-41)个框中的索引
            calendar.setTime(curDate);

            // 根据框的索引,判断这个日期是上一月、还是下一月的
            if (downIndex < curStartIndex) {
                calendar.add(Calendar.MONTH, -1);
            } else if (downIndex <= curEndIndex) {
                calendar.add(Calendar.MONTH, 1);
            }
            calendar.set(Calendar.DAY_OF_MONTH, date[downIndex]);// date[downIndex]表示具体的提起(1.2.3...)
            downDate = calendar.getTime();//此处moveDate必须赋值,防止在Up的时候崩溃的bug
        }
        invalidate();
    }

    public void drawDownOrSlidedBg(Canvas canvas) {
        //按下
        if (downDate != null) {
            drawCellBg(canvas, downIndex, surface.cellDownColor);// 绘制按下框的背景
        }

        //下滑
        if (moveIndex > downIndex) {
            for (int i = downIndex + 1; i < moveIndex; i++) {
                drawCellBg(canvas, i, surface.dateSelectMiddleColor);
            }
            drawCellBg(canvas, moveIndex, surface.cellDownColor);
        }

        //上滑
        if (downIndex > moveIndex) {
            for (int i = moveIndex; i < downIndex; i++) {
                drawCellBg(canvas, i, surface.dateSelectMiddleColor);
            }
            drawCellBg(canvas, moveIndex, surface.cellDownColor);
        }
    }

    /**
     * 绘制按下和滑动时候的背景
     */
    private void drawCellBg(Canvas canvas, int index, int color) {
        int x = getXByIndex(index);
        int y = getYByIndex(index);
        surface.cellBgPaint.setColor(color);
        float left = surface.cellWidth * (x - 1);
        float top = surface.monthHeight + surface.weekHeight + (y - 1) * surface.cellHeight;
        canvas.drawRect(left, top, left + surface.cellWidth, top + surface.cellHeight, surface.cellBgPaint);
    }

    /**
     * 根据移动时的坐标xy,得到0-41的索引以及日期
     */
    public void setMoveDateByCoor(float x, float y) {
        /**
         * 移动时不能超出边界
         */
        if (y < surface.monthHeight + surface.weekHeight) {
            return;
        }
        if (y >= surface.height) {
            return;
        }

        if (y > surface.monthHeight + surface.weekHeight) {// 按在了日期上面
            int m = (int) (Math.floor(x / surface.cellWidth) + 1);// 得到横向按下的框的位置
            int n = (int) (Math
                    .floor((y - (surface.monthHeight + surface.weekHeight))
                            / Float.valueOf(surface.cellHeight)) + 1);// 得到纵向按下的框的位置
            moveIndex = (n - 1) * 7 + m - 1;// 得到按下的位置在42(0-41)个框中的索引
            calendar.setTime(curDate);

            // 根据框的索引,判断这个日期是上一月、还是下一月的
            if (moveIndex < curStartIndex) {
                calendar.add(Calendar.MONTH, -1);
            } else if (moveIndex <= curEndIndex) {
                calendar.add(Calendar.MONTH, 1);
            }

            calendar.set(Calendar.DAY_OF_MONTH, date[moveIndex]);// date[startIndex]表示具体的提起(1.2.3...)
            moveDate = calendar.getTime();
            if (mLastMoveDate == null) {
                mLastMoveDate = moveDate;
            }
            if (moveDate.before(today))
                moveDate = mLastMoveDate;
            mLastMoveDate = moveDate;
        }
    }
}
