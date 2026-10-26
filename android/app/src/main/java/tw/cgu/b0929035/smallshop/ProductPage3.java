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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tw.cgu.b0929035.smallshop.Activity.ChatActivity;
import tw.cgu.b0929035.smallshop.Activity.PaypalActivity;
import tw.cgu.b0929035.smallshop.models.User;
import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class ProductPage3 extends AppCompatActivity {
    ImageButton back, chat;
    ImageView Photo;
    TextView txv, Product, Seller, Price, Time, Deal, Distance, Info;
    Button Cart, Paypal;
    String ID, productOwner;
    User user;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page3);
        preferenceManager = new PreferenceManager(getApplicationContext());

        SharedPreferences language = getSharedPreferences("Language", MODE_PRIVATE);
        String lan = language.getString("lan", "");

        Bundle bundle = getIntent().getExtras();
        ID = bundle.getString("ID");
        //Toast.makeText(this, ID, Toast.LENGTH_SHORT).show();

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                finish();
            }
        });

        chat = (ImageButton) findViewById(R.id.BtnChat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
        });

        txv = (TextView) findViewById(R.id.txv);
        if (Objects.equals(lan, "English")) {
            txv.setText("Product Details");
        }

        Photo = (ImageView) findViewById(R.id.Photo);
        Product = (TextView) findViewById(R.id.Product);
        Seller = (TextView) findViewById(R.id.Seller);
        Price = (TextView) findViewById(R.id.Price);
        Time = (TextView) findViewById(R.id.Time);
        Deal = (TextView) findViewById(R.id.Deal);
        Distance = (TextView) findViewById(R.id.Distance);
        Info = (TextView) findViewById(R.id.Info);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_PRODUCT).document(ID);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Picasso.get().load(documentSnapshot.getString("imageUrl")).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).fit().into(Photo);
                Product.setText("商品:" + documentSnapshot.getString(Constants.KEY_PRODUCT_NAME));
                productOwner = documentSnapshot.getString(Constants.KEY_PRODUCT_OWNER);
                DataFromproductOwnerID();

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
        Cart = (Button) findViewById(R.id.Cart);
        Cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("ShoppingCart")
                        .whereEqualTo("productID", ID)
                        .whereEqualTo("userID", preferenceManager.getString(Constants.KEY_USER_ID))
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                String CartID = documentSnapshot.getId();
                                DocumentReference docRef = db.collection("ShoppingCart").document(CartID);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(ProductPage3.this, "移除成功!", Toast.LENGTH_SHORT).show();
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
                            }else {

                            }
                        });
            }
        });
        Paypal = (Button) findViewById(R.id.Paypal);
        Paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    String Price = documentSnapshot.getString(Constants.KEY_PRODUCT_PRICE);
                    Intent intent = new Intent();
                    intent.setClass(ProductPage3.this, PaypalActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Price", Price);
                    intent.putExtras(bundle);
                    startActivity(intent);
                });
            }
        });
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    String productOwnerId = productOwner;
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (productOwnerId.equals(queryDocumentSnapshot.getId())) {
                                user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                user.id = productOwnerId;
                            }
                        }
                    }
                });

    }

    private void DataFromproductOwnerID() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("distance").document(preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String distance = documentSnapshot.getString(productOwner); // 使用正確的字段名
                        // 使用 productOwnerID 去獲取相應的數據
                        Log.d("DistanceData", "距離" + distance + " 公尺");
                        float intdistanct = Float.parseFloat(distance);
                        ;
                        intdistanct = intdistanct / 1000;
                        String stringdistance = String.valueOf(intdistanct);
                        Distance.setText("距離:" + stringdistance + "公里");
                        saveDistanceToFirestore(preferenceManager.getString(Constants.KEY_USER_ID), ID, distance);
                    } else {
                        // 處理文檔不存在的情況
                        Log.d("DistanceData", "錯誤2");
                    }
                })
                .addOnFailureListener(e -> {
                    // 處理失敗的情況
                    Log.d("DistanceData", "錯誤3");
                });
    }

    private void saveDistanceToFirestore(String myUid, String productUid, String distance) {

        // 创建一个用户距离的 Map
        Map<String, Object> userDistance = new HashMap<>();
        userDistance.put(productUid, distance);

        // 存储用户距离信息到 "distance" 集合中，以自己的 UID 为文档，其他人的 UID 为字段
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Productdistance").document(myUid)
                .set(userDistance, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "商品距离已成功更新或创建"))
                .addOnFailureListener(e -> Log.e("Firestore", "错误：" + e.getMessage()));
    }
}