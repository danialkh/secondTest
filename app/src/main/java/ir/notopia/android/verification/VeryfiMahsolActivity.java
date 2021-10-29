package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import in.aabhasjindal.otptextview.OtpTextView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VeryfiMahsolActivity extends AppCompatActivity {

    JsonVerificationApi jsonVerificationApi;
    String MahsoldCode;

    private boolean onceBtnTab = true;

    private boolean canResend = true;
    private final Integer[] arrayTimes = {2,2,5,10,15,30};
    private int pointer = 0;
    private OtpTextView otpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veryfi_mahsol);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        otpTextView = findViewById(R.id.verify_mahsol_opt);
        CardView BTVerfyMahsolCode = findViewById(R.id.BTVerfySmsMahsolCode);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://185.239.104.35:8081/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonVerificationApi = retrofit.create(JsonVerificationApi.class);

        SharedPreferences login = VeryfiMahsolActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            MahsoldCode = extras.getString("EXTRA_MAHSOL_CODE");
        }


        TextView resendSms = findViewById(R.id.resendSmsForMahsol);
        resendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.TextViweFadeOutFadeIn(resendSms);

                if(canResend){
                    if(arrayTimes.length > pointer) {
                        canResend = false;

                        PostResendSmsKhashdar(VeryfiMahsolActivity.this,userNumber,MahsoldCode);

                        new CountDownTimer(arrayTimes[pointer]*60*1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                resendSms.setText(String.valueOf(millisUntilFinished / 1000));
                                //here you can have your logic to set text to edittext
                            }

                            public void onFinish() {
                                resendSms.setText(VeryfiMahsolActivity.this.getString(R.string.resendSms));
                                canResend = true;
                            }

                        }.start();

                        pointer++;
                    }
                    else{
                        Toast.makeText(VeryfiMahsolActivity.this,"نمیتوانید درخواست پیامک بیشتری بکنید",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        BTVerfyMahsolCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(onceBtnTab) {

                    onceBtnTab = false;

//                    EditText ETSmsCode = findViewById(R.id.ETMahsolCode);
//                    String smsCode = ETSmsCode.getText().toString();
                    String smsCode = otpTextView.getOTP();


                    if (userNumber != null && MahsoldCode != null) {
                        PostVerifyKhashdar(VeryfiMahsolActivity.this, userNumber, MahsoldCode, smsCode);
                    } else {
                        onceBtnTab = true;
                        Toast.makeText(VeryfiMahsolActivity.this, "لطفا ابتدا ثبت نام کنید", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void PostVerifyKhashdar(final Context context, String number, String MahsoldCode,String smsCode) {

        Call<List<UserStrings>> call = jsonVerificationApi.VerifyMahsol(number,MahsoldCode,smsCode);

        call.enqueue(new Callback<List<UserStrings>>() {
            @Override
            public void onResponse(@NotNull Call<List<UserStrings>> call, @NotNull Response<List<UserStrings>> response) {
                if(!response.isSuccessful()){
                    return;
                }

                List<UserStrings> posts = response.body();

//                String str = posts.get(0).getState() + "\n" + posts.get(0).getStatus();
//                Toast.makeText(SignUpActivity.this,str,Toast.LENGTH_SHORT).show();
                assert posts != null;

                if(posts.get(0).getState().equals("1")){

                    SharedPreferences Login = VeryfiMahsolActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
                    Login.edit().putString("Mahsol_bool_PR",MahsoldCode).apply();


                    Toast.makeText(VeryfiMahsolActivity.this,posts.get(0).getStatus(),Toast.LENGTH_SHORT).show();
                    Intent intentShelf = new Intent(VeryfiMahsolActivity.this, MainActivity.class);
                    intentShelf.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentShelf);
                }
                else{
                    onceBtnTab = true;
                    Toast.makeText(VeryfiMahsolActivity.this,posts.get(0).getStatus(),Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(@NotNull Call<List<UserStrings>> call, @NotNull Throwable t) {

            }
        });
    }


    private void PostResendSmsKhashdar(final Context context,String userNumber, String code) {

        Call<List<UserStrings>> call = jsonVerificationApi.VerifyKhashdar(userNumber,code);

        call.enqueue(new Callback<List<UserStrings>>() {
            @Override
            public void onResponse(@NotNull Call<List<UserStrings>> call, @NotNull Response<List<UserStrings>> response) {
                if(!response.isSuccessful()){
                    return;
                }

                List<UserStrings> posts = response.body();

                assert posts != null;

                if(posts.get(0).getState().equals("1")){
                    // code sended
                }
                else{
                    Toast.makeText(VeryfiMahsolActivity.this,posts.get(0).getStatus(),Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(@NotNull Call<List<UserStrings>> call, @NotNull Throwable t) {

            }
        });

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onceBtnTab = true;
    }
}