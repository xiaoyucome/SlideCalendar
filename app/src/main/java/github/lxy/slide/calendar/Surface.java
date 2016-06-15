package github.lxy.slide.calendar;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * 1. 布局尺寸 2. 文字颜色，大小 3. 当前日期的颜色，选择的日期颜色
 */
public class Surface {
    public float density;
    public int width; // 整个控件的宽度
    public int height; // 整个控件的高度
    public float cellWidth; // 日期方框宽度
    public float cellHeight; // 日期方框高度
    public float monthHeight; // 显示月的高度
    public float weekHeight; // 显示星期的高度
    public float monthChangeWidth; // 上一月、下一月按钮宽度

    // 颜色设置
    private int weekColor = Color.parseColor("#a1e8ef");// 周一到周日的颜色
    public int todayNumberColor = Color.parseColor("#ff804e");// 今天日期的颜色
    public int dateBgColor = Color.parseColor("#50c8d3");// 周一到周日的背景色
    private int dateMiddleColor = Color.parseColor("#f4f5f7");// 周一到周日的背景色
    public int cellDownColor = Color.parseColor("#5dcad5");// 按下的日期背景框的颜色
    private int noneMonthDateColor = Color.parseColor("#c8c8c8");// 非本月日期的颜色
    private int monthColor = Color.parseColor("#555555");// 本月的日期颜色(1-31)的颜色
    public int calendarBgColor = Color.parseColor("#ffffff");// 整个日历的背景色,不包括上面的title
    public int selectDateColor = Color.parseColor("#ffffff");// 滑动选择的时候,被选中的日期为白色
    public int dateSelectMiddleColor = Color.parseColor("#85dde5");// 滑动选择日期非首位背景框的颜色

    public Paint weekPaint;// 周一到周日的画笔
    public Paint monthPaint;// 日期的画笔
    public Paint cellBgPaint;// 被选中的日期背景框的画笔
    public Paint dateMiddlePaint;// 六行日期中,第三四两行的画笔
    public String[] weekText = {"日", "一", "二", "三", "四", "五", "六"};

    public void init() {
        float temp = height / 7f;
        monthHeight = 0;// (float) ((temp + temp * 0.3f) * 0.6);
        monthChangeWidth = monthHeight * 1.5f;
        weekHeight = (float) ((temp + temp * 0.3f) * 0.7);
        cellHeight = (height - monthHeight - weekHeight) / 6f;
        cellWidth = width / 7f;

        // 周一到周日的画笔
        weekPaint = new Paint();
        weekPaint.setColor(weekColor);
        weekPaint.setAntiAlias(true);
        float weekTextSize = weekHeight * 0.6f;
        weekPaint.setTextSize(weekTextSize);

        // 日期的画笔
        monthPaint = new Paint();
        monthPaint.setColor(monthColor);
        monthPaint.setAntiAlias(true);
        float cellTextSize = cellHeight * 0.5f;
        monthPaint.setTextSize(cellTextSize);

        // 被选中的日期背景框的画笔
        cellBgPaint = new Paint();
        cellBgPaint.setAntiAlias(true);
        cellBgPaint.setStyle(Paint.Style.FILL);

        // 六行日期中,第三四两行的画笔
        dateMiddlePaint = new Paint();
        dateMiddlePaint.setAntiAlias(true);
        dateMiddlePaint.setStyle(Paint.Style.FILL);
    }
}
