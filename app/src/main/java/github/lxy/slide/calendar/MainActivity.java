package github.lxy.slide.calendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener{

    private SlideCalendarView calendar;
    private TextView calendarCenter;

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

        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");;
        try {
            calendar.setCalendarData(format.parse("2015-01-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendarCenter.setText(calendar.getYearAndmonth());
        calendar.setOnItemClickListener(new SlideCalendarView.OnItemClickListener() {
            @Override
            public void OnItemClick(Date selectedStartDate, Date selectedEndDate, Date downDate) {

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
}
