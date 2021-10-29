package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import in.aabhasjindal.otptextview.OtpTextView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.ShelfsActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SignInActivity extends AppCompatActivity {

    EditText ETSignInCode;
    CardView BTVerfyCode;
    TextView resendSms;
    private JsonVerificationApi jsonVerificationApi;
    private String imei;
    public static final int PERMISSION_READ_STATE = 50;

    private boolean onceBtnTab = true;
    private boolean canResend = true;
    private final Integer[] arrayTimes = {2,2,5,10,15,30};
    private int pointer = 0;
    private OtpTextView otpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        imei = Settings.Secure.getString(
                SignInActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String number;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            number = extras.getString("EXTRA_LOGIN_NUMBER");


            otpTextView = findViewById(R.id.SignInOpt);
            ETSignInCode = findViewById(R.id.ETSignInCode);
            BTVerfyCode = findViewById(R.id.BTVerfySignInCode);
            resendSms = findViewById(R.id.resendSmsForSignIn);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://185.239.104.35:8081/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            jsonVerificationApi = retrofit.create(JsonVerificationApi.class);

            resendSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivity.TextViweFadeOutFadeIn(resendSms);

                    if(canResend){
                        if(arrayTimes.length > pointer) {
                            canResend = false;

                            PostSendCodeVorod(SignInActivity.this,number);

                            new CountDownTimer(arrayTimes[pointer]*60*1000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    resendSms.setText(String.valueOf(millisUntilFinished / 1000));
                                    //here you can have your logic to set text to edittext
                                }

                                public void onFinish() {
                                    resendSms.setText(SignInActivity.this.getString(R.string.resendSms));
                                    canResend = true;
                                }

                            }.start();

                            pointer++;
                        }
                        else{
                            Toast.makeText(SignInActivity.this,"نمیتوانید درخواست پیامک بیشتری بکنید",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


            BTVerfyCode.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("HardwareIds")
                @Override
                public void onClick(View v) {

                    if(onceBtnTab) {

                        onceBtnTab = false;

                        String enteredCode = otpTextView.getOTP();

                        if (enteredCode != null) {

                            Log.d("SignInn", enteredCode);
                            PostSignInUser(SignInActivity.this,imei, number, enteredCode);

                        } else {
                            onceBtnTab = true;
                            Toast.makeText(SignInActivity.this, "ورودی ها صحیح نیست", Toast.LENGTH_SHORT).show();
                        }


                    }
                }
            });
        }
    }

    private void PostSignInUser(final Context context,String imei,String number,String code) {

        Call<List<UserStrings>> call = jsonVerificationApi.SignInUser(imei,number,code);

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

//                    Toast.makeText(context,"Yeap",Toast.LENGTH_SHORT).show();
//                    SharedPreferences Login = SignInActivity.this.getSharedPreferences("LOGIN_PR", Context.MODE_PRIVATE);
//                    Login.edit().putString("LOGIN_PR",number).apply();

                    String name = posts.get(0).getName();
                    String family = posts.get(0).getFamily();

                    SharedPreferences Login = SignInActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = Login.edit();
                    editor.putString("USER_NUMBER_PR",number);
                    editor.putString("USER_NAME_PR",name);
                    editor.putString("USER_FAMILY_PR",family).apply();


                    Intent intent = new Intent(context, ShelfsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else{
                    onceBtnTab = true;
                    Toast.makeText(SignInActivity.this,posts.get(0).getStatus(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<UserStrings>> call, @NotNull Throwable t) {

            }
        });

    }


    private void PostSendCodeVorod(final Context context, String number) {

        Call<List<UserStrings>> call = jsonVerificationApi.SignUpCode(number);

        call.enqueue(new Callback<List<UserStrings>>() {
            @Override
            public void onResponse(Call<List<UserStrings>> call, Response<List<UserStrings>> response) {
                if(!response.isSuccessful()){
                    return;
                }

                List<UserStrings> posts = response.body();

                if(posts.get(0).getState().equals("1") || posts.get(0).getState().equals("3")){
                    // code mojadadan ersal shod
                }
                else{
                    Toast.makeText(SignInActivity.this,posts.get(0).getStatus(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserStrings>> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        onceBtnTab = true;
    }
}