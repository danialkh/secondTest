package ir.notopia.android.scanner.opennotescanner.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.List;

import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.omr.OMR;
import ir.notopia.android.scanner.omr.QrCode;

public class MyOmrTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "OpenNoteScannerActivity";
    private Context mContext;
    private AppRepository mRepository;
    private String mImage;
    private Mat source;
    private String[] categories = {"1", "2", "3", "4", "5", "6"};
    private String mCategory;
    private String mDay;
    private String mMonth;
    private String mYear;
    private final String mQrCode;
    private SharedPreferences doScan;
    private  SharedPreferences.Editor editor;

    public MyOmrTask(Context context, AppRepository mRepository, String image, String qrCode) {
        this.mRepository = mRepository;
        mContext = context;
        mImage = image;
        source = Imgcodecs.imread(mImage);
        this.mQrCode = qrCode;

        doScan = mContext.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        editor = doScan.edit();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        editor.putString("doHamedState","1").apply();

        Log.d("calcS doHamedState:", "OMRTK:1");

        Log.d("doInBackground: " , mQrCode);
        QrCode qrCode = new QrCode(mQrCode);
        qrCode.calc();

        OMR omr = new OMR(source,qrCode.isNew(), qrCode.isEven(), mContext);
        List<String> Results;
        try {
            Results = omr.get_PageInfo();
            Log.i("TAG_LOG", "getOMRData: " + " Month: " + Results.get(0) + " Day: " + Results.get(1) + " Category: " + Results.get(2) + " QrCode: " + mQrCode);

            mMonth = Results.get(0);
            mDay = Results.get(1);
            mCategory = Results.get(2);
            if(qrCode.getMahsolYear() == null){
                mYear = "1399";
            }
            else{
                if (qrCode.getMahsolYear().length() == 1) {
                    mYear = "140" + qrCode.getMahsolYear();
                }
                else {
                    mYear = "13" + qrCode.getMahsolYear();
                }
            }
            String date = mYear + "/" + Results.get(0) + "/" + Results.get(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPostExecute(Void result) {

        Log.d(TAG, "onPostExecute: " + mCategory + mYear + mMonth + mDay);
        Log.d(TAG, "onPostExecute: " + new ScanEntity(mImage, mCategory, mYear, mMonth, mDay, mQrCode, true).toString());

        Log.d(TAG, "onPostExecute start: " + mRepository.getScans().size());

        ScanEntity mScan;
        int lsTedadScans = mRepository.getScans().size();

        if (mCategory.equals("0") || mYear.equals("1398") || mMonth.equals("0") || mDay.equals("0")) {

            mScan = new ScanEntity(mImage, mCategory, mYear, mMonth, mDay, mQrCode, true);
            mRepository.insertScan(mScan);

        } else {
            mScan = new ScanEntity(mImage, mCategory, mYear, mMonth, mDay, mQrCode, false);
            mRepository.insertScan(mScan);
        }
        CheckScanFinished(mImage,mScan,lsTedadScans);

    }

    private void CheckScanFinished(String image, ScanEntity mScan,int lsTedadScans) {

        int TedadScans = mRepository.getScans().size();

        if (TedadScans <= lsTedadScans) {
            new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    CheckScanFinished(image,mScan,lsTedadScans);
                }
            },
            400);
        }
        else {
            editor.putString("doHamedState","0").apply();
        }

    }
}
