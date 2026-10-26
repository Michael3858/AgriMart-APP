package tw.cgu.b0929035.smallshop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class ReviseInfo extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    String lan;
    TextView txv, Name, Gender, PhoneNum, Birthday, Address;

    Calendar c = Calendar.getInstance();
    EditText Name2, PhoneNum2, Address2, Birthday2;
    Button Confirm, openMapButton;
    RadioButton men, women;
    ImageButton Back;
    static final int REQUEST_CODE_MAP = 1;  // 請求碼
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise_info);

        SharedPreferences language = getSharedPreferences("Language", Context.MODE_PRIVATE);
        lan = language.getString("lan", "");

        txv = (TextView) findViewById(R.id.txv);
        Name = (TextView) findViewById(R.id.Name);
        Gender = (TextView) findViewById(R.id.Gender);
        men = (RadioButton) findViewById(R.id.men);
        women = (RadioButton) findViewById(R.id.women);
        PhoneNum = (TextView) findViewById(R.id.PhoneNum);
        Birthday = (TextView) findViewById(R.id.Birthday);
        Address = (TextView) findViewById(R.id.Address);
        Name2 = (EditText) findViewById(R.id.Name2);
        PhoneNum2 = (EditText) findViewById(R.id.PhoneNum2);
        Birthday2 = (EditText) findViewById(R.id.Birthday2);
        Address2 = (EditText) findViewById(R.id.Address2);
        Confirm = (Button) findViewById(R.id.Confirm);
        Back = (ImageButton) findViewById(R.id.Back);
        openMapButton = (Button) findViewById(R.id.openMapButton);
        Birthday2.setOnClickListener(this);
        Confirm.setOnClickListener(this);
        Back.setOnClickListener(this);
        openMapButton.setOnClickListener(this);

        if(Objects.equals(lan, "English")) {
            txv.setText("Edit profile");
            Name.setText("Name:");
            Gender.setText("Gender:");
            men.setText("men");
            women.setText("women");
            PhoneNum.setText("PhoneNum:");
            Birthday.setText("Birthday:");
            Address.setText("Address:");
            Confirm.setText("Confirm");
        }
    }

    @Override
    public void onClick(View v) {
        if(v == Birthday2){
            new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        }
        if(v == Confirm){
            preferenceManager = new PreferenceManager(getApplicationContext());
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            HashMap<String, Object> updates = new HashMap<>();
            if (!Name2.getText().toString().equals("")){
                updates.put(Constants.KEY_NAME, Name2.getText().toString());
            }
            if (!PhoneNum2.getText().toString().equals("")){
                updates.put(Constants.KEY_PHONE_NUMBER, PhoneNum2.getText().toString());
            }
            if (!Birthday2.getText().toString().equals("")){
                updates.put(Constants.KEY_BIRTHDAY, Birthday2.getText().toString());
            }
            if (!Address2.getText().toString().equals("")){
                updates.put(Constants.KEY_ADDRESS, Address2.getText().toString());
            }
            firestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .update(updates)
                    .addOnSuccessListener(result ->{
                        Toast.makeText(this, "更新成功!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->{

                    });
            Intent intent = new Intent();
            finish();
        }
        if(v == Back){
            Intent intent = new Intent();
            finish();
        }
        if (v == openMapButton) {
            Intent intent = new Intent(this, MapsActivity.class); // 将 map.class 替换为您的地图活动的类名
            startActivityForResult(intent, REQUEST_CODE_MAP);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Birthday2.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedAddress")) {
                String selectedAddress = data.getStringExtra("selectedAddress");
                TextView addressTextView = findViewById(R.id.Address2);
                addressTextView.setText(selectedAddress);
            }
        }
    }
}