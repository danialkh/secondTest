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
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class EnterNumberActivity extends AppCompatActivity {

    private JsonVerificationApi jsonVerificationApi;
    private LottieAnimationView animationView;
    private TextView EnterNumberState,TVBtnVerfyUser;
    private boolean ServerReady = true;
    private OtpTextView otpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vf_phone_number);

        EditText ETVerifyCode = findViewById(R.id.ETVerficationCode);
        CardView BTVerifyUser = findViewById(R.id.BTVerfyUser);
        TVBtnVerfyUser = findViewById(R.id.TVBtnVerfyUser);

        TextView showTerms = findViewById(R.id.showTerms);
        CheckBox checkBoxTerms = findViewById(R.id.CheckBoxTerms);
        showTerms.setText(Html.fromHtml(getString(R.string.checkBoxMatn)));


        showTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.TextViweFadeOutFadeIn(showTerms);

                Intent intent = new Intent(EnterNumberActivity.this, ReadTermsActivity.class);
                EnterNumberActivity.this.startActivity(intent);
            }
        });


        BTVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean checkBoxTerm = checkBoxTerms.isChecked();

                if(checkBoxTerm) {

                    if (ServerReady) {

                        ServerReady = false;

                        EnterNumberState = findViewById(R.id.EnterNumberState);
                        EnterNumberState.setText("در انتظار پاسخ سرور");
                        animationView = findViewById(R.id.lottieAnimationSpiner);
                        animationView.setVisibility(View.VISIBLE);
                        TVBtnVerfyUser.setVisibility(View.GONE);

                        String PhoneNumber = ETVerifyCode.getText().toString();
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://185.239.104.35:8081/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        jsonVerificationApi = retrofit.create(JsonVerificationApi.class);
                        PostSendCodeVorod(EnterNumberActivity.this, PhoneNumber);

                    }
                }
                else{
                    String matn = getResources().getString(R.string.errorTerms);
                    Toast.makeText(EnterNumberActivity.this, matn, Toast.LENGTH_SHORT).show();
                }



            }
        });
    }



    private void PostSendCodeVorod(final Context context, String number) {

        Call<List<UserStrings>> call = jsonVerificationApi.SignUpCode(number);

        call.enqueue(new Callback<List<UserStrings>>() {
            @Override
            public void onResponse(Call<List<UserStrings>> call, Response<List<UserStrings>> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                List<UserStrings> posts = response.body();

                animationView.setVisibility(View.GONE);
                TVBtnVerfyUser.setVisibility(View.VISIBLE);

                if (posts.get(0).getState().equals("1")) {
                    Intent intentEnterCode = new Intent(context, SignUpActivity.class);
                    intentEnterCode.putExtra("EXTRA_LOGIN_NUMBER", number);
                    context.startActivity(intentEnterCode);
                }
                else if (posts.get(0).getState().equals("3")) {
                    Intent intentEnterCode = new Intent(context, SignInActivity.class);
                    intentEnterCode.putExtra("EXTRA_LOGIN_NUMBER", number);
                    context.startActivity(intentEnterCode);
                }
                else {
                    EnterNumberState.setText(posts.get(0).getStatus());
                    ServerReady = true;
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
        ServerReady = true;
        TextView EnterNumberState = findViewById(R.id.EnterNumberState);
        EnterNumberState.setText("شماره تلفن خود را وارد نمایید");
    }
}