package github.lxy.slide.calendar;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {
    private SlideCalendarView calendar;
    private TextView calendarCenter;
    private String startTime, endTime;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarCenter = (TextView) findViewById(R.id.calendarCenter);
        calendar = (SlideCalendarView) findViewById(R.id.calendar);
        ImageView arrow_left = (ImageView) findViewById(R.id.arrow_left);
        ImageView arrow_right = (ImageView) findViewById(R.id.arrow_right);
        arrow_left.setOnClickListener(this);
        arrow_right.setOnClickListener(this);

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ;
        try {
            calendar.setCalendarData(format.parse("2015-01-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendarCenter.setText(calendar.getYearAndmonth());
        calendar.setOnItemClickListener(new SlideCalendarView.OnItemClickListener() {
            @Override
            public void OnItemClick(Date selectedStartDate, Date selectedEndDate) {
                if (selectedStartDate != null && selectedEndDate != null) {
                    startTime = format.format(selectedStartDate);
                    endTime = format.format(selectedEndDate);
                    count = daysBetween(selectedStartDate, selectedEndDate);
                    Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.select_time_day), startTime, endTime, count), 1).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.arrow_left:
                calendarCenter.setText(calendar.clickLeftMonth());
                break;
            case R.id.arrow_right:
                calendarCenter.setText(calendar.clickRightMonth());
                break;
            default:
                break;
        }
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int daysBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days = Math.abs((time2 - time1)) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days)) + 1;
    }
}
