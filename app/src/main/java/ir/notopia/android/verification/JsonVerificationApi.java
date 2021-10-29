package ir.notopia.android.verification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface JsonVerificationApi {
    @FormUrlEncoded
    @POST("api/SignUpCodeApi.php")
    Call<List<UserStrings>> SignUpCode(
            @Field("number") String number
    );

    @FormUrlEncoded
    @POST("api/SignUpApi.php")
    Call<List<UserStrings>> SignUpUser(
            @Field("number") String number,
            @Field("imei") String imei,
            @Field("code") String code,
            @Field("name") String name,
            @Field("family") String family
    );

    @FormUrlEncoded
    @POST("api/SignInApi.php")
    Call<List<UserStrings>> SignInUser(
            @Field("imei") String imei,
            @Field("number") String number,
            @Field("code") String code
    );


    @FormUrlEncoded
    @POST("api/VerifyKhashdarApi.php")
    Call<List<UserStrings>> VerifyKhashdar(
            @Field("number") String number,
            @Field("code") String code
    );


    @FormUrlEncoded
    @POST("api/VerifyMahsolApi.php")
    Call<List<UserStrings>> VerifyMahsol(
            @Field("number") String number,
            @Field("mahsolCode") String mahsolCode,
            @Field("smsCode") String smsCode
    );
}
