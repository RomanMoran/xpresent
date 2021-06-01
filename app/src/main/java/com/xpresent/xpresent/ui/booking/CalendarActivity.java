package com.xpresent.xpresent.ui.booking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.kizitonwose.calendarview.CalendarView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.ui.auth.AuthorizationActivity;
import org.jetbrains.annotations.NotNull;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;

public class CalendarActivity extends AppCompatActivity{
    private String sessionKey;
    private int orderSum, price_old;
    private String impressionName, human_name, duration_name;
    private CalendarView calendarView;
    private LocalDate selectedDate;
    private Spinner time;
    private String[] month_array;
    private boolean isDateChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        AndroidThreeTen.init(this); // for calendar

        // connect to app storage
        SharedPreferences settings = this.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        // get sessionKey and order params from storage
        sessionKey = settings.getString("sessionKey", "");
        // order info
        orderSum = settings.getInt("orderSum", 0);
        price_old = settings.getInt("price_old", 0);
        impressionName = settings.getString("itemName", "");
        human_name = settings.getString("human_name", "");
        duration_name = settings.getString("duration_name", "");

        setOrderData();

        ImageView backBtn = findViewById(R.id.iv_nav);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // change offer
        TextView changeOfferTv = findViewById(R.id.changeOffer);
        changeOfferTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // book button
        Button buttonConfirm = findViewById(R.id.btnConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickConfirm();
            }
        });
        // Time
        time = findViewById(R.id.time);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, config.TIME);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time.setAdapter(adapter);
        time.setSelection(4);
        // Calendar
        calendarView = findViewById(R.id.calendarView);
        final YearMonth currentMonth = YearMonth.now();
        long index = 1;
        calendarView.setup(currentMonth, currentMonth.plusMonths(6), DayOfWeek.MONDAY);
        calendarView.scrollToMonth(currentMonth);
        month_array = getResources().getStringArray(R.array.month_array);

        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NotNull
            @Override
            public DayViewContainer create(@NotNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NotNull DayViewContainer dayViewContainer, @NotNull CalendarDay calendarDay) {
                int day = calendarDay.getDate().getDayOfMonth();
                LocalDate today = LocalDate.now();
                dayViewContainer.textView.setText(Integer.toString(day));
                dayViewContainer.setCalendarDay(calendarDay);
                if (calendarDay.getOwner() == DayOwner.THIS_MONTH && !calendarDay.getDate().isBefore(today.plusDays(1))) {
                    if(selectedDate == calendarDay.getDate()){
                        dayViewContainer.view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        dayViewContainer.textView.setTextColor(Color.WHITE);
                    }
                    else{
                        dayViewContainer.view.setBackground(getResources().getDrawable(R.drawable.bg_day));
                        dayViewContainer.textView.setTextColor(Color.BLACK);
                    }
                } else {
                    dayViewContainer.textView.setTextColor(getResources().getColor(R.color.colorCardStroke));
                }
            }
        });
        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NotNull
            @Override
            public MonthViewContainer create(@NotNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NotNull MonthViewContainer monthViewContainer, @NotNull CalendarMonth calendarMonth) {
                final int month_index = calendarMonth.getYearMonth().getMonthValue();
                final int year = calendarMonth.getYearMonth().getYear();
                String title = month_array[month_index-1]+" "+year;
                monthViewContainer.textView.setText(title);

                monthViewContainer.nextMonthBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int next_month = month_index+1;
                        int next_year = year;
                        if(next_month > 12){
                            next_year++;
                            next_month -= 12;
                        }
                        YearMonth nextMonth = YearMonth.of(next_year, next_month);
                        calendarView.smoothScrollToMonth(nextMonth);
                    }
                });

                monthViewContainer.prevMonthBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int prev_month = month_index-1;
                        int prev_year = year;
                        if(prev_month <= 0){
                            prev_year--;
                            prev_month += 12;
                        }
                        YearMonth prevMonth = YearMonth.of(prev_year, prev_month);
                        calendarView.smoothScrollToMonth(prevMonth);
                    }
                });
            }
        });

    }

    public void onClickConfirm(){
        if(isDateChosen) {
            // record Order params to storage
            String formatDate = selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            SharedPreferences settings = getSharedPreferences("xp_client", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("selectedDateFormat", formatDate);
            editor.putString("selectedDate", selectedDate.toString());
            editor.putString("selectedTime", time.getSelectedItem().toString());
            editor.apply();
            //check authorization
            if (!sessionKey.isEmpty()) {
                Intent intent = new Intent(this, BookActivity.class);
                startActivity(intent);
            } else {
                Bundle extras = new Bundle();
                extras.putString("redirect", "book");
                Intent intent = new Intent(this, AuthorizationActivity.class);
                intent.putExtras(extras);
                startActivity(intent);
            }
        }
        else Toast.makeText(this, getResources().getString(R.string.choose_date), Toast.LENGTH_LONG).show();
    }

    private class DayViewContainer extends ViewContainer {
        private CalendarDay calendarDay;
        View view;
        TextView textView;

        DayViewContainer(@NotNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarDayText);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedDate = calendarDay.getDate();
                    calendarView.notifyCalendarChanged();
                    isDateChosen = true;
                }
            });
        }

        void setCalendarDay(CalendarDay calDay){
            calendarDay = calDay;
        }
    }

    private static class MonthViewContainer extends ViewContainer {
        View view;
        TextView textView;
        RelativeLayout nextMonthBtn, prevMonthBtn;

        MonthViewContainer(@NotNull View view) {
            super(view);
            this.view = view;
            textView = view.findViewById(R.id.calendarMonthText);
            nextMonthBtn = view.findViewById(R.id.calendar_next_button);
            prevMonthBtn = view.findViewById(R.id.calendar_prev_button);
        }
    }

    public void setOrderData(){
        TextView impOfferNameTxt = findViewById(R.id.impressionOfferName);
        TextView durationNameTxt = findViewById(R.id.duration_name);
        TextView humanNameTxt = findViewById(R.id.human_name);
        TextView impSumTxt = findViewById(R.id.impSum);
        TextView priceOldTxt = findViewById(R.id.info_price_old);

        String sum = orderSum+ " " +config.RUB;
        String priceOld = (price_old == 0) ? "" : price_old+ " " +config.RUB;
        impOfferNameTxt.setText(impressionName);
        durationNameTxt.setText(duration_name);
        humanNameTxt.setText(human_name);
        priceOldTxt.setText(priceOld);
        impSumTxt.setText(sum);
    }
}