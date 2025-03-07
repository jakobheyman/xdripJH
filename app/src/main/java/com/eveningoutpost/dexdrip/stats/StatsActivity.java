package com.eveningoutpost.dexdrip.stats;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.Home;
import com.eveningoutpost.dexdrip.models.JoH;
import com.eveningoutpost.dexdrip.models.UserError.Log;
import com.eveningoutpost.dexdrip.R;
import com.eveningoutpost.dexdrip.utilitymodels.JamorhamShowcaseDrawer;
import com.eveningoutpost.dexdrip.utilitymodels.Pref;
import com.eveningoutpost.dexdrip.utilitymodels.ShotStateStore;
import com.eveningoutpost.dexdrip.utils.ActivityWithMenu;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.eveningoutpost.dexdrip.models.JoH.goFullScreen;
import static com.eveningoutpost.dexdrip.R.id.pager;

public class StatsActivity extends ActivityWithMenu {

    //public static final String MENU_NAME = "Statistics";
    public static final int TODAY = 0;
    public static final int YESTERDAY = 1;
    public static final int D7 = 2;
    public static final int D30 = 3;
    public static final int D90 = 4;
    public static final int DD = 5;
    public static int state = D7;
    public static GregorianCalendar date1;
    public static GregorianCalendar date2;
    private static boolean swipeInfoNotNeeded = false; // don't show info if already swiped after startup.
    StatisticsPageAdapter mStatisticsPageAdapter;
    ViewPager mViewPager;
    TextView[] indicationDots;
    private Button buttonTD;
    private Button buttonYTD;
    private Button button7d;
    private Button button30d;
    private Button button90d;
    private Button buttonDD;
    MenuItem menuItem;
    MenuItem menuItem2;
    private View decorView;
    private String stateString;
    private final static int MY_PERMISSIONS_REQUEST_STORAGE_SCREENSHOT = 106;
    private static final String SHOW_STATISTICS_FULL_SCREEN = "show_statistics_full_screen";
    public static final String SHOW_STATISTICS_PRINT_COLOR = "show_statistics_print_color";
    private static final String TAG = "Statistics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        evaluateColors(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        decorView = getWindow().getDecorView();
        assignButtonNames();
        initPagerAndIndicator();
        setButtonColors();
        registerButtonListeners();

        long tenDaysBack = System.currentTimeMillis()-1000*60*60*24*10; //ten days back in time
        long presentDay = System.currentTimeMillis(); //one days forward in time
        if (date1 == null) {
            date1 = new GregorianCalendar();
            date1.setTimeInMillis(tenDaysBack);
            date1.set(Calendar.HOUR_OF_DAY, 0);
            date1.set(Calendar.MINUTE, 0);
            date1.set(Calendar.SECOND, 0);
            date1.set(Calendar.MILLISECOND, 0);
        }
        if (date2 == null) {
            date2 = new GregorianCalendar();
            date2.setTimeInMillis(presentDay);
            date2.set(Calendar.HOUR_OF_DAY, 23);
            date2.set(Calendar.MINUTE, 59);
            date2.set(Calendar.SECOND, 59);
            date2.set(Calendar.MILLISECOND, 999);
        }

