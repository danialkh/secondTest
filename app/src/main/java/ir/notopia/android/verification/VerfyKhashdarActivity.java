package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VerfyKhashdarActivity extends AppCompatActivity {

    JsonVerificationApi jsonVerificationApi;
    private boolean ServerReady = true;
    private TextView EnterNumberState,TVBtnVerfyMahsolCode;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verfy_khashdar);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        TVBtnVerfyMahsolCode = findViewById(R.id.TVBtnVerfyMahsolCode);
        CardView BTVerfyMahsolCode = findViewById(R.id.BTVerfyMahsolCode);

        BTVerfyMahsolCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ServerReady) {

                    ServerReady = false;

                    EnterNumberState = findViewById(R.id.EnterKhashdarCodeState);
                    EnterNumberState.setText("در انتظار پاسخ سرور");
                    animationView = findViewById(R.id.lottieAnimationSpinerMahsol);
                    animationView.setVisibility(View.VISIBLE);
                    TVBtnVerfyMahsolCode.setVisibility(View.GONE);

                    EditText ETMahsolCode = findViewById(R.id.ETMahsolCode);
                    String enteredCode = ETMahsolCode.getText().toString();

                    SharedPreferences login = VerfyKhashdarActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
                    String userNumber = login.getString("USER_NUMBER_PR", null);

                    if (userNumber != null) {

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://185.239.104.35:8081/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        jsonVerificationApi = retrofit.create(JsonVerificationApi.class);
                        PostVerifyKhashdar(VerfyKhashdarActivity.this, userNumber, enteredCode);
                    } else {
                        Toast.makeText(VerfyKhashdarActivity.this, "لطفا ابتدا ثبت نام کنید", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void PostVerifyKhashdar(final Context context,String userNumber, String code) {

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

                    Intent intentMahsol = new Intent(VerfyKhashdarActivity.this, VeryfiMahsolActivity.class);
                    intentMahsol.putExtra("EXTRA_MAHSOL_CODE",code);
                    startActivity(intentMahsol);
                }
                else{
                    EnterNumberState.setText(posts.get(0).getStatus());
                    animationView.setVisibility(View.GONE);
                    TVBtnVerfyMahsolCode.setVisibility(View.VISIBLE);
                    ServerReady = true;
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
        ServerReady = true;
        TextView EnterNumberState = findViewById(R.id.EnterKhashdarCodeState);
        EnterNumberState.setText("کد محصول را وارد نماید");
    }
}
