package ir.notopia.android;

import android.content.Context;
import android.util.Log;

import java.util.List;

import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ArEntity;
import ir.notopia.android.database.entity.CategoryEntity;

public class FirstCategoryInit {
    public FirstCategoryInit(Context context) {

        AppRepository mRepository = AppRepository.getInstance(context);
        List<CategoryEntity> mCategorys = mRepository.getCategorys();
        Log.d("CheckCategorys",mCategorys.toString());

        if(mCategorys.size() == 0) {
            for(int i = 1;i <= 6;i++){
                String matn = "دسته"  + " " + String.valueOf(i) ;
                CategoryEntity category = new CategoryEntity(matn);
                mRepository.insertCategory(category);
            }
        }

    }
}
