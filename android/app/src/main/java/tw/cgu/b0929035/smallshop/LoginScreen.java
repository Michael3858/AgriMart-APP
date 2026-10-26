package tw.cgu.b0929035.smallshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class LoginScreen extends AppCompatActivity implements View.OnClickListener {

    EditText Account, Password;
    CheckBox Remember;
    Button Login, Sign, ForgetPW;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), FrontPage.class);
            startActivity(intent);
            finish();
        }

        SharedPreferences language = getSharedPreferences("Language", MODE_PRIVATE);
        String lan = language.getString("lan", "");

        Account = findViewById(R.id.Account);
        Password = findViewById(R.id.Password);
        Remember = findViewById(R.id.Remember);
        print();

        Login = (Button) findViewById(R.id.Login);
        Login.setOnClickListener(this);

        Sign = (Button) findViewById(R.id.Sign);
        Sign.setOnClickListener(this);

        ForgetPW = (Button) findViewById(R.id.ForgetPW);
        ForgetPW.setOnClickListener(this);

        if(Objects.equals(lan, "English")){
            Account.setHint("Enter your account(email)");
            Password.setHint("Enter your password");
            Remember.setText("Save login information");
            Login.setText("Login");
            Sign.setText("Sign up");
            ForgetPW.setText("Forget password");
        }
    }

    private void print() {
        SharedPreferences user = getSharedPreferences("remember", MODE_PRIVATE);
        String acc = user.getString("account", "");
        String pw = user.getString("password", "");
        boolean remember = user.getBoolean("remember", false);
        Account.setText(acc);
        Password.setText(pw);
        Remember.setChecked(remember);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.Login:
                if (isValidSignInDetail()) {
                    signIn();
                }
                break;
            case R.id.Sign:
                Intent intent2 = new Intent(LoginScreen.this, SignUpPage.class);
                startActivity(intent2);
                break;
        }
    }
    private void signIn() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, Account.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, Password.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        input();
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        Intent intent = new Intent(getApplicationContext(), FrontPage.class);
                        startActivity(intent);
                    }else {
                        showToast("Unable to log in");
                    }
                });
    }
    private Boolean isValidSignInDetail() {
        if (Account.getText().toString().trim().isEmpty()) {
            showToast("Enter account");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Account.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (Password.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else {
            return true;
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void input() {

        SharedPreferences user = getSharedPreferences("remember", MODE_PRIVATE);
        SharedPreferences.Editor edit = user.edit();
        if (Remember.isChecked()) { //如果記住密碼那個選項有打勾就把這次成功登入的資料儲存起來
            edit.putString("account",Account.getText().toString());
            edit.putString("password",Password.getText().toString());
            edit.putBoolean("remember",true);  //把我自己設的remember狀態設為true

        } else {
            edit.remove("account"); //若上次沒打勾就把它清除記住的資料
            edit.remove("password");
            edit.putBoolean("remember",false);
        }
        edit.commit();
        SharedPreferences user1 = getSharedPreferences("account", MODE_PRIVATE);
        SharedPreferences.Editor edit1 = user1.edit();
        edit1.putString("acc", Account.getText().toString());
        edit1.commit();
    }
}