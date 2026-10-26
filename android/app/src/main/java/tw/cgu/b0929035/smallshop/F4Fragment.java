package tw.cgu.b0929035.smallshop;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class F4Fragment extends Fragment implements View.OnClickListener {
    String lan;
    TextView txv, EmailAddress, EmailAddress2, Name, Name2, Gender, Gender2, PhoneNum, PhoneNum2, Birthday, Birthday2, Address, Address2;

    Button Revise, ChangePassword;
    Dialog dialog;
    View viewdialog;
    EditText oldpassword, newpassword;
    Button cancel, confirm;
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this
        SharedPreferences language = requireActivity().getSharedPreferences("Language", Context.MODE_PRIVATE);
        lan = language.getString("lan", "");
        View rootView =  inflater.inflate(R.layout.fragment_f4, container, false);

        AppCompatImageView imageLogOut = rootView.findViewById(R.id.imageLogOut);
        imageLogOut.setOnClickListener(this);

        txv = (TextView) rootView.findViewById(R.id.txv);
        EmailAddress = (TextView) rootView.findViewById(R.id.EmailAddress);
        Name = (TextView) rootView.findViewById(R.id.Name);
        Gender = (TextView) rootView.findViewById(R.id.Gender);
        PhoneNum = (TextView) rootView.findViewById(R.id.PhoneNum);
        Birthday = (TextView) rootView.findViewById(R.id.Birthday);
        Address = (TextView) rootView.findViewById(R.id.Address);
        EmailAddress2 = (TextView) rootView.findViewById(R.id.EmailAddress2);
        Name2 = (TextView) rootView.findViewById(R.id.Name2);
        Gender2 = (TextView) rootView.findViewById(R.id.Gender2);
        PhoneNum2 = (TextView) rootView.findViewById(R.id.PhoneNum2);
        Birthday2 = (TextView) rootView.findViewById(R.id.Birthday2);
        Address2 = (TextView) rootView.findViewById(R.id.Address2);
        Revise = (Button) rootView.findViewById(R.id.Revise);
        Revise.setOnClickListener(this);
        ChangePassword = (Button) rootView.findViewById(R.id.ChangePassword);
        ChangePassword.setOnClickListener(this);


        if(Objects.equals(lan, "English")){
            txv.setText("Profile");
            EmailAddress.setText("Email:");
            Name.setText("Name:");
            Gender.setText("Gender:");
            PhoneNum.setText("PhoneNum:");
            Birthday.setText("Birthday:");
            Address.setText("Address:");
            Revise.setText("Edit profile");
            ChangePassword.setText("Change password");
        }

        preferenceManager = new PreferenceManager(getContext());
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }

            if (documentSnapshot.exists()) {
                String emailAddress = documentSnapshot.getString(Constants.KEY_EMAIL);
                String name = documentSnapshot.getString(Constants.KEY_NAME);
                String gender = documentSnapshot.getString(Constants.KEY_GENDER);
                String phoneNum = documentSnapshot.getString(Constants.KEY_PHONE_NUMBER);
                String birthday = documentSnapshot.getString(Constants.KEY_BIRTHDAY);
                String address = documentSnapshot.getString(Constants.KEY_ADDRESS);

                EmailAddress2.setText(emailAddress);
                Name2.setText(name);
                Gender2.setText(gender);
                PhoneNum2.setText(phoneNum);
                Birthday2.setText(birthday);
                Address2.setText(address);
            } else {

            }
        });
        /*EmailAddress2.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        Name2.setText(preferenceManager.getString(Constants.KEY_NAME));
        Gender2.setText(preferenceManager.getString(Constants.KEY_GENDER));
        PhoneNum2.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        Birthday2.setText(preferenceManager.getString(Constants.KEY_BIRTHDAY));*/
        /*FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    String emailAddress = document.getString("email");
                    String name = document.getString("name");
                    String gender = document.getString("gender");
                    String phoneNum = document.getString("phoneNumber");
                    String birthday = document.getString("birthday");
                    //String address = document.getString("address");
                    EmailAddress2.setText(emailAddress);
                    Name2.setText(name);
                    Gender2.setText(gender);
                    PhoneNum2.setText(phoneNum);
                    Birthday2.setText(birthday);
                    //Address2.setText(address);
                } else {

                }
            } else {
                Exception exception = task.getException();
                if (exception != null) {
                    exception.printStackTrace();
                }
            }
        });*/

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == Revise){
            Intent intent = new Intent(getContext(), ReviseInfo.class);
            startActivity(intent);
        } else if (v == ChangePassword) {
            getDialog();
        } else if (v.getId() == R.id.imageLogOut) {
            logOut();
        }
    }
    private void getDialog(){
        dialog = new Dialog(getContext());
        viewdialog = getLayoutInflater().inflate(R.layout.dialog, null);
        dialog.setContentView(viewdialog);
        oldpassword = viewdialog.findViewById(R.id.oldpassword);
        newpassword = viewdialog.findViewById(R.id.newpassword);
        cancel = viewdialog.findViewById(R.id.cancel);
        confirm = viewdialog.findViewById(R.id.confirm);
        if(Objects.equals(lan, "English")){
            oldpassword.setHint("Please enter your old password");
            newpassword.setHint("Please enter your new password");
            cancel.setText("Cancel");
            confirm.setText("Confirm");
        }
        dialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(oldpassword.getText().toString())){
                    Toast.makeText(getContext(), "請輸入舊密碼!", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(newpassword.getText().toString())){
                    Toast.makeText(getContext(), "請輸入新密碼!", Toast.LENGTH_SHORT).show();
                }
                else{
                    preferenceManager = new PreferenceManager(getContext());
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo(Constants.KEY_EMAIL, EmailAddress2.getText().toString())
                            .whereEqualTo(Constants.KEY_PASSWORD, oldpassword.getText().toString())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                                    HashMap<String, Object> updates = new HashMap<>();
                                    updates.put(Constants.KEY_PASSWORD, newpassword.getText().toString());
                                    database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID))
                                            .update(updates)
                                            .addOnSuccessListener(result ->{
                                                Toast.makeText(getContext(), "修改成功!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(e ->{

                                            });

                                }else {
                                    showToast("舊密碼錯誤!");
                                }
                            });
                }
            }
        });
    }
    private void logOut() {
        preferenceManager.clear();
        showToast("Log out");
        startActivity(new Intent(getContext(), LoginScreen.class));
    }
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}