package github.lxy.slide.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SlideCalendarView extends View implements View.OnTouchListener{
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
    private Date showFirstDate, showLastDate; // 日历显示的第一个日期和最后一个日期
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

        // 设置按下和选择的背景色--多选完的也是这个方法画的
        drawDownOrSelectedBg(canvas);

        // write date number
        // today index
        todayIndex = -1;
        calendar.setTime(curDate);
        String curYearAndMonth = calendar.get(Calendar.YEAR) + ""
                + calendar.get(Calendar.MONTH);
        calendar.setTime(today);
        String todayYearAndMonth = calendar.get(Calendar.YEAR) + ""
                + calendar.get(Calendar.MONTH);
        if (curYearAndMonth.equals(todayYearAndMonth)) {
            int todayNumber = calendar.get(Calendar.DAY_OF_MONTH);// 22
            todayIndex = curStartIndex + todayNumber - 1;// 4+22-1=25--这个是今天(10.22)的22在0-41中的索引
        }

        // 画日期
        for (int i = 0; i < 42; i++) {// 0-41
            int color = surface.monthColor;// 默认的也就是本月日期的颜色
            if (isLastMonth(i)) {// 上一月的日期的颜色
                color = surface.noneMonthDateColor;
            } else if (isNextMonth(i)) {// 下一月的日期的颜色
                color = surface.noneMonthDateColor;
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
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);// 5
        int monthStart = dayInWeek;// 5
        if (monthStart == 1) {
            monthStart = 8;
        }
        monthStart -= 1;
        curStartIndex = monthStart;
        date[monthStart] = 1;

        // last month
        if (monthStart > 0) {
            calendar.set(Calendar.DAY_OF_MONTH, 0);
            int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);
            for (int i = monthStart - 1; i >= 0; i--) {
                date[i] = dayInmonth;
                dayInmonth--;
            }
            calendar.set(Calendar.DAY_OF_MONTH, date[0]);
        }
        showFirstDate = calendar.getTime();

        calendar.setTime(curDate);

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < monthDay; i++) {
            date[monthStart + i] = i + 1;
        }
        curEndIndex = monthStart + monthDay;

        // next month
        for (int i = monthStart + monthDay; i < 42; i++) {
            date[i] = i - (monthStart + monthDay) + 1;
        }

        if (curEndIndex < 42) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.set(Calendar.DAY_OF_MONTH, date[41]);
        showLastDate = calendar.getTime();
    }

    private void drawDownOrSelectedBg(Canvas canvas) {
        noUp = false;
        if (downDate != null && todayIndex <= downIndex) {
            drawCellBg(canvas, downIndex, surface.cellDownColor);// 绘制按下框的背景
        }

        if (downIndex == -1 && moveIndex == -1) {
            return;
        } else {
            if (downIndex < todayIndex)//不能滑
                return;
            if (downIndex == todayIndex) //只能下滑
                noUp = true;

            //往下划
            if (moveIndex > downIndex) {
                drawCellBg(canvas, downIndex, surface.cellDownColor);
                for (int i = downIndex + 1; i < moveIndex; i++) {
                    drawCellBg(canvas, i, surface.dateSelectMiddleColor);
                }
                drawCellBg(canvas, moveIndex, surface.cellDownColor);
                isSelectTime = true;
            }

            if (!noUp) {
                //往上划
                if (downIndex > moveIndex && moveIndex >= todayIndex) {
                    drawCellBg(canvas, downIndex, surface.cellDownColor);
                    for (int i = moveIndex; i < downIndex; i++) {
                        drawCellBg(canvas, i, surface.dateSelectMiddleColor);
                    }
                    drawCellBg(canvas, moveIndex, surface.cellDownColor);
                    isSelectTime = true;
                }
            }
        }
    }

    /**
     * 绘制按下框的背景
     */
    private void drawCellBg(Canvas canvas, int index, int color) {
        int x = getXByIndex(index);
        int y = getYByIndex(index);
        surface.cellBgPaint.setColor(color);
        float left = surface.cellWidth * (x - 1);
        float top = surface.monthHeight + surface.weekHeight + (y - 1)
                * surface.cellHeight;
        canvas.drawRect(left, top, left + surface.cellWidth, top
                + surface.cellHeight, surface.cellBgPaint);
    }

    // 根据按下的位置,得到横向按到的位置
    private int getXByIndex(int i) {
        return i % 7 + 1; // 1 2 3 4 5 6 7
    }

    // 根据按下的位置,得到纵向按到的位置
    private int getYByIndex(int i) {
        return i / 7 + 1; // 1 2 3 4 5 6
    }

    private boolean isLastMonth(int i) {
        if (i < curStartIndex) {
            return true;
        }
        return false;
    }

    private boolean isNextMonth(int i) {
        if (i >= curEndIndex) {
            return true;
        }
        return false;
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        isSelectTime = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setSelectedDateByCoor(event.getX(), event.getY());
                moveIndex = downIndex;
                break;
            case MotionEvent.ACTION_MOVE:
                setMoveDateByCoor(event.getX(), event.getY());
                if (moveIndex >= todayIndex && moveIndex <= 41)
                    invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (downDate == null || downDate == null)
                    break;

                if (isSelectTime && !downDate.before(today)) {
                    //对选择的日期进行边界判断
                    if (downDate.before(today)) {
                        downDate = today;
                    } else if (moveDate.before(today)) {
                        moveDate = today;
                    }

                    //对起始日期赋值
                    if (downDate.before(moveDate)) {//下滑
                        selectedStartDate = downDate;
                        selectedEndDate = moveDate;
                    } else {//上滑/单天
                        selectedStartDate = moveDate;
                        selectedEndDate = downDate;
                    }
                    if (onItemClickListener != null)
                        onItemClickListener.OnItemClick(selectedStartDate, selectedEndDate,
                                downDate);
                } else {
                    Toast.makeText(getContext(), "不能选之前的日期", Toast.LENGTH_SHORT).show();
                    if (onItemClickListener != null)
                        onItemClickListener.OnItemClick(null, null, null);
                }
                break;
        }
        return true;
    }

    /**
     * 根据抬起时的坐标xy,得到0-41的索引以及日期
     */
    public void setMoveDateByCoor(float x, float y) {
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
            if (isLastMonth(moveIndex)) {
                calendar.add(Calendar.MONTH, -1);
            } else if (isNextMonth(moveIndex)) {
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

    // 设置按下的一些日期数据
    private void setSelectedDateByCoor(float x, float y) {
        // change month
        if (y < surface.monthHeight) {// 按下了表示月份那行
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

        // cell click down
        if (y > surface.monthHeight + surface.weekHeight) {// 按在了日期上面
            int m = (int) (Math.floor(x / surface.cellWidth) + 1);// 得到横向按下的框的位置
            int n = (int) (Math
                    .floor((y - (surface.monthHeight + surface.weekHeight))
                            / Float.valueOf(surface.cellHeight)) + 1);// 得到纵向按下的框的位置
            downIndex = (n - 1) * 7 + m - 1;// 得到按下的位置在42(0-41)个框中的索引
            calendar.setTime(curDate);

            // 根据框的索引,判断这个日期是上一月、还是下一月的
            if (isLastMonth(downIndex)) {
                calendar.add(Calendar.MONTH, -1);
            } else if (isNextMonth(downIndex)) {
                calendar.add(Calendar.MONTH, 1);
            }
            calendar.set(Calendar.DAY_OF_MONTH, date[downIndex]);// date[downIndex]表示具体的提起(1.2.3...)
            moveDate = downDate = calendar.getTime();//此处moveDate必须赋值,防止在Up的时候崩溃的bug
        }
        invalidate();
    }
}
