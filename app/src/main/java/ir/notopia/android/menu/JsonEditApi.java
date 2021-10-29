package ir.notopia.android.menu;

import java.util.List;

import ir.notopia.android.verification.UserStrings;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface JsonEditApi {


    @FormUrlEncoded
    @POST("api/EditProfileApi.php")
    Call<List<UserStrings>> EditProfile(
            @Field("number") String number,
            @Field("name") String name,
            @Field("family") String family
    );
}
