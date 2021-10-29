
package ir.notopia.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;
import ir.mirrajabi.persiancalendar.PersianCalendarView;
import ir.mirrajabi.persiancalendar.core.PersianCalendarHandler;
import ir.mirrajabi.persiancalendar.core.interfaces.OnDayClickedListener;
import ir.mirrajabi.persiancalendar.core.models.PersianDate;
import ir.notopia.android.ar.ARActivity;
import ir.notopia.android.ar.CheckArFiles;
import ir.notopia.android.ar.WebViewActivity;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.menu.AboutActivity;
import ir.notopia.android.menu.SettingActivity;
import ir.notopia.android.menu.CategoryActivity;
import ir.notopia.android.menu.EditProfileActivity;
import ir.notopia.android.menu.SupportActivity;
import ir.notopia.android.scanner.opennotescanner.GalleryAdaptor;
import ir.notopia.android.scanner.opennotescanner.OpenNoteScannerActivity;
import ir.notopia.android.utils.Constants;
import ir.notopia.android.verification.CheckSignedIn;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.pushpole.sdk.PushPole;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private final String TAG = MainActivity.class.getName();
    private BottomSheetBehavior mBottomSheet;
    private ImageView darkness_view;
    private ImageView AddFloatingBtn,AiIconAnimation,SelfIcon, FiltersFloatingBtn;
    private LottieAnimationView ArIconAnimation;
    private ImageView removeFiltersIndicator;
    private AnimatedVectorDrawable animation;
    private ConstraintLayout constraintLayout;
    private ConstraintSet show_selected_label_constraint;
    private ConstraintSet box_year_constraint;
    private AppRepository mRepository;
    private List<ScanEntity> mScans;
    private GalleryAdaptor galleryAdaptor;
    private RecyclerView galleryRecycleView;
    BottomSheetSliderAdaptor bottomSheetSliderAdaptor;
    ViewPager viewPager;
    DrawerLayout mDrawerLayout;

    private String doHamedState;
    private String FilterCategory = "All";
    private long filterStartDate = -1;
    private long filterEndDate = -1;

    private PersianCalendarView persianCalendarView;
    private PersianCalendarHandler calendar;
    private PersianDate today;
    @SuppressLint("ClickableViewAccessibility")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNavigationViewListener();

        // check if login needed throw user to verification activity
        new CheckSignedIn(MainActivity.this);
        // init chategories for the first time
        new FirstCategoryInit(MainActivity.this);

        PushPole.initialize(this,true);


        SharedPreferences helpScan = MainActivity.this.getSharedPreferences("helpScan", Context.MODE_PRIVATE);

        SharedPreferences defainHamed = MainActivity.this.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        defainHamed.edit().putString("doHamedState","0").apply();

        SharedPreferences mahsol = MainActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
        String mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

        SharedPreferences login = MainActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);
        String userName = login.getString("USER_NAME_PR", null);
        String userFamily = login.getString("USER_FAMILY_PR", null);



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView TVUserNumber = headerView.findViewById(R.id.MenuUserNumber);
        TVUserNumber.setText("+98" + userNumber);
        TextView TVUserFullName = headerView.findViewById(R.id.MenuUserFullName);
        TVUserFullName.setText(userName + " " + userFamily);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        removeFiltersIndicator = findViewById(R.id.remove_filters_indicator);
        ArIconAnimation = findViewById(R.id.ar_icon_animation);
        AiIconAnimation = findViewById(R.id.ai_icon_animation);
        AddFloatingBtn = findViewById(R.id.add_floating_btn);
        FiltersFloatingBtn = findViewById(R.id.filter_floating_btn);
        SelfIcon = findViewById(R.id.shelf_icon);



        constraintLayout = findViewById(R.id.mainL);
        bottomSheetSliderAdaptor = new BottomSheetSliderAdaptor(this);
        viewPager = findViewById(R.id.SliderViewPager);
        viewPager.setAdapter(bottomSheetSliderAdaptor);

        try {
            loadGallery(FilterCategory,filterStartDate,filterEndDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        TextView label5 = findViewById(R.id.bottomSheet_label5);
        TextView label3 = findViewById(R.id.bottomSheet_label3);
        TextView label2 = findViewById(R.id.bottomSheet_label2);

        //AddFloatingBtn,ArIconAnimation,SelfIcon
        View[] views = {AddFloatingBtn,FiltersFloatingBtn,ArIconAnimation,SelfIcon};
        startShowCase(MainActivity.this,views,"ShowCaseMain15");


        label5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewPager.setCurrentItem(0);
                changeSelectedLabelShowerSize(label5);
            }
        });
        label3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewPager.setCurrentItem(1);
                changeSelectedLabelShowerSize(label3);
            }
        });
        label2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // GO TO PAGE DATE
                viewPager.setCurrentItem(2);
                // EXPAND DATE PAGE ITEMS TO TAGHVIM
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    expanding_date_layout(1);
                }
                else{
                    expanding_date_layout(0);
                }
                // RESIZE THE SHOWER PAGE SELECTED
                changeSelectedLabelShowerSize(label2);
            }
        });

        SelfIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(SelfIcon);
                Intent intentShelf = new Intent(MainActivity.this, ShelfsActivity.class);
                startActivity(intentShelf);
            }
        });



        removeFiltersIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.imageViweFadeOutFadeIn(removeFiltersIndicator);

                FilterCategory = "All";
                filterStartDate = -1;
                filterEndDate = -1;
                try {
                    loadGallery(FilterCategory,filterStartDate,filterEndDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(today != null){
                    persianCalendarView.update();
                    persianCalendarView.goToDate(today);
                }
            }
        });

        ArIconAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(ArIconAnimation);

                if(!mahsolCode.equals("")) {
                    boolean isOkArFiles = new CheckArFiles(MainActivity.this).isOkArFiles();
                    if(isOkArFiles) {
                        Intent intentAr = new Intent(MainActivity.this, ARActivity.class);
                        startActivity(intentAr);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"لطفا یک محصول اضافه کنید",Toast.LENGTH_SHORT).show();
                }
            }
        });

        AddFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(AddFloatingBtn);

                String helpScanState = helpScan.getString("helpScanState", "true");

                if(!mahsolCode.equals("")) {

                    if(helpScanState.equals("true")){
                        Intent intentAi = new Intent(MainActivity.this, HelpScanActivity.class);
                        startActivity(intentAi);
                    }
                    else{
                        Intent intentAi = new Intent(MainActivity.this, OpenNoteScannerActivity.class);
                        startActivity(intentAi);
                    }


                }
                else{
                    Toast.makeText(MainActivity.this,"لطفا یک محصول اضافه کنید",Toast.LENGTH_SHORT).show();
                }

            }
        });

        ImageView VoiceIcon = findViewById(R.id.voice_icon);
        VoiceIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(VoiceIcon);
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                int currentTextView;
                switch (position){
                    case 1:

                        loadTedadCategory();

                        ImageView dasteBandiAddIcon = findViewById(R.id.dastebandi_add_icon);
                        dasteBandiAddIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageViweFadeOutFadeIn(dasteBandiAddIcon);
                            }
                        });

                        if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                            expanding_progress_layout(1);

                            ImageView dasteBandiEditIcon = findViewById(R.id.dastebandi_edit_icon);
                            dasteBandiEditIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(dasteBandiEditIcon);
                                }
                            });

                            ImageView DasteBandiRemoveIcon = findViewById(R.id.dastebandi_remove_icon);
                            DasteBandiRemoveIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(DasteBandiRemoveIcon);
                                }
                            });
                        }
                        else{
                            expanding_progress_layout(0);
                        }
                        currentTextView = R.id.bottomSheet_label3;

                        List<CategoryEntity> mCategorys = mRepository.getCategorys();

                        for(int i = 1;i < 7;i++){
                            String strId = "back_category" + i;
                            String strIdText = "LabelDaste" + i;
                            int id = MainActivity.this.getResources().getIdentifier(strId,"id",MainActivity.this.getPackageName());
                            int idTX = getResources().getIdentifier(strIdText,"id",getPackageName());
                            int identifier = i;
                            ImageView imageView = findViewById(id);

                            int identifierText = 5 - (i - 1);
                            TextView textView = findViewById(idTX);
                            textView.setText(mCategorys.get(identifierText).getName());

                            int category = 7 - identifier;

                            if (!FilterCategory.equals("All")){
                                if(i ==  (7 - Integer.valueOf(FilterCategory))){
                                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_selected_category, getApplicationContext().getTheme()));
                                }
                            }
                            else
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_dastebandi_item, getApplicationContext().getTheme()));

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if(FilterCategory.equals(String.valueOf(category))) {
                                        try {
                                            loadGallery("All", filterStartDate, filterEndDate);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else{
                                        FilterCategory = String.valueOf(category);
                                        try {
                                            loadGallery(FilterCategory, filterStartDate, filterEndDate);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Log.w("category clicked:",String.valueOf(category));
                                }
                            });
                        }



                        break;
                    case 2:

                        mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                        expanding_date_layout(1);

                        currentTextView = R.id.bottomSheet_label2;

                        persianCalendarView = findViewById(R.id.filter_calendar);
                        calendar = persianCalendarView.getCalendar();
                        today = calendar.getToday();

                        calendar.setOnDayClickedListener(new OnDayClickedListener() {
                            @SuppressLint("UseCompatLoadingForColorStateLists")
                            @Override
                            public void onClick(PersianDate date) {

                                TextView TVIndicatorEndDate = findViewById(R.id.TVIndicatorEndDate);
                                TextView TVIndicatorStartDate = findViewById(R.id.TVIndicatorStartDate);

                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                                try {

                                    String str = (String)(date.getYear() + "/" + date.getMonth() + "/" + date.getDayOfMonth());
                                    long timeStep = ((Date)formatter.parse(str)).getTime();

                                    if(filterStartDate == -1){
                                        calendar.setSelectedDayBackground(R.drawable.vc_back_current_day_start);
                                        TVIndicatorStartDate.setText(str);
                                        filterStartDate = timeStep;

//                                        Toast.makeText(MainActivity.this,"تاریخ شروع انتخاب شد",Toast.LENGTH_SHORT).show();
                                    }
                                    else if(filterEndDate == -1){

                                        calendar.setSelectedDayBackground(R.drawable.vc_back_current_day_end);
                                        if(timeStep > filterStartDate) {
                                            TVIndicatorEndDate.setText(str);
                                            filterEndDate = timeStep;
                                        }
                                        else{
                                            Toast.makeText(MainActivity.this,"تاریخ پایان باید بعد از تاریخ شروع باشد",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        calendar.setSelectedDayBackground(R.drawable.vc_back_current_day_start);
                                        TVIndicatorStartDate.setText(str);
                                        filterStartDate = timeStep;
                                        filterEndDate = -1;

//                                        Toast.makeText(MainActivity.this,"تاریخ شروع انتخاب شد",Toast.LENGTH_SHORT).show();
                                    }

                                    loadGallery(FilterCategory,filterStartDate,filterEndDate);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if(date.getDayOfMonth() == today.getDayOfMonth() && date.getDayOfWeek() == today.getDayOfWeek()){
                                    calendar.setTodayBackground(R.drawable.vc_back_current_day);
                                }
                                else{
                                    calendar.setTodayBackground(R.drawable.vc_back_item_date_today);
                                }

                            }
                        });

                        break;
                    default:

                        ImageView dashboardAddIcon2 = findViewById(R.id.dashboard_add_icon);
                        dashboardAddIcon2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageViweFadeOutFadeIn(dashboardAddIcon2);
                            }
                        });

                        if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                            expanding_progress_layout(1);

                            ImageView dashboardEditIcon = findViewById(R.id.dashboard_edit_icon);
                            dashboardEditIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(dashboardEditIcon);
                                }
                            });

                            ImageView dashboardRemoveIcon = findViewById(R.id.dashboard_remove_icon);
                            dashboardRemoveIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(dashboardRemoveIcon);
                                }
                            });
                        }
                        else{
                            expanding_progress_layout(0);
                        }
                        currentTextView = R.id.bottomSheet_label5;
                        break;
                }

                //change Constraint of shower selected label
                show_selected_label_constraint = new ConstraintSet();
                show_selected_label_constraint.clone(constraintLayout);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.START, currentTextView, ConstraintSet.START, 0);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.END, currentTextView, ConstraintSet.END, 0);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.BOTTOM, currentTextView, ConstraintSet.BOTTOM, 0);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.TOP, currentTextView, ConstraintSet.TOP, 0);
                TransitionManager.beginDelayedTransition(constraintLayout);
                show_selected_label_constraint.applyTo(constraintLayout);

                //change width of shower selected label
                TextView tempLabel = findViewById(currentTextView);
                changeSelectedLabelShowerSize(tempLabel);

                //make all bottom sheet textView Labels whites to black
                int[] arr = new int[]{R.id.bottomSheet_label2, R.id.bottomSheet_label3,R.id.bottomSheet_label5};
                for (int value : arr) {
                    if (value != currentTextView) {
                        TextView tempBlacker = findViewById(value);
                        if (tempBlacker.getCurrentTextColor() != Color.BLACK)
                            colorChangerTextView(tempBlacker, "#FFFFFF", "#000000");
                    }
                }
                //make current bottom sheet textView Labels black to white
                colorChangerTextView(tempLabel,"#000000","#FFFFFF");

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        FiltersFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // fade out/in animation
                imageViweFadeOutFadeIn(FiltersFloatingBtn);

                // COLLAPSED/Hide bottom sheet
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else if(mBottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        View bottomsheet = findViewById(R.id.bottom_sheet);
        mBottomSheet = BottomSheetBehavior.from(bottomsheet);
        mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        moveFloatingActionBtn(-1);
        darkness_view = findViewById(R.id.img_darkness);
        mBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                Log.w("slide:",""+slideOffset);

                runFadeKoli(slideOffset);
                moveFloatingActionBtn(slideOffset);
                expanding_date_layout(slideOffset);
                expanding_progress_layout(slideOffset);
                if (slideOffset <= 0) {
                    darkness_view.setVisibility(View.INVISIBLE);

                } else {
                    darkness_view.setVisibility(View.VISIBLE);
                }
            }
        });

        //open and close bottomsheet with click on the takht btn gray
        ImageView btn_BottomSheet_takht = findViewById(R.id.img_takhtBottomShape);
        btn_BottomSheet_takht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // COLLAPSED/Expand bottom sheet
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        ImageView iv_menu = findViewById(R.id.icon_menu);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(iv_menu);
                mDrawerLayout.openDrawer(GravityCompat.START);


            }

        });

        //if click on darkness bottomsheet state set to STATE_COLLAPSED
        darkness_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // COLLAPSED  bottom sheet
                mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

    }

    static void startShowCase(Activity activity, View[] views, String SHOWCASE_ID) {

        // Views:
        // 0) AddFloatingBtn
        // 1) FiltersFloatingBtn
        // 2) ArIconAnimation
        // 3) SelfIcon

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(views[0],"\"نوتون\" های خودت را ذخیره کن:\n" +
                "هر وقت چیزی نوشتی ازش یه \"نوتون\" بگیر!\n" +
                "برای اینکه بتونی چیزی رو که نوشتی تبدیل به \"نوتون\" کنی تا بعداً از امکاناتش استفاده کنی این دکمه را بزن و طبق راهنما گوشیت را روی صفحه نگه دار و صبر کن تا نوتوپیا با فوت و فنی که خودش بلده از صفحه عکس بگیره و \"نوتون\" جدیدی برات ایجاد کنه  \n" +
                "\"نوتون\" های جدیدت رو میتونی تو همین صفحه اصلی برنامه ببینی. ", "متوجه شدم");

        sequence.addSequenceItem(views[1],"دنبال چی هستی؟\n" +
                "هر وقت نیاز داشتی تا بین \"نوتون\" هات چیزی را پیدا کنی این دکمه رو بزن. فقط کافیه بگی دنبال چی هستی تا خیلی زود برات پیداش کنه.\n" +
                " \"نوتون\" ها بر اساس پارامترهای مختلفی مثل تاریخ، دسته بندی و نوع نوشت افزارت قابلیت فیلتر شدن دارن و نمایش \"نوتون\" ها تو گالری هم بر اساس همین فیلترها اتفاق می افته.", "متوجه شدم");

        sequence.addSequenceItem(views[2],
                "\"چو در جلوه آیی جهان دیدنیست\"\n" +
                        "هرجایی از نوشت افزارهای نوتوپیا که این علامت وجود داشت، چیزی بیش از اونچه که می بینید در اون نهفته است. برای کشف اون، این دکمه را بزن و گوشیت رو روی صفحه نگه دار.\n", "متوجه شدم");

        sequence.addSequenceItem(views[3],
                "برای مدیریت نوشت افزار های نوتوپیا از این قسمت وارد شو، اینجا می تونی انواع نوشت افزارهایی که تهیه کردی رو مشاهده و مدیریت کنی.", "متوجه شدم");

        sequence.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        doHamedLoading();
    }

    @Override
    protected void onStart() {
        super.onStart();


        // at first when app starts darkness set to invisible
        darkness_view.setVisibility(View.INVISIBLE);

        //start animation icons
        Drawable drawable_ar = ArIconAnimation.getDrawable();
        Drawable drawable_ai = AiIconAnimation.getDrawable();

        if (drawable_ar instanceof AnimatedVectorDrawableCompat && drawable_ai instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd_ar = (AnimatedVectorDrawableCompat) drawable_ar;
            AnimatedVectorDrawableCompat avd_ai = (AnimatedVectorDrawableCompat) drawable_ai;

            //  start icon animations
            avd_ar.start();
            avd_ai.start();

            //  repeat icon animations
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avd_ar.start();
                            avd_ai.start();
                        }
                    });
                }
            },0,8000);

        }
        if (drawable_ar instanceof AnimatedVectorDrawable && drawable_ai instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd_ar = (AnimatedVectorDrawable) drawable_ar;
            AnimatedVectorDrawable avd_ai = (AnimatedVectorDrawable) drawable_ai;

            //  start icon animations
            avd_ar.start();
            avd_ai.start();

            //  repeat icon animations
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avd_ar.start();
                            avd_ai.start();
                        }
                    });
                }
            },0,7000);
        }
    }


    private void expanding_date_layout(float number){

        //expanded date bottom sheet view
        if(viewPager.getCurrentItem() == 2) {
            if(number >= 0) {
                LinearLayout box_year = findViewById(R.id.box_year);
                float scale = box_year.getResources().getDisplayMetrics().density;
                box_year.getLayoutParams().height = (int) ((40 * scale + 0.5f) * number);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) box_year.getLayoutParams();
                params.topMargin = (int) ((10 * scale + 0.5f) * number);
                box_year.setLayoutParams(box_year.getLayoutParams());

            }
        }
    }

    private void expanding_progress_layout(float number){
        //expanded date bottom sheet view
        if(viewPager.getCurrentItem() != 1 && viewPager.getCurrentItem() != 3 ) {
            if(number >= 0) {
                LinearLayout progress_linearLayout;

                if(viewPager.getCurrentItem() == 2){
                    progress_linearLayout = findViewById(R.id.progress_linearLayout_dastebandi);

                    ImageView dasteBandiEditIcon = findViewById(R.id.dastebandi_edit_icon);
                    dasteBandiEditIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dasteBandiEditIcon);
                        }
                    });

                    ImageView dasteBandiRemoveIcon = findViewById(R.id.dastebandi_remove_icon);
                    dasteBandiRemoveIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dasteBandiRemoveIcon);
                        }
                    });

                }
                else{
                    progress_linearLayout = findViewById(R.id.progress_linearLayout_dashboard);

                    ImageView dashboardBandiEditIcon = findViewById(R.id.dashboard_edit_icon);
                    dashboardBandiEditIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dashboardBandiEditIcon);
                        }
                    });


                    ImageView dashboardBandiAddIcon = findViewById(R.id.dashboard_add_icon);
                    dashboardBandiAddIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dashboardBandiAddIcon);
                        }
                    });

                    ImageView dashboardBandiRemoveIcon = findViewById(R.id.dashboard_remove_icon);
                    dashboardBandiRemoveIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dashboardBandiRemoveIcon);
                        }
                    });
                }
                float scale = progress_linearLayout.getResources().getDisplayMetrics().density;
                progress_linearLayout.getLayoutParams().height = (int) ((46 * scale + 0.5f) * number) + (int)(17 * scale + 0.5f);
                progress_linearLayout.setLayoutParams(progress_linearLayout.getLayoutParams());
            }
        }
    }


    private void moveFloatingActionBtn(float number) {
        if(number <= 0){
            float TempNumber = 1 + number;
            float scale = AddFloatingBtn.getResources().getDisplayMetrics().density;
            ViewGroup.MarginLayoutParams add_params = (ViewGroup.MarginLayoutParams) AddFloatingBtn.getLayoutParams();
            add_params.bottomMargin = (int) ((115 * scale + 0.5f) * TempNumber) + (int) ((16 * scale + 0.5f)) ;
            AddFloatingBtn.setLayoutParams(AddFloatingBtn.getLayoutParams());

            ImageView FillterFloatingBtn = findViewById(R.id.filter_floating_btn);
            ViewGroup.MarginLayoutParams fillter_params = (ViewGroup.MarginLayoutParams) FillterFloatingBtn.getLayoutParams();
            fillter_params.bottomMargin = (int) ((115 * scale + 0.5f) * TempNumber) + (int) ((16 * scale + 0.5f)) ;
            FillterFloatingBtn.setLayoutParams(FillterFloatingBtn.getLayoutParams());
        }
    }

    //fading darkness function for bottomsheet
    private void runFadeKoli(float number) {

        if(number > 0) {
            //runFadeKoli
            darkness_view.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(darkness_view, "alpha", number);
            fadeIn.setDuration(0);
            final AnimatorSet mAnimationSet = new AnimatorSet();
            mAnimationSet.play(fadeIn);
            mAnimationSet.start();
        }
    }

    public static void imageViweFadeOutFadeIn(View tempView) {


        float FadeFirstAlphaNum  = (float) 1;
        float FadeSecondAlphaNum  = (float) 0.4;

        ObjectAnimator fadeOutAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeFirstAlphaNum, FadeSecondAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOutAnimation);
        mAnimationSet.start();

        ObjectAnimator fadeInAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeSecondAlphaNum, FadeFirstAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet nAnimationSet = new AnimatorSet();
        nAnimationSet.play(fadeInAnimation);
        nAnimationSet.start();
    }

    public static void TextViweFadeOutFadeIn(TextView tempView) {

        float FadeFirstAlphaNum  = (float) 1;
        float FadeSecondAlphaNum  = (float) 0.4;

        ObjectAnimator fadeOutAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeFirstAlphaNum, FadeSecondAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOutAnimation);
        mAnimationSet.start();

        ObjectAnimator fadeInAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeSecondAlphaNum, FadeFirstAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet nAnimationSet = new AnimatorSet();
        nAnimationSet.play(fadeInAnimation);
        nAnimationSet.start();
    }

    private void changeSelectedLabelShowerSize(TextView TempLabel){

        int size = TempLabel.getMeasuredWidth() + 55;
        ImageView showerPage = findViewById(R.id.ShowerLableState);
        showerPage.getLayoutParams().width = size;
        showerPage.setLayoutParams(showerPage.getLayoutParams());
    }

    private void colorChangerTextView(TextView tempLabel,String sColor,String eColor){

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setDuration(350);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float fractionAnim = (float) valueAnimator.getAnimatedValue();

                tempLabel.setTextColor(ColorUtils.blendARGB(Color.parseColor(sColor)
                        , Color.parseColor(eColor)
                        , fractionAnim));
            }
        });
        valueAnimator.start();
    }

    private void loadTedadCategory(){
        if(viewPager.getCurrentItem() == 1){
            List<CategoryEntity> mCategorys = mRepository.getCategorys();
            List<ScanEntity> mScans = mRepository.getScans();
            for(int i = 1;i < 7;i++){
                int identifier = i - 1;
                int counter = 0;
                String categoryId = String.valueOf(7 - mCategorys.get(identifier).getId());
                String strId = "LabelTedadDaste" + i;
                int id = getResources().getIdentifier(strId,"id",getPackageName());
                TextView textView = findViewById(id);

                for (int a = 0; a < mScans.size(); a++) {
                    ScanEntity obj = mScans.get(a);

                    Log.d("tededCheck_ScanCT",obj.getCategory());
                    Log.d("tededCheck_mCateId",categoryId);

                    if (obj.getCategory().equals(categoryId)) {
                        counter++;
                    }
                }
                String matn = "تعداد : " + counter;
                textView.setText(matn);

            }
        }
    }

    private void loadGallery(String category,long filterStartDate,long filterEndDate) throws ParseException {

        loadTedadCategory();

        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

        TextView TVIndicatorCategory = findViewById(R.id.TVIndicatorCategory);
        TextView TVIndicatorStartDate = findViewById(R.id.TVIndicatorStartDate);
        TextView TVIndicatorEndDate = findViewById(R.id.TVIndicatorEndDate);



        LinearLayout box_filter = findViewById(R.id.box_filter_indicator);
        float scale = box_filter.getResources().getDisplayMetrics().density;
        LayoutTransition layoutTransition = box_filter.getLayoutTransition();
        int sizeInDp;
        if(filterStartDate != -1 || filterEndDate != -1 || !category.equals("All")) {
            sizeInDp = 60;
        }
        else {
            sizeInDp = 0;
        }

        if(box_filter.getLayoutParams().height > 0){

            layoutTransition.setDuration(250); // Change duration
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            box_filter.getLayoutParams().height = (int) ((0 * scale + 0.5f));
            box_filter.setLayoutParams(box_filter.getLayoutParams());
            box_filter.requestLayout();

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layoutTransition.setDuration(250); // Change duration
                    layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                    box_filter.getLayoutParams().height = (int) ((sizeInDp * scale + 0.5f));
                    box_filter.setLayoutParams(box_filter.getLayoutParams());
                    box_filter.requestLayout();
                }
            }, 250);

        }
        else{
            layoutTransition.setDuration(250); // Change duration
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            box_filter.getLayoutParams().height = (int) ((sizeInDp * scale + 0.5f));
            box_filter.setLayoutParams(box_filter.getLayoutParams());
            box_filter.requestLayout();
        }






        if(filterStartDate == -1){
            TVIndicatorStartDate.setText("-");
        }
        if(filterEndDate == -1){
            TVIndicatorEndDate.setText("-");
        }


        if(category.equals("All")){
            TVIndicatorCategory.setText("همه");


            if(viewPager.getCurrentItem() == 1) {
                for (int i = 1; i < 7; i++) {
                    String strId = "back_category" + i;
                    int id = MainActivity.this.getResources().getIdentifier(strId, "id", MainActivity.this.getPackageName());
                    int identifier = i;
                    ImageView imageView = findViewById(id);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_dastebandi_item, getApplicationContext().getTheme()));
                }
            }

        }
        else{

            CategoryEntity categoryEntity = mRepository.getCategoryById(Integer.valueOf(category));
            TVIndicatorCategory.setText(categoryEntity.getName());

            if(viewPager.getCurrentItem() == 1) {
                for (int i = 1; i < 7; i++) {
                    String strId = "back_category" + i;
                    int id = MainActivity.this.getResources().getIdentifier(strId, "id", MainActivity.this.getPackageName());
                    int identifier = i;
                    ImageView imageView = findViewById(id);

                    if (i == (7 - Integer.valueOf(category)))
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_selected_category, getApplicationContext().getTheme()));
                    else
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_dastebandi_item, getApplicationContext().getTheme()));
                }
            }

        }

        FilterCategory = category;

        mRepository = AppRepository.getInstance(MainActivity.this);
        mScans = mRepository.getScans();


        List<ScanEntity> tempScans = new ArrayList<>();
        ArrayList<String> ab = new ArrayList<>();

        if(true){

            for (int i = 0; i < mScans.size(); i++) {

                ScanEntity obj = mScans.get(i);
                boolean check = false;
                String str = (String)(obj.getYear() + "/" + obj.getMonth() + "/" + obj.getDay());
                long date = ((Date)formatter.parse(str)).getTime();

                Log.d("dddddd i:",String.valueOf(i));

                if (obj.getCategory().equals(category)) {
                    check = true;
                }
                else if(category.equals("All")){
                    check = true;
                }
                else{
                    check = false;
                }

                if(check){

                    if(filterStartDate != -1){
                        if(date < filterStartDate) {
                            check = false;
                        }
                    }
                }

                if(check){
                    if (filterEndDate != -1){
                        if(date > filterEndDate){
                            check = false;
                        }
                    }
                }

                Log.d(TAG + "Filter number:",String.valueOf(i));
                Log.d(TAG + "Filter Date:",str);
                Log.d(TAG + "Filter Date:",String.valueOf(date));
                Log.d(TAG + "Filter DateStart:",String.valueOf(filterStartDate));
                Log.d(TAG + "Filter DateEnd:",String.valueOf(filterEndDate));
                Log.d(TAG + "Filter DateEnd:",String.valueOf(check));
                Log.d(TAG + "Filter DateEnd:",String.valueOf(" "));


                if(check)
                    tempScans.add(obj);

            }

        }
        else{
            tempScans = mScans;
        }

        galleryAdaptor = new GalleryAdaptor(tempScans,MainActivity.this);
        // new Utils(getApplicationContext()).getFilePaths(););

        galleryRecycleView = (DragSelectRecyclerView) findViewById(R.id.GalleryRecyclerview);
        galleryRecycleView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryRecycleView.setAdapter(galleryAdaptor);


    }


    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }



    private void doHamedLoading(){

        SharedPreferences doHamed = MainActivity.this.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        doHamedState = doHamed.getString("doHamedState", "0");

        LinearLayout LLContentDoHamed = findViewById(R.id.LLContentDoHamed);
        ImageView DarkdoHamed = findViewById(R.id.DarkdoHamed);


        if(doHamedState.equals("1")){
            LLContentDoHamed.setVisibility(View.VISIBLE);
            DarkdoHamed.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    doHamedLoading();
                }
            },
            500);
        }
        else{
            LLContentDoHamed.setVisibility(View.GONE);
            DarkdoHamed.setVisibility(View.GONE);
            try {
                loadGallery(FilterCategory,filterStartDate,filterEndDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);





    }
}