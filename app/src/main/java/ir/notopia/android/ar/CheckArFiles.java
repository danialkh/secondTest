package ir.notopia.android.ar;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Objects;

import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ArEntity;

public class CheckArFiles {

    public boolean isOkArFiles() {
        return okArFiles;
    }

    private boolean okArFiles = true;

    public CheckArFiles(Context context) {

        AppRepository mRepository = AppRepository.getInstance(context);
        List<ArEntity> mArs = mRepository.getArs();
        Log.d("CheckArFile",mArs.toString());

        if(mArs.size() != 0) {
            if (countFiles("Targets") == mArs.size() && countFiles("Trackers") == mArs.size()) {
                for (int i = 0; i < mArs.size(); i++) {
                    ArEntity mAr = mArs.get(i);
                    boolean check = CreateFileExist(mAr);
                    if (!check) {
                        okArFiles = false;
                        break;
                    }
                }
            } else {
                okArFiles = false;
            }
        }
        else{
            okArFiles = false;
        }

        if(!okArFiles){
            Intent intent = new Intent(context,DownloadArActivity.class);
            context.startActivity(intent);
        }
    }

    private int countFiles(String folder) {
        String directory = "/Notopia/Ar/";
        File dir = new File(Environment.getExternalStorageDirectory() + directory + folder);
        if (dir.isDirectory()){
            return Objects.requireNonNull(dir.list()).length;
        }
        return 0;
    }


    private boolean CreateFileExist(ArEntity mAr) {


        File filepath = Environment.getExternalStorageDirectory();
        File targetFile = new File(filepath.getPath() + "/Notopia/Ar/Targets/" ,mAr.getType() + "-" + mAr.getTarget());
        File trackerFile = new File(filepath.getPath() + "/Notopia/Ar/Trackers/",mAr.getTracker());

        Log.d("CheckArFile:Targer",targetFile.getPath());
        Log.d("CheckArFile:Tracker",trackerFile.getPath());

        if (!targetFile.exists()) {
            Log.d("CheckArFile:TargetNX",mAr.getTarget());
            return false;
        }
        if (!trackerFile.exists()) {
            Log.d("CheckArFile:TrackerNX",mAr.getTracker());
            return false;
        }
        return true;
    }
}
