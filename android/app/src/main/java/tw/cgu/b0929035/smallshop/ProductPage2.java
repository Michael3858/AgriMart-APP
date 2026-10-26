package tw.cgu.b0929035.smallshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class ProductPage2 extends AppCompatActivity {

    ImageButton back;
    ImageView Photo;
    TextView txv, Product, Seller, Price, Time, Deal, Info;
    Button Remove;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page2);

        preferenceManager = new PreferenceManager(getApplicationContext());

        SharedPreferences language = getSharedPreferences("Language", MODE_PRIVATE);
        String lan = language.getString("lan", "");

        Bundle bundle = getIntent().getExtras();
        String ID = bundle.getString("ID");
        //Toast.makeText(this, ID, Toast.LENGTH_SHORT).show();

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                finish();
            }
        });

        txv = (TextView) findViewById(R.id.txv);
        if(Objects.equals(lan, "English")){
            txv.setText("Product Details");
        }

        Photo = (ImageView) findViewById(R.id.Photo);
        Product = (TextView) findViewById(R.id.Product);
        Seller = (TextView) findViewById(R.id.Seller);
        Price = (TextView) findViewById(R.id.Price);
        Time = (TextView) findViewById(R.id.Time);
        Deal = (TextView) findViewById(R.id.Deal);
        Info = (TextView) findViewById(R.id.Info);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_PRODUCT).document(ID);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).fit().into(Photo);
                Product.setText("商品:" + documentSnapshot.getString(Constants.KEY_PRODUCT_NAME));
                String productOwner = documentSnapshot.getString(Constants.KEY_PRODUCT_OWNER);
                CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_USERS);
                DocumentReference userDocumentRef = usersRef.document(productOwner);
                userDocumentRef.get().addOnSuccessListener(documentSnapshot2 -> {
                    if (documentSnapshot2.exists()) {
                        Map<String, Object> userData = documentSnapshot2.getData();
                        String name = (String) userData.get("name");
                        Seller.setText("賣家:" + name);
                        Log.e("FirestoreDebug", "test:" + name);
                    } else {
                        Log.e("FirestoreDebug", "Not exists");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Query failed: " + e.getMessage());
                });
                Price.setText("價格:" + documentSnapshot.getString(Constants.KEY_PRODUCT_PRICE));
                String productTime = documentSnapshot.getString(Constants.KEY_PRODUCT_TIME);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                String time = simpleDateFormat.format(date);
                try {
                    Date begin = simpleDateFormat.parse(productTime);
                    Date end = simpleDateFormat.parse(time);
                    long diff = end.getTime() - begin.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
                    String t = Long.toString(days) + " 天 " + Long.toString(hours) + " 小時";
                    Time.setText("商品上架時間:" + t);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Deal.setText("交易方式:" + documentSnapshot.getString(Constants.KEY_PRODUCT_TRADE));
                Info.setText("詳細資訊:" + documentSnapshot.getString(Constants.KEY_PRODUCT_DESCRIPTION));
            }
        });
        Remove = (Button) findViewById(R.id.Remove);
        Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection(Constants.KEY_COLLECTION_PRODUCT).document(ID);
                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(ProductPage2.this, "下架商品成功!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        finish();
                        Log.d("Firestore", "DocumentSnapshot successfully deleted!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Error deleting document", e);
                    }
                });
            }
        });
    }
}