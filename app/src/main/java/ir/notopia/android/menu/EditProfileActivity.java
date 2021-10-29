package ir.notopia.android.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.verification.UserStrings;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText ETEditName,ETEditFamily;
    private JsonEditApi jsonEditApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences login = EditProfileActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);
        String userName = login.getString("USER_NAME_PR", null);
        String userFamily = login.getString("USER_FAMILY_PR", null);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://185.239.104.35:8081/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonEditApi = retrofit.create(JsonEditApi.class);

        ETEditName = findViewById(R.id.ETEditName);
        ETEditFamily = findViewById(R.id.ETEditFamily);

        ETEditName.setText(userName);
        ETEditFamily.setText(userFamily);

        CardView bTEditProfile = findViewById(R.id.BTEditProfile);

        bTEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = ETEditName.getText().toString();
                String family = ETEditFamily.getText().toString();

                PostEditProfile(EditProfileActivity.this, userNumber,name,family);

            }
        });



        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);
                Intent intentBack = new Intent(EditProfileActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });
    }

    private void PostEditProfile(Context context, String userNumber, String name, String family) {

        Call<List<UserStrings>> call = jsonEditApi.EditProfile(userNumber,name,family);

        call.enqueue(new Callback<List<UserStrings>>() {
            @Override
            public void onResponse(Call<List<UserStrings>> call, Response<List<UserStrings>> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                List<UserStrings> posts = response.body();

                if (posts.get(0).getState().equals("1")) {

                    SharedPreferences Login = EditProfileActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = Login.edit();
                    editor.putString("USER_NUMBER_PR",userNumber);
                    editor.putString("USER_NAME_PR",name);
                    editor.putString("USER_FAMILY_PR",family).apply();

                    Intent intentMain = new Intent(context, MainActivity.class);
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentMain);
                }
                else {
                    Toast.makeText(context,posts.get(0).getStatus(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserStrings>> call, Throwable t) {

            }
        });

    }


}