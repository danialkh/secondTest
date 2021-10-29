package ir.notopia.android.scanner.opennotescanner;

/*
 * based on code originally at http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.PagerAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ir.hamsaa.persiandatepicker.Listener;
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.opennotescanner.helpers.Utils;

public class FullScreenImageAdapter extends PagerAdapter{

    private static final String TAG = "FullScreenImageAdapter";
    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private int maxTexture;
    private ImageLoader mImageLoader;
    private ImageSize mTargetSize;
    private Executor executor = Executors.newSingleThreadExecutor();
    private List<ScanEntity> mScans;
    private AppRepository mRepository;
    private PersianCalendar initDate;
    private TextView docDate;
    private ImageView docRotate;
    private ImageView docSave;
    private ImageView docDelete;
    private String date;
    private Spinner docCategory;
    private String mCategory;
    private String mDay;
    private String mMonth;
    private String mYear;
    private String mQrCode;
    private PersianDatePickerDialog picker;
    private float rotate = 0;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  List<ScanEntity> mScans) {
        this._activity = activity;
        this.mScans = mScans;
        mRepository = AppRepository.getInstance(_activity);


    }


    private static double Log(double n, double base) {
        return Math.log(n) / Math.log(base);
    }

    @Override
    public int getCount() {
        return this.mScans.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        TouchImageView imgDisplay;
        rotate = 0;
        LayoutInflater inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);
        ScanEntity mScan = mScans.get(position);
        Log.d(TAG, "mScan: " + mScan.toString());
        mCategory = mScan.getCategory();
        mYear = mScan.getYear();
        mMonth = mScan.getMonth();
        mDay = mScan.getDay();
        date = mYear + "/" + mMonth + "/" + mDay;
        Log.d(TAG, "mScan: " + date);

        imgDisplay =  viewLayout.findViewById(R.id.imgDisplay);
        docCategory = viewLayout.findViewById(R.id.doc_category);
        docDate = viewLayout.findViewById(R.id.doc_date_test);
        docRotate = viewLayout.findViewById(R.id.doc_rotate);
        docSave = viewLayout.findViewById(R.id.doc_save);
        docDelete = viewLayout.findViewById(R.id.doc_delete);

        List<CategoryEntity> mCategorys = mRepository.getCategorys();
        String[] categories = {
                mCategorys.get(5).getName(),
                mCategorys.get(4).getName(),
                mCategorys.get(3).getName(),
                mCategorys.get(2).getName(),
                mCategorys.get(1).getName(),
                mCategorys.get(0).getName()
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(_activity,
                android.R.layout.simple_spinner_item, categories);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        docCategory.setAdapter(adapter);

        docCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub
                String selCat = docCategory.getItemAtPosition(arg2).toString();
                int index = -1;
                for (int i=0;i < categories.length;i++) {
                    if (categories[i].equals(selCat)) {
                        index = i;
                        break;
                    }
                }
                index++;
                mScan.setCategory(String.valueOf(index));
                mRepository.updateScan(mScan);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        int index = Integer.parseInt(mCategory) - 1;

        docCategory.setSelection(index);
        docDate.setText(date);
        Log.d(TAG, "instantiateItem: position " + position + " scanId " + mScan.getId());
        initDate = new PersianCalendar();
        docDate.setOnClickListener(v -> {
            showCalendar(mScan, position, viewLayout);
        });


        String imagePath = mScan.getImage();
//        docRotate.setOnClickListener(v ->
//        {
//            rotate = rotate == 270 ? 0 : rotate + 90;
//            imgDisplay.setRotation(rotate);
//            imgDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
//
//
//        });
//        docSave.setOnClickListener(v ->
//        {
//            rotateImageByUri(imagePath, rotate);
//
//
//        });
        mImageLoader.displayImage("file:///" + imagePath, imgDisplay, mTargetSize);


        docDelete.setOnClickListener(v -> {

            AlertDialog.Builder deleteConfirmBuilder;

            deleteConfirmBuilder = new AlertDialog.Builder(_activity,R.style.AlertDialogCustom);
            deleteConfirmBuilder.setTitle(_activity.getResources().getString(R.string.confirm_title));
            deleteConfirmBuilder.setMessage(_activity.getResources().getString(R.string.confirm_delete_text));
            deleteConfirmBuilder.setPositiveButton(_activity.getResources().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    deleteImage(imagePath);
                    dialog.dismiss();
                }

            });


            deleteConfirmBuilder.setNegativeButton(_activity.getResources().getString(R.string.answer_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            deleteConfirmBuilder.create().show();


        });

        container.addView(viewLayout);

        return viewLayout;
    }

    private void deleteImage(String filePath) {

        final File photoFile = new File(filePath);

        photoFile.delete();
        Utils.removeImageFromGallery(filePath, _activity);
        mRepository.deleteScanByImage(filePath);

        Intent intentGallery = new Intent(_activity, MainActivity.class);
        _activity.startActivity(intentGallery);
    }

    private void showCalendar(ScanEntity scan, int pos, View viewLayout) {
        Typeface typeface = Typeface.createFromAsset(_activity.getAssets(), "fonts/B_Yekan.ttf");

        initDate.setPersianDate(Integer.parseInt(scan.getYear()), Integer.parseInt(scan.getMonth()), Integer.parseInt(scan.getDay()));
        Log.d(TAG, "instantiateItem: position " + pos + " in date scanID: " + scan.getId());
        Log.d(TAG, "instantiateItem:" + scan.getId());


        Log.d(TAG,"manoMige:"+scan.toString());

        picker = new PersianDatePickerDialog(_activity)
                .setPositiveButtonString("باشه")
                .setNegativeButton("بیخیال")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setMinYear(1397)
                .setMaxYear(1400)
//                .setMaxYear(PersianDatePickerDialog.THIS_YEAR)
                .setInitDate(initDate)
                .setActionTextColor(Color.GRAY)
                .setTypeFace(typeface)
                .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                .setShowInBottomSheet(false)
                .setListener(new Listener() {
                    @Override
                    public void onDateSelected(PersianCalendar persianCalendar) {
                        Log.d(TAG, "instantiateItem: position " + pos + " in date scanID: " + scan.getId());
                        TextView myTvDate = viewLayout.findViewById(R.id.doc_date_test);
                        mDay = "" + persianCalendar.getPersianDay();
                        scan.setDay(mDay);
                        mMonth = "" + persianCalendar.getPersianMonth();
                        scan.setMonth(mMonth);
                        mYear = "" + persianCalendar.getPersianYear();
                        scan.setYear(mYear);
                        scan.setNeedEdit(false);
                        date = mYear + "/" + mMonth + "/" + mDay;
//                        Toast.makeText(_activity, date, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "scan: " + scan);
                        mRepository.updateScan(scan);
                        myTvDate.setText(date);
                    }

                    @Override
                    public void onDismissed() {

                    }
                });


        picker.show();
    }

    public String getPath(int position) {
        return mScans.get(position).getImage();
//        return _imagePaths.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    public void setMaxTexture(int maxTexture, ImageSize targetSize) {
        this.maxTexture = maxTexture;
        mTargetSize = targetSize;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }


    private void rotateImageByUri(String filePath, float rotate) {
        Uri uri = Uri.parse("file:///" + filePath);
        try {
            Bitmap bitmap = null;
            bitmap = MediaStore.Images.Media.getBitmap(_activity.getContentResolver(), uri);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //learn content provider for more info
            OutputStream os = _activity.getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
