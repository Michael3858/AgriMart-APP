package tw.cgu.b0929035.smallshop;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.SimpleFormatter;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class Sell extends AppCompatActivity {
    String lan;

    ImageButton back;
    ImageView pic;
    TextView txv, pi, Name, Info, Price, Num, Classification, Deal;
    EditText ProductName, ProductInfo, ProductPrice, ProductNumber, ProductDeal;
    Spinner Spinner;
    Button Btn;
    private PreferenceManager preferenceManager;
    Uri uri;
    String time;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        SharedPreferences language = getSharedPreferences("Language", Context.MODE_PRIVATE);
        lan = language.getString("lan", "");

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                finish();
            }
        });

        if (getApplicationContext() != null) {
            preferenceManager = new PreferenceManager(getApplicationContext());
        } else {
            showToast("Error");
            return;
        }

        pic = (ImageView) findViewById(R.id.pic);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
                pic.setBackgroundResource(0);
            }
        });
        txv = (TextView) findViewById(R.id.txv);
        pi = (TextView) findViewById(R.id.pi);
        Name = (TextView) findViewById(R.id.Name);
        Info = (TextView) findViewById(R.id.Info);
        Price = (TextView) findViewById(R.id.Price);
        Num = (TextView) findViewById(R.id.Num);
        Classification = (TextView) findViewById(R.id.Classification);
        Deal = (TextView) findViewById(R.id.Deal);
        ProductName = findViewById(R.id.Name2);
        ProductInfo = findViewById(R.id.Info2);
        ProductPrice = findViewById(R.id.Price2);
        ProductNumber = findViewById(R.id.Num2);
        ProductDeal = findViewById(R.id.Deal2);
        Spinner = (Spinner) findViewById(R.id.Spinner);
        String[] dataSource = getResources().getStringArray(R.array.classification);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataSource);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner.setAdapter(adapter);
        Btn = (Button) findViewById(R.id.Btn);
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUp();
            }
        });

        if(Objects.equals(lan, "English")){
            txv.setText("List products");
            pi.setText("Product pic:");
            Name.setText("Product name:");
            Info.setText("Product description:");
            Price.setText("Price:");
            Num.setText("Quantity:");
            Classification.setText("Classification:");
            Deal.setText("Transaction method:");
            String[] newDataSource = getResources().getStringArray(R.array.classification2);
            ArrayAdapter<String> newAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, newDataSource);
            newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner.setAdapter(newAdapter);
            Btn.setText("Put up");
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void setUp() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference productCollection = database.collection(Constants.KEY_COLLECTION_PRODUCT);
        DocumentReference newProductRef = productCollection.document();
        HashMap<String, Object> product = new HashMap<>();
        product.put(Constants.KEY_PRODUCT_OWNER, preferenceManager.getString(Constants.KEY_USER_ID));
        product.put(Constants.KEY_PRODUCT_NAME, ProductName.getText().toString());
        product.put(Constants.KEY_PRODUCT_DESCRIPTION, ProductInfo.getText().toString());
        product.put(Constants.KEY_PRODUCT_PRICE, ProductPrice.getText().toString());
        product.put(Constants.KEY_PRODUCT_ACCOUNT, ProductNumber.getText().toString());
        product.put(Constants.KEY_PRODUCT_CLASSIFIABLE, Spinner.getSelectedItem().toString());
        product.put(Constants.KEY_PRODUCT_TRADE, ProductDeal.getText().toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        time = simpleDateFormat.format(date);
        product.put(Constants.KEY_PRODUCT_TIME, time);

        newProductRef.set(product)
                .addOnSuccessListener(documentReference -> {
                    String productId = newProductRef.getId();
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + productId + ".jpg");
                    storageRef.putFile(uri)
                            .addOnSuccessListener(taskSnapshot -> {
                                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    newProductRef.update("imageUrl", imageUrl)
                                            .addOnSuccessListener(aVoid1 -> {
                                                showToast("Launch");
                                                Intent intent = new Intent(getApplicationContext(), FrontPage.class);
                                                startActivity(intent);
                                            })
                                            .addOnFailureListener(e -> {
                                                showToast("圖像URL添加失敗：" + e.getMessage());
                                            });
                                });
                            })
                            .addOnFailureListener(e -> {
                                showToast("上傳圖像失敗：" + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showToast("Error!!");
                });
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            pic.setImageBitmap(bitmap);
                            uri = imageUri;
                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
}