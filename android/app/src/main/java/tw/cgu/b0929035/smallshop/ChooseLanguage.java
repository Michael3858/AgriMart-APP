package tw.cgu.b0929035.smallshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseLanguage extends AppCompatActivity implements View.OnClickListener {

    Button btn1, btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SharedPreferences language = getSharedPreferences("Language", MODE_PRIVATE);
        SharedPreferences.Editor edit = language.edit();
        if (v == btn1){
            edit.putString("lan", "中文");
            edit.apply();
        }
        else if (v == btn2){
            edit.putString("lan", "English");
            edit.apply();
        }
        Intent intent = new Intent(ChooseLanguage.this, LoginScreen.class);
        startActivity(intent);
    }
}