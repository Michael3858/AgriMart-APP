package tw.cgu.b0929035.smallshop;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Phaser;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class SignUpPage extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    String lan;
    Calendar c = Calendar.getInstance();
    TextView txv, Gen;
    EditText Address, Birthday, EmailAddress, Password, ConfirmPassword, Name, PhoneNum;
    RadioGroup Gender;
    RadioButton men, women;
    Button Confirm;
    ImageButton Back;
    static final int REQUEST_CODE_MAP = 1;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        SharedPreferences language = getSharedPreferences("Language", Context.MODE_PRIVATE);
        lan = language.getString("lan", "");

        txv = (TextView) findViewById(R.id.txv);
        Address = (EditText) findViewById(R.id.Address);
        Birthday = (EditText) findViewById(R.id.Birthday);
        Confirm = (Button) findViewById(R.id.Confirm);
        Back = (ImageButton) findViewById(R.id.Back);
        EmailAddress = (EditText) findViewById(R.id.EmailAddress);
        Password = (EditText) findViewById(R.id.Password);
        ConfirmPassword = (EditText) findViewById(R.id.Password2);
        Name = (EditText) findViewById(R.id.Name);
        PhoneNum = (EditText) findViewById(R.id.PhoneNum);
        Gen = (TextView) findViewById(R.id.Gender);
        Gender = findViewById(R.id.RadioGroup);
        men = (RadioButton) findViewById(R.id.men);
        women = (RadioButton) findViewById(R.id.women);
        Address.setOnClickListener(this);
        Birthday.setOnClickListener(this);
        Confirm.setOnClickListener(this);
        Back.setOnClickListener(this);

        if(Objects.equals(lan, "English")) {
            txv.setText("User registration");
            EmailAddress.setHint("Please enter your email address");
            Password.setHint("Please enter your password");
            ConfirmPassword.setHint("Please confirm your password");
            Name.setHint("Please enter your name");
            PhoneNum.setHint("Please enter your phone number");
            Birthday.setHint("Please enter your birthday");
            Address.setHint("Please enter your address");
            Gen.setText("Select gender:");
            men.setText("men");
            women.setText("women");
            Confirm.setText("Confirm");
        }

        preferenceManager = new PreferenceManager(getApplicationContext());
    }
    @Override
    public void onClick(View v) {
        if(v == Address){
            Intent intent = new Intent(this, MapsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_MAP);
        }
        if(v == Birthday){
            new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        }
        if(v == Confirm){
            if (isValidSignUpDetails()) {
                signUp();
            }
        }
        if(v == Back){
            Intent intent = new Intent();
            finish();
        }
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Birthday.setText(year + "/" + (month + 1) + "/" + dayOfMonth);
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("selectedAddress")) {
                String selectedAddress = data.getStringExtra("selectedAddress");
                Address.setText(selectedAddress);
            }
        }
    }

    private void signUp() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_EMAIL, EmailAddress.getText().toString());
        user.put(Constants.KEY_PASSWORD, Password.getText().toString());
        user.put(Constants.KEY_NAME, Name.getText().toString());
        user.put(Constants.KEY_PHONE_NUMBER, PhoneNum.getText().toString());
        user.put(Constants.KEY_BIRTHDAY, Birthday.getText().toString());
        user.put(Constants.KEY_ADDRESS, Address.getText().toString());

        String g = "";
        if (Gender.getCheckedRadioButtonId() == R.id.men) {
            g = "男生";
        } else if (Gender.getCheckedRadioButtonId() == R.id.women) {
            g = "女生";
        }

        user.put(Constants.KEY_GENDER, g);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, Name.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), FrontPage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    showToast(e.getMessage());
                });
    }
    private Boolean isValidSignUpDetails() {
        if (EmailAddress.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(EmailAddress.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (Password.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (ConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!Password.getText().toString().equals(ConfirmPassword.getText().toString())) {
            showToast("Password and confirm password must be same");
            return false;
        } else if (Name.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (PhoneNum.getText().toString().trim().isEmpty()) {
            showToast("Enter phone number");
            return false;
        } else if (Birthday.getText().toString().trim().isEmpty()){
            showToast("Enter birthday");
            return false;
        } else if (Address.getText().toString().trim().isEmpty()){
            showToast("Enter address");
            return false;
        }else {
            return true;
        }
    }
}