        if (JoH.ratelimit("statistics-startup",5)) showStartupInfo();

    }

    private void showStartupInfo() {
        // if (swipeInfoNotNeeded) {
        //    //show info only if user didn't swipe already.
        //    return;
        // }
        final boolean oneshot = true;
        final int option = Home.SHOWCASE_STATISTICS;
        if ((oneshot) && (ShotStateStore.hasShot(option))) return;


        // This could do with being in a utility static method also used in Home
        final int size1 = 50;
        final int size2 = 20;
        final String title = "Swipe for Different Reports";
        final String message = "Swipe left and right to see different report tabs.\n\nChoose time period for Today, Yesterday, 7 Days etc.\n\nFull screen mode, print colors and Sharing are supported from the butttons and 3 dot menu.";
        final ViewTarget target = new ViewTarget(R.id.button_stats_7d, this);
        final Activity activity = this;


        JoH.runOnUiThreadDelayed(new Runnable() {
                                     @Override
                                     public void run() {
                                         final ShowcaseView myShowcase = new ShowcaseView.Builder(activity)

                                                 .setTarget(target)
                                                 .setStyle(R.style.CustomShowcaseTheme2)
                                                 .setContentTitle(title)
                                                 .setContentText("\n" + message)
                                                 .setShowcaseDrawer(new JamorhamShowcaseDrawer(getResources(), getTheme(), size1, size2))
                                                 .singleShot(oneshot ? option : -1)
                                                 .build();
                                         myShowcase.setBackgroundColor(Color.TRANSPARENT);
                                         myShowcase.show();
                                     }
                                 }
                , 3000);

        // TextView tv = new TextView(this);
        //  tv.setText("Swipe left/right to switch between reports!");
        //  tv.setTextColor(Color.GREEN);
        //  tv.setTextSize(25);

        //for (int i = 0; i < 2; i++) {
        //Show toast twice the "long" period
        //       Toast toast = new Toast(getApplicationContext());
        //       toast.setView(tv);
        //       toast.setGravity(Gravity.CENTER, 0, 0);
        //        toast.show();
        //}
    }

    private void assignButtonNames() {
        buttonTD = (Button) findViewById(R.id.button_stats_today);
        buttonYTD = (Button) findViewById(R.id.button_stats_yesterday);
        button7d = (Button) findViewById(R.id.button_stats_7d);
        button30d = (Button) findViewById(R.id.button_stats_30d);
        button90d = (Button) findViewById(R.id.button_stats_90d);
        buttonDD = (Button) findViewById(R.id.button_stats_DD);
    }

    private void initPagerAndIndicator() {
        mStatisticsPageAdapter =
                new StatisticsPageAdapter(
                        getSupportFragmentManager());
        // set dots for indication
        indicationDots = new TextView[mStatisticsPageAdapter.getCount()];
        LinearLayout indicator = (LinearLayout) findViewById(R.id.indicator_layout);
        for (int i = 0; i < indicationDots.length; i++) {
            indicationDots[i] = new TextView(this);
            indicationDots[i].setText("\u25EF");
            indicationDots[i].setTextSize(12);
            indicator.addView(indicationDots[i], new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        indicationDots[0].setText("\u26AB");
        mViewPager = (ViewPager) findViewById(pager);
        mViewPager.setAdapter(mStatisticsPageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //if (position != 0) {
                //    swipeInfoNotNeeded = true;
                //}

                for (int i = 0; i < indicationDots.length; i++) {
                    indicationDots[i].setText("\u25EF"); //U+2B24
                }
                indicationDots[position].setText("\u26AB");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(2);
    }

    void setButtonColors() {

        switch (state)
        {
            case TODAY:
                stateString = "Today";
                break;
            case YESTERDAY:
                stateString = "Yesterday";
                break;
            case D7:
                stateString = "7 days";
                break;
            case D30:
                stateString = "30 days";
                break;
            case D90:
                stateString = "90 days";
                break;
            case DD:
                stateString = "Day to day";
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{0xFF606060});
            buttonTD.setBackgroundTintList(csl);
            buttonYTD.setBackgroundTintList(csl);
            button7d.setBackgroundTintList(csl);
            button30d.setBackgroundTintList(csl);
            button90d.setBackgroundTintList(csl);
            buttonDD.setBackgroundTintList(csl);
            csl = new ColorStateList(new int[][]{new int[0]}, new int[]{0xFFAA0000});
            switch (state) {
                case TODAY:
                    buttonTD.setBackgroundTintList(csl);
                    break;
                case YESTERDAY:
                    buttonYTD.setBackgroundTintList(csl);
                    break;
                case D7:
                    button7d.setBackgroundTintList(csl);
                    break;
                case D30:
                    button30d.setBackgroundTintList(csl);
                    break;
                case D90:
                    button90d.setBackgroundTintList(csl);
                    break;
                case DD:
                    buttonDD.setBackgroundTintList(csl);
                    break;
            }
        } else {

            buttonTD.setAlpha(0.5f);
            buttonYTD.setAlpha(0.5f);
            button7d.setAlpha(0.5f);
            button30d.setAlpha(0.5f);
            button90d.setAlpha(0.5f);
            buttonDD.setAlpha(0.5f);
            switch (state) {
                case TODAY:
                    buttonTD.setAlpha(1f);
                    break;
                case YESTERDAY:
                    buttonYTD.setAlpha(1f);
                    break;
                case D7:
                    button7d.setAlpha(1f);
                    break;
                case D30:
                    button30d.setAlpha(1f);
                    break;
                case D90:
                    button90d.setAlpha(1f);
                    break;
                case DD:
                    buttonDD.setAlpha(1f);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_statistics, menu);

        menuItem = menu.findItem(R.id.action_toggle_fullscreen);
        menuItem2 = menu.findItem(R.id.action_toggle_printing);

        updateMenuChecked();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) fullScreenHandler(false);

    }

    private void updateMenuChecked() {
        menuItem.setChecked(Pref.getBoolean(SHOW_STATISTICS_FULL_SCREEN, false));
        menuItem2.setChecked(Pref.getBoolean(SHOW_STATISTICS_PRINT_COLOR, false));
    }

    private void evaluateColors(boolean recreate) {
        if (Pref.getBooleanDefaultFalse(SHOW_STATISTICS_PRINT_COLOR)) {
            setTheme(R.style.StatisticsWhiteTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        if (recreate) recreate();
    }

    private void fullScreenHandler(boolean recreate)
    {
        goFullScreen(Pref.getBooleanDefaultFalse(SHOW_STATISTICS_FULL_SCREEN), decorView);
        if ((recreate) && (!Pref.getBooleanDefaultFalse(SHOW_STATISTICS_FULL_SCREEN))) recreate();
    }

    public void toggleStatisticsFullScreenMode(MenuItem m)
    {
        Pref.toggleBoolean(SHOW_STATISTICS_FULL_SCREEN);
        updateMenuChecked();
        fullScreenHandler(true);
    }
    public void toggleStatisticsPrintingMode(MenuItem m)
    {
        Pref.toggleBoolean(SHOW_STATISTICS_PRINT_COLOR);
        evaluateColors(true);
        updateMenuChecked();
    }
    public void statisticsDisableFullScreen(View v)
    {
        toggleStatisticsFullScreenMode(null);
    }
    public void  statisticsShare(View v)
    {
      statisticsShare((MenuItem)null);
    }

    public void statisticsShare(MenuItem m)
    {
        try {
        if (checkPermissions()) {
            final Activity context = this;
            JoH.runOnUiThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    View rootView = getWindow().getDecorView().findViewWithTag(mViewPager.getCurrentItem()); // search by tag :(
                    String file_name = "xDrip-Screenshot-" + JoH.dateTimeText(JoH.tsl()).replace(" ", "-").replace(":", "-").replace(".", "-") + ".png";
                    final String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Screenshots";
                    JoH.bitmapToFile(JoH.screenShot(rootView,"xDrip+ Statistics for "+stateString+"   @ "+JoH.dateText(JoH.tsl())), dirPath, file_name);
                    JoH.shareImage(context, new File(dirPath + "/" + file_name));
                }
            }, 250);

        }
        } catch (Exception e)
        {
            Log.e(TAG,"Got exception sharing statistics: "+e);
            JoH.static_toast_long("Got an error: "+e);
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE_SCREENSHOT);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE_SCREENSHOT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                statisticsShare((MenuItem)null);
            } else {
                JoH.static_toast_long(this, "Cannot save screenshot without permission");
            }
        }
    }



    private void registerButtonListeners() {

        View.OnClickListener myListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v == buttonTD) {
                    state = TODAY;
                } else if (v == buttonYTD) {
                    state = YESTERDAY;
                } else if (v == button7d) {
                    state = D7;
                } else if (v == button30d) {
                    state = D30;
                } else if (v == button90d) {
                    state = D90;
                } else if (v == buttonDD) {
                    setStartEndDates(v);
                    state = DD;
                }

                Log.d("DrawStats", "button pressed, invalidating");
                mStatisticsPageAdapter.notifyDataSetChanged();
                mViewPager.invalidate();
                setButtonColors();

            }
        };
        buttonTD.setOnClickListener(myListener);
        buttonYTD.setOnClickListener(myListener);
        button7d.setOnClickListener(myListener);
        button30d.setOnClickListener(myListener);
        button90d.setOnClickListener(myListener);
        buttonDD.setOnClickListener(myListener);


    }

    private void setStartEndDates(View myitem) {
        final Dialog dialog = new Dialog(StatsActivity.this);
        dialog.setContentView(R.layout.dialog_stats_dates);

        final Button startDateButton = (Button) dialog.findViewById(R.id.button_start_date);
        final Button endDateButton = (Button) dialog.findViewById(R.id.button_end_date);
        final Button updateDates = (Button) dialog.findViewById(R.id.button_update_dates);

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog1 = new DatePickerDialog(StatsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        date1.set(year, monthOfYear, dayOfMonth);
                    }
                }, date1.get(Calendar.YEAR), date1.get(Calendar.MONTH), date1.get(Calendar.DAY_OF_MONTH));
                dialog1.show();
            }
        });
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog2 = new DatePickerDialog(StatsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        date2.set(year, monthOfYear, dayOfMonth);
                    }
                }, date2.get(Calendar.YEAR), date2.get(Calendar.MONTH), date2.get(Calendar.DAY_OF_MONTH));
                dialog2.show();
            }
        });
        updateDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatisticsPageAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public String getMenuName() {
        return getString(R.string.statistics);
    }


    public class StatisticsPageAdapter extends FragmentStatePagerAdapter {
        public StatisticsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            switch (i) {
                case 0:
                    return new FirstPageFragment();
                case 1:
                    return new ChartFragment();
                default:
                    return new PercentileFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "General";
                case 1:
                    return "Range Pi Chart";
                default:
                    return "Percentile Chart";
            }
        }

        @Override
        public int getItemPosition(Object object) {
            // return POSITION_NONE to update/repaint the views if notifyDataSetChanged()+invalidate() is called
            return POSITION_NONE;
        }

    }

}
