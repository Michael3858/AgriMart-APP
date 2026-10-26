package tw.cgu.b0929035.smallshop;

import static android.content.Context.MODE_PRIVATE;

import static com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.SetOptions;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import android.Manifest;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class F1Fragment extends Fragment implements TextToSpeech.OnInitListener {

    String lan;

    EditText SearchBar;
    ImageButton Search, ShoppingCart, Sell;
    Button Crop, Vegetable, Fruit, Poultry, DairyProduct, Others;
    TextView textView;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    MyAdapter myAdapter;
    private FirebaseFirestore db;
    private AtomicInteger pendingQueries = new AtomicInteger(0); // 用于跟踪未完成查询的计数器
    private Map<String, String> tempUserNames = new HashMap<>(); // 临时存储用户名的Map
    ArrayList<String> name = new ArrayList<>();
    ArrayList<String> pname = new ArrayList<>();
    ArrayList<String> price = new ArrayList<>();
    ArrayList<String> url = new ArrayList<>();
    ArrayList<String> IDlist = new ArrayList<>();
    ArrayList<String> productOwnerIds = new ArrayList<>();
    /*String[] name = {"高麗菜", "青花菜"};
    String[] pname = {"林麥可", "麥可林"};
    String[] price = {"160~200", "150~200"};
    String[] url = {"https://cc.tvbs.com.tw/img/program/upload/2021/03/04/20210304162322-de58f0bf.jpg", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRGoK89UX589WhlYIdFY68xvoM9G6wEctNPyA&usqp=CAU"};*/
    private final int REQ_CODE_SPEECH_INPUT = 101; // 可以根据需要更改请求代码
    private PreferenceManager preferenceManager;
    ImageButton voiceInputButton; // 语音输入按钮
    private TextToSpeech textToSpeech;

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 請求位置權限
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    requireActivity().runOnUiThread(() -> {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        saveUserLocationToFirestore(latitude, longitude);
                        getUserLocations(latitude, longitude);
                    });
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            });
        } else {
            Log.d("YourFragment", "error");
        }
    }

    private void saveUserLocationToFirestore(double latitude, double longitude) {
        Map<String, Object> userLocation = new HashMap<>();
        userLocation.put("latitude", latitude);
        userLocation.put("longitude", longitude);

        db.collection("userLocations")
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .set(userLocation)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "用戶位置已成功添加"))
                .addOnFailureListener(e -> Log.e("Firestore", "錯誤：" + e.getMessage()));
    }

    private float calculateDistance(double myLatitude, double myLongitude, double userLatitude, double userLongitude) {

        double myLatitudeInDegrees = Math.toDegrees(myLatitude);
        double myLongitudeInDegrees = Math.toDegrees(myLongitude);
        double userLatitudeInDegrees = Math.toDegrees(userLatitude);
        double userLongitudeInDegrees = Math.toDegrees(userLongitude);

        Location myLocation = new Location("My Location");
        myLocation.setLatitude(myLatitudeInDegrees);
        myLocation.setLongitude(myLongitudeInDegrees);
        Log.d("Location", "My Latitude: " + myLatitude + ", My Longitude: " + myLongitude);

        Location userLocation = new Location("User Location");
        userLocation.setLatitude(userLatitudeInDegrees);
        userLocation.setLongitude(userLongitudeInDegrees);
        Log.d("userLocation", "user Latitude: " + userLatitude + ", user Longitude: " + userLongitude);

        // 計算距離
        return myLocation.distanceTo(userLocation);

    }

    private void getUserLocations(double myLatitude, double myLongitude) {
        CollectionReference userLocationsRef = db.collection("userLocations");

        userLocationsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // 获取每个用户的位置信息
                        String userId = documentSnapshot.getId();
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");

                        // 现在你可以处理每个用户的位置信息
                        if (latitude != null && longitude != null) {
                            // 计算距离
                            float distance = calculateDistance(myLatitude, myLongitude, latitude, longitude);
                            Log.d("Distance", "距离: " + distance + " 公尺");
                            saveDistanceToFirestore(preferenceManager.getString(Constants.KEY_USER_ID), userId, distance);
                        } else {
                            Log.e("UserLocation", "无效的用户位置信息");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // 处理错误
                });
    }

    private void saveDistanceToFirestore(String myUid, String otherUid, float distance) {

        // 创建一个用户距离的 Map
        Map<String, Object> userDistance = new HashMap<>();
        String dis = String.valueOf(distance);
        userDistance.put(otherUid, dis);

        // 存储用户距离信息到 "distance" 集合中，以自己的 UID 为文档，其他人的 UID 为字段
        db.collection("distance").document(myUid)
                .set(userDistance, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "用户距离已成功更新或创建"))
                .addOnFailureListener(e -> Log.e("Firestore", "错误：" + e.getMessage()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requireActivity().runOnUiThread(this::startLocationUpdates);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(requireContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getLocation();
        String myUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        findClosestPeople(myUserId);
        SharedPreferences language = requireActivity().getSharedPreferences("Language", MODE_PRIVATE);
        lan = language.getString("lan", "");

        View rootView = inflater.inflate(R.layout.fragment_f1, container, false);
        SearchBar = (EditText) rootView.findViewById(R.id.SearchBar);
        List<String> productNameList = new ArrayList<>();
        List<String> productIDList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference productRef = db.collection("products");
        productRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                productNameList.clear();
                productIDList.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String name = document.getString(Constants.KEY_PRODUCT_NAME);
                        productNameList.add(name);
                        productIDList.add(document.getId());
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
        ArrayList<String> searchIDList = new ArrayList<>();
        ArrayList<String> Search1 = new ArrayList<>();
        ArrayList<String> Search2 = new ArrayList<>();
        ArrayList<String> Search3 = new ArrayList<>();
        ArrayList<String> Search4 = new ArrayList<>();
        ArrayList<String> Search5 = new ArrayList<>();
        Search = (ImageButton) rootView.findViewById(R.id.Search);
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchIDList.clear();
                Search1.clear();
                Search2.clear();
                Search3.clear();
                Search4.clear();
                Search5.clear();
                String sear = SearchBar.getText().toString();
                for (int i = 0; i < productNameList.size(); i++) {
                    if (productNameList.get(i).contains(sear)) {
                        searchIDList.add(productIDList.get(i));
                    }
                }
                for (int i = 0; i < searchIDList.size(); i++) {
                    Search1.add(null);
                    Search2.add(null);
                    Search3.add(null);
                    Search4.add(null);
                    Search5.add(null);
                }
                if (searchIDList.size() == 0) {
                    Toast.makeText(getContext(), "找不到", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseFirestore fb = FirebaseFirestore.getInstance();
                    AtomicInteger outerCounter = new AtomicInteger(searchIDList.size());
                    AtomicInteger innerCounter = new AtomicInteger(0);
                    for (int j = 0; j < searchIDList.size(); j++) {
                        int finalj = j;
                        DocumentReference docRef = fb.collection(Constants.KEY_COLLECTION_PRODUCT).document(searchIDList.get(j));
                        docRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Search1.set(finalj, documentSnapshot.getString("imageUrl"));
                                Search2.set(finalj, documentSnapshot.getString(Constants.KEY_PRODUCT_NAME));
                                Search3.set(finalj, documentSnapshot.getString(Constants.KEY_PRODUCT_PRICE));
                                String productOwner = documentSnapshot.getString(Constants.KEY_PRODUCT_OWNER);
                                CollectionReference usersRef = fb.collection(Constants.KEY_COLLECTION_USERS);
                                DocumentReference userDocumentRef = usersRef.document(productOwner);
                                innerCounter.incrementAndGet();
                                userDocumentRef.get().addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        Map<String, Object> userData = documentSnapshot2.getData();
                                        String name = (String) userData.get("name");
                                        Search4.set(finalj, name);

                                    } else {

                                    }
                                    innerCounter.decrementAndGet();
                                    if (outerCounter.get() == 0 && innerCounter.get() == 0) {
                                        SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Search1", MODE_PRIVATE).edit();
                                        edit1.putInt("num", Search1.size());
                                        for (int i = 0; i < Search1.size(); i++) {
                                            edit1.putString("item_" + i, Search1.get(i));
                                        }
                                        edit1.commit();
                                        SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Search2", MODE_PRIVATE).edit();
                                        edit2.putInt("num", Search2.size());
                                        for (int i = 0; i < Search2.size(); i++) {
                                            edit2.putString("item_" + i, Search2.get(i));
                                        }
                                        edit2.commit();
                                        SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Search3", MODE_PRIVATE).edit();
                                        edit3.putInt("num", Search3.size());
                                        for (int i = 0; i < Search3.size(); i++) {
                                            edit3.putString("item_" + i, Search3.get(i));
                                        }
                                        edit3.commit();
                                        SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Search4", MODE_PRIVATE).edit();
                                        edit4.putInt("num", Search4.size());
                                        for (int i = 0; i < Search4.size(); i++) {
                                            edit4.putString("item_" + i, Search4.get(i));
                                        }
                                        edit4.commit();
                                        SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Search5", MODE_PRIVATE).edit();
                                        edit5.putInt("num", Search5.size());
                                        for (int i = 0; i < Search5.size(); i++) {
                                            edit5.putString("item_" + i, Search5.get(i));
                                        }
                                        edit5.commit();
                                        SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Search6", MODE_PRIVATE).edit();
                                        edit6.putInt("num", searchIDList.size());
                                        for (int i = 0; i < searchIDList.size(); i++) {
                                            edit6.putString("item_" + i, searchIDList.get(i));
                                        }
                                        edit6.commit();
                                        Intent intent = new Intent(getContext(), SearchList.class);
                                        startActivity(intent);
                                    }
                                });
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
                                    Search5.set(finalj, t);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                outerCounter.decrementAndGet();
                            }
                        });
                    }
                }
            }
        });
        ArrayList<String> Cart1 = new ArrayList<>();
        ArrayList<String> Cart2 = new ArrayList<>();
        ArrayList<String> Cart3 = new ArrayList<>();
        ArrayList<String> Cart4 = new ArrayList<>();
        ArrayList<String> Cart5 = new ArrayList<>();
        ArrayList<String> Cart6 = new ArrayList<>();
        FirebaseFirestore firebasedatabase = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebasedatabase.collection("ShoppingCart");
        CollectionReference Ref2 = firebasedatabase.collection(Constants.KEY_COLLECTION_PRODUCT);
        CollectionReference Ref3 = firebasedatabase.collection(Constants.KEY_COLLECTION_USERS);
        preferenceManager = new PreferenceManager(getContext());
        String userID = preferenceManager.getString(Constants.KEY_USER_ID);
        Query quer = Ref.whereEqualTo("userID", userID);
        quer.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Cart1.clear();
            Cart2.clear();
            Cart3.clear();
            Cart4.clear();
            Cart5.clear();
            Cart6.clear();
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                String ProductID = document.getString("productID");
                DocumentReference DocumentRef = Ref2.document(ProductID);
                DocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> productData = documentSnapshot.getData();
                        Cart1.add((String) productData.get("imageUrl"));
                        Cart2.add((String) productData.get(Constants.KEY_PRODUCT_NAME));
                        Cart3.add((String) productData.get(Constants.KEY_PRODUCT_PRICE) + "$");
                        String productOwner = (String) productData.get(Constants.KEY_PRODUCT_OWNER);
                        DocumentReference userDocumentRef = Ref3.document(productOwner);
                        userDocumentRef.get().addOnSuccessListener(documentSnapshot2 -> {
                            if (documentSnapshot2.exists()) {
                                Map<String, Object> userData = documentSnapshot2.getData();
                                String name = (String) userData.get("name");
                                Cart4.add(name);

                            } else {

                            }
                        }).addOnFailureListener(e -> {

                        });
                        String productTime = (String) productData.get(Constants.KEY_PRODUCT_TIME);
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
                            Cart5.add(t);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        String productID = documentSnapshot.getId();
                        Cart6.add(productID);
                    } else {

                    }
                }).addOnFailureListener(e -> {

                });
            }
        }).addOnFailureListener(e -> {

        });
        ShoppingCart = (ImageButton) rootView.findViewById(R.id.ShoppingCart);
        ShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Cart1.size() == Cart2.size() && Cart2.size() == Cart3.size() && Cart3.size() == Cart4.size() && Cart4.size() == Cart5.size() && Cart5.size() == Cart6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Cart1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Cart1.size());
                    for (int i = 0; i < Cart1.size(); i++) {
                        edit1.putString("item_" + i, Cart1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Cart2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Cart2.size());
                    for (int i = 0; i < Cart2.size(); i++) {
                        edit2.putString("item_" + i, Cart2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Cart3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Cart3.size());
                    for (int i = 0; i < Cart3.size(); i++) {
                        edit3.putString("item_" + i, Cart3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Cart4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Cart4.size());
                    for (int i = 0; i < Cart4.size(); i++) {
                        edit4.putString("item_" + i, Cart4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Cart5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Cart5.size());
                    for (int i = 0; i < Cart5.size(); i++) {
                        edit5.putString("item_" + i, Cart5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Cart6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Cart6.size());
                    for (int i = 0; i < Cart6.size(); i++) {
                        edit6.putString("item_" + i, Cart6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), ShoppingCart.class);
                    startActivity(intent);
                }
            }
        });
        Sell = (ImageButton) rootView.findViewById(R.id.Sell);
        Sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Sell.class);
                startActivity(intent);
            }
        });

        // 初始化 TextToSpeech
        textToSpeech = new TextToSpeech(requireContext(), this);

        ArrayList<String> Crop1 = new ArrayList<>();
        ArrayList<String> Crop2 = new ArrayList<>();
        ArrayList<String> Crop3 = new ArrayList<>();
        ArrayList<String> Crop4 = new ArrayList<>();
        ArrayList<String> Crop5 = new ArrayList<>();
        ArrayList<String> Crop6 = new ArrayList<>();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference usersRef = database.collection(Constants.KEY_COLLECTION_PRODUCT);
        CollectionReference usersRef2 = database.collection(Constants.KEY_COLLECTION_USERS);
        List<String> classifiableValues = Arrays.asList("穀物", "Crops");
        Query query = usersRef.whereIn(Constants.KEY_PRODUCT_CLASSIFIABLE, classifiableValues);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Crop1.clear();
            Crop2.clear();
            Crop3.clear();
            Crop4.clear();
            Crop5.clear();
            Crop6.clear();
            Log.d("FirestoreDebug", "Query successful. Number of documents: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Document data: " + document.getData().toString());
                //Log.d("FirestoreDebug", "test: " + Crop5.size());
                if (document.contains("imageUrl")) {
                    String imageUrl = document.getString("imageUrl");
                    Crop1.add(imageUrl);
                }
                if (document.contains(Constants.KEY_PRODUCT_NAME)) {
                    String productName = document.getString(Constants.KEY_PRODUCT_NAME);
                    Crop2.add(productName);
                }
                if (document.contains(Constants.KEY_PRODUCT_PRICE)) {
                    String productPrice = document.getString(Constants.KEY_PRODUCT_PRICE) + "$";
                    Crop3.add(productPrice);
                }
                if (document.contains(Constants.KEY_PRODUCT_OWNER)) {
                    String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
                    DocumentReference userDocumentRef = usersRef2.document(productOwner);
                    userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            String name = (String) userData.get("name");
                            Crop4.add(name);

                        } else {

                        }
                    }).addOnFailureListener(e -> {

                    });
                }
                if (document.contains(Constants.KEY_PRODUCT_TIME)) {
                    String productTime = document.getString(Constants.KEY_PRODUCT_TIME);
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
                        Crop5.add(t);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                String ID = document.getId();
                Crop6.add(ID);
            }
        }).addOnFailureListener(e -> {

        });
        Crop = (Button) rootView.findViewById(R.id.Crop);
        Crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Crop1.size() == Crop2.size() && Crop2.size() == Crop3.size() && Crop3.size() == Crop4.size() && Crop4.size() == Crop5.size() && Crop5.size() == Crop6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Crop1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Crop1.size());
                    for (int i = 0; i < Crop1.size(); i++) {
                        edit1.putString("item_" + i, Crop1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Crop2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Crop2.size());
                    for (int i = 0; i < Crop2.size(); i++) {
                        edit2.putString("item_" + i, Crop2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Crop3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Crop3.size());
                    for (int i = 0; i < Crop3.size(); i++) {
                        edit3.putString("item_" + i, Crop3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Crop4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Crop4.size());
                    for (int i = 0; i < Crop4.size(); i++) {
                        edit4.putString("item_" + i, Crop4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Crop5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Crop5.size());
                    for (int i = 0; i < Crop5.size(); i++) {
                        edit5.putString("item_" + i, Crop5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Crop6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Crop6.size());
                    for (int i = 0; i < Crop6.size(); i++) {
                        edit6.putString("item_" + i, Crop6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), CropList.class);
                    startActivity(intent);
                }
            }
        });
        ArrayList<String> Vege1 = new ArrayList<>();
        ArrayList<String> Vege2 = new ArrayList<>();
        ArrayList<String> Vege3 = new ArrayList<>();
        ArrayList<String> Vege4 = new ArrayList<>();
        ArrayList<String> Vege5 = new ArrayList<>();
        ArrayList<String> Vege6 = new ArrayList<>();
        List<String> classifiableValues2 = Arrays.asList("蔬菜", "Vegetables");
        Query query2 = usersRef.whereIn(Constants.KEY_PRODUCT_CLASSIFIABLE, classifiableValues2);
        query2.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Vege1.clear();
            Vege2.clear();
            Vege3.clear();
            Vege4.clear();
            Vege5.clear();
            Vege6.clear();
            Log.d("FirestoreDebug", "Query successful. Number of documents: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Document data: " + document.getData().toString());
                if (document.contains("imageUrl")) {
                    String imageUrl = document.getString("imageUrl");
                    Vege1.add(imageUrl);
                }
                if (document.contains(Constants.KEY_PRODUCT_NAME)) {
                    String productName = document.getString(Constants.KEY_PRODUCT_NAME);
                    Vege2.add(productName);
                }
                if (document.contains(Constants.KEY_PRODUCT_PRICE)) {
                    String productPrice = document.getString(Constants.KEY_PRODUCT_PRICE) + "$";
                    Vege3.add(productPrice);
                }
                if (document.contains(Constants.KEY_PRODUCT_OWNER)) {
                    String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
                    DocumentReference userDocumentRef = usersRef2.document(productOwner);
                    userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            String name = (String) userData.get("name");
                            Vege4.add(name);

                        } else {

                        }
                    }).addOnFailureListener(e -> {

                    });
                }
                if (document.contains(Constants.KEY_PRODUCT_TIME)) {
                    String productTime = document.getString(Constants.KEY_PRODUCT_TIME);
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
                        Vege5.add(t);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                String ID = document.getId();
                Vege6.add(ID);
            }
        }).addOnFailureListener(e -> {

        });
        Vegetable = (Button) rootView.findViewById(R.id.Vegetable);
        Vegetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Vege1.size() == Vege2.size() && Vege2.size() == Vege3.size() && Vege3.size() == Vege4.size() && Vege4.size() == Vege5.size() && Vege5.size() == Vege6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Vege1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Vege1.size());
                    for (int i = 0; i < Vege1.size(); i++) {
                        edit1.putString("item_" + i, Vege1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Vege2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Vege2.size());
                    for (int i = 0; i < Vege2.size(); i++) {
                        edit2.putString("item_" + i, Vege2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Vege3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Vege3.size());
                    for (int i = 0; i < Vege3.size(); i++) {
                        edit3.putString("item_" + i, Vege3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Vege4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Vege4.size());
                    for (int i = 0; i < Vege4.size(); i++) {
                        edit4.putString("item_" + i, Vege4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Vege5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Vege5.size());
                    for (int i = 0; i < Vege5.size(); i++) {
                        edit5.putString("item_" + i, Vege5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Vege6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Vege6.size());
                    for (int i = 0; i < Vege6.size(); i++) {
                        edit6.putString("item_" + i, Vege6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), VegeList.class);
                    startActivity(intent);
                }
            }
        });
        ArrayList<String> Fruit1 = new ArrayList<>();
        ArrayList<String> Fruit2 = new ArrayList<>();
        ArrayList<String> Fruit3 = new ArrayList<>();
        ArrayList<String> Fruit4 = new ArrayList<>();
        ArrayList<String> Fruit5 = new ArrayList<>();
        ArrayList<String> Fruit6 = new ArrayList<>();
        List<String> classifiableValues3 = Arrays.asList("水果", "Fruits");
        Query query3 = usersRef.whereIn(Constants.KEY_PRODUCT_CLASSIFIABLE, classifiableValues3);
        query3.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Fruit1.clear();
            Fruit2.clear();
            Fruit3.clear();
            Fruit4.clear();
            Fruit5.clear();
            Fruit6.clear();
            Log.d("FirestoreDebug", "Query successful. Number of documents: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Document data: " + document.getData().toString());
                if (document.contains("imageUrl")) {
                    String imageUrl = document.getString("imageUrl");
                    Fruit1.add(imageUrl);
                }
                if (document.contains(Constants.KEY_PRODUCT_NAME)) {
                    String productName = document.getString(Constants.KEY_PRODUCT_NAME);
                    Fruit2.add(productName);
                }
                if (document.contains(Constants.KEY_PRODUCT_PRICE)) {
                    String productPrice = document.getString(Constants.KEY_PRODUCT_PRICE) + "$";
                    Fruit3.add(productPrice);
                }
                if (document.contains(Constants.KEY_PRODUCT_OWNER)) {
                    String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
                    DocumentReference userDocumentRef = usersRef2.document(productOwner);
                    userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            String name = (String) userData.get("name");
                            Fruit4.add(name);

                        } else {

                        }
                    }).addOnFailureListener(e -> {

                    });
                }
                if (document.contains(Constants.KEY_PRODUCT_TIME)) {
                    String productTime = document.getString(Constants.KEY_PRODUCT_TIME);
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
                        Fruit5.add(t);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                String ID = document.getId();
                Fruit6.add(ID);
            }
        }).addOnFailureListener(e -> {

        });
        Fruit = (Button) rootView.findViewById(R.id.Fruit);
        Fruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Fruit1.size() == Fruit2.size() && Fruit2.size() == Fruit3.size() && Fruit3.size() == Fruit4.size() && Fruit4.size() == Fruit5.size() && Fruit5.size() == Fruit6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Fruit1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Fruit1.size());
                    for (int i = 0; i < Fruit1.size(); i++) {
                        edit1.putString("item_" + i, Fruit1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Fruit2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Fruit2.size());
                    for (int i = 0; i < Fruit2.size(); i++) {
                        edit2.putString("item_" + i, Fruit2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Fruit3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Fruit3.size());
                    for (int i = 0; i < Fruit3.size(); i++) {
                        edit3.putString("item_" + i, Fruit3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Fruit4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Fruit4.size());
                    for (int i = 0; i < Fruit4.size(); i++) {
                        edit4.putString("item_" + i, Fruit4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Fruit5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Fruit5.size());
                    for (int i = 0; i < Fruit5.size(); i++) {
                        edit5.putString("item_" + i, Fruit5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Fruit6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Fruit6.size());
                    for (int i = 0; i < Fruit6.size(); i++) {
                        edit6.putString("item_" + i, Fruit6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), FruitList.class);
                    startActivity(intent);
                }
            }
        });
        ArrayList<String> Poultry1 = new ArrayList<>();
        ArrayList<String> Poultry2 = new ArrayList<>();
        ArrayList<String> Poultry3 = new ArrayList<>();
        ArrayList<String> Poultry4 = new ArrayList<>();
        ArrayList<String> Poultry5 = new ArrayList<>();
        ArrayList<String> Poultry6 = new ArrayList<>();
        List<String> classifiableValues4 = Arrays.asList("家禽", "Poultry");
        Query query4 = usersRef.whereIn(Constants.KEY_PRODUCT_CLASSIFIABLE, classifiableValues4);
        query4.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Poultry1.clear();
            Poultry2.clear();
            Poultry3.clear();
            Poultry4.clear();
            Poultry5.clear();
            Poultry6.clear();
            Log.d("FirestoreDebug", "Query successful. Number of documents: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Document data: " + document.getData().toString());
                if (document.contains("imageUrl")) {
                    String imageUrl = document.getString("imageUrl");
                    Poultry1.add(imageUrl);
                }
                if (document.contains(Constants.KEY_PRODUCT_NAME)) {
                    String productName = document.getString(Constants.KEY_PRODUCT_NAME);
                    Poultry2.add(productName);
                }
                if (document.contains(Constants.KEY_PRODUCT_PRICE)) {
                    String productPrice = document.getString(Constants.KEY_PRODUCT_PRICE) + "$";
                    Poultry3.add(productPrice);
                }
                if (document.contains(Constants.KEY_PRODUCT_OWNER)) {
                    String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
                    DocumentReference userDocumentRef = usersRef2.document(productOwner);
                    userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            String name = (String) userData.get("name");
                            Poultry4.add(name);

                        } else {

                        }
                    }).addOnFailureListener(e -> {

                    });
                }
                if (document.contains(Constants.KEY_PRODUCT_TIME)) {
                    String productTime = document.getString(Constants.KEY_PRODUCT_TIME);
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
                        Poultry5.add(t);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                String ID = document.getId();
                Poultry6.add(ID);
            }
        }).addOnFailureListener(e -> {

        });
        Poultry = (Button) rootView.findViewById(R.id.Poultry);
        Poultry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Poultry1.size() == Poultry2.size() && Poultry2.size() == Poultry3.size() && Poultry3.size() == Poultry4.size() && Poultry4.size() == Poultry5.size() && Poultry5.size() == Poultry6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Poultry1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Poultry1.size());
                    for (int i = 0; i < Poultry1.size(); i++) {
                        edit1.putString("item_" + i, Poultry1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Poultry2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Poultry2.size());
                    for (int i = 0; i < Poultry2.size(); i++) {
                        edit2.putString("item_" + i, Poultry2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Poultry3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Poultry3.size());
                    for (int i = 0; i < Poultry3.size(); i++) {
                        edit3.putString("item_" + i, Poultry3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Poultry4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Poultry4.size());
                    for (int i = 0; i < Poultry4.size(); i++) {
                        edit4.putString("item_" + i, Poultry4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Poultry5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Poultry5.size());
                    for (int i = 0; i < Poultry5.size(); i++) {
                        edit5.putString("item_" + i, Poultry5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Poultry6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Poultry6.size());
                    for (int i = 0; i < Poultry6.size(); i++) {
                        edit6.putString("item_" + i, Poultry6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), PoultryList.class);
                    startActivity(intent);
                }
            }
        });
        ArrayList<String> Dairy1 = new ArrayList<>();
        ArrayList<String> Dairy2 = new ArrayList<>();
        ArrayList<String> Dairy3 = new ArrayList<>();
        ArrayList<String> Dairy4 = new ArrayList<>();
        ArrayList<String> Dairy5 = new ArrayList<>();
        ArrayList<String> Dairy6 = new ArrayList<>();
        List<String> classifiableValues5 = Arrays.asList("乳製品", "DairyProduct");
        Query query5 = usersRef.whereIn(Constants.KEY_PRODUCT_CLASSIFIABLE, classifiableValues5);
        query5.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Dairy1.clear();
            Dairy2.clear();
            Dairy3.clear();
            Dairy4.clear();
            Dairy5.clear();
            Dairy6.clear();
            Log.d("FirestoreDebug", "Query successful. Number of documents: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Document data: " + document.getData().toString());
                if (document.contains("imageUrl")) {
                    String imageUrl = document.getString("imageUrl");
                    Dairy1.add(imageUrl);
                }
                if (document.contains(Constants.KEY_PRODUCT_NAME)) {
                    String productName = document.getString(Constants.KEY_PRODUCT_NAME);
                    Dairy2.add(productName);
                }
                if (document.contains(Constants.KEY_PRODUCT_PRICE)) {
                    String productPrice = document.getString(Constants.KEY_PRODUCT_PRICE) + "$";
                    Dairy3.add(productPrice);
                }
                if (document.contains(Constants.KEY_PRODUCT_OWNER)) {
                    String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
                    DocumentReference userDocumentRef = usersRef2.document(productOwner);
                    userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            String name = (String) userData.get("name");
                            Dairy4.add(name);

                        } else {

                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FirestoreDebug", "Query failed: " + e.getMessage());
                    });
                }
                if (document.contains(Constants.KEY_PRODUCT_TIME)) {
                    String productTime = document.getString(Constants.KEY_PRODUCT_TIME);
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
                        Dairy5.add(t);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                String ID = document.getId();
                Dairy6.add(ID);
            }
        }).addOnFailureListener(e -> {

        });
        DairyProduct = (Button) rootView.findViewById(R.id.DairyProduct);
        DairyProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Dairy1.size() == Dairy2.size() && Dairy2.size() == Dairy3.size() && Dairy3.size() == Dairy4.size() && Dairy4.size() == Dairy5.size() && Dairy5.size() == Dairy6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Dairy1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Dairy1.size());
                    for (int i = 0; i < Dairy1.size(); i++) {
                        edit1.putString("item_" + i, Dairy1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Dairy2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Dairy2.size());
                    for (int i = 0; i < Dairy2.size(); i++) {
                        edit2.putString("item_" + i, Dairy2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Dairy3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Dairy3.size());
                    for (int i = 0; i < Dairy3.size(); i++) {
                        edit3.putString("item_" + i, Dairy3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Dairy4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Dairy4.size());
                    for (int i = 0; i < Dairy4.size(); i++) {
                        edit4.putString("item_" + i, Dairy4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Dairy5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Dairy5.size());
                    for (int i = 0; i < Dairy5.size(); i++) {
                        edit5.putString("item_" + i, Dairy5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Dairy6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Dairy6.size());
                    for (int i = 0; i < Dairy6.size(); i++) {
                        edit6.putString("item_" + i, Dairy6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), DairyList.class);
                    startActivity(intent);
                }
            }
        });
        ArrayList<String> Other1 = new ArrayList<>();
        ArrayList<String> Other2 = new ArrayList<>();
        ArrayList<String> Other3 = new ArrayList<>();
        ArrayList<String> Other4 = new ArrayList<>();
        ArrayList<String> Other5 = new ArrayList<>();
        ArrayList<String> Other6 = new ArrayList<>();
        List<String> classifiableValues6 = Arrays.asList("其他", "Others");
        Query query6 = usersRef.whereIn(Constants.KEY_PRODUCT_CLASSIFIABLE, classifiableValues6);
        query6.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Other1.clear();
            Other2.clear();
            Other3.clear();
            Other4.clear();
            Other5.clear();
            Other6.clear();
            Log.d("FirestoreDebug", "Query successful. Number of documents: " + queryDocumentSnapshots.size());
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Log.d("FirestoreDebug", "Document data: " + document.getData().toString());
                if (document.contains("imageUrl")) {
                    String imageUrl = document.getString("imageUrl");
                    Other1.add(imageUrl);
                }
                if (document.contains(Constants.KEY_PRODUCT_NAME)) {
                    String productName = document.getString(Constants.KEY_PRODUCT_NAME);
                    Other2.add(productName);
                }
                if (document.contains(Constants.KEY_PRODUCT_PRICE)) {
                    String productPrice = document.getString(Constants.KEY_PRODUCT_PRICE) + "$";
                    Other3.add(productPrice);
                }
                if (document.contains(Constants.KEY_PRODUCT_OWNER)) {
                    String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
                    DocumentReference userDocumentRef = usersRef2.document(productOwner);
                    userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            String name = (String) userData.get("name");
                            Other4.add(name);

                        } else {

                        }
                    }).addOnFailureListener(e -> {
                        Log.e("FirestoreDebug", "Query failed: " + e.getMessage());
                    });
                }
                if (document.contains(Constants.KEY_PRODUCT_TIME)) {
                    String productTime = document.getString(Constants.KEY_PRODUCT_TIME);
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
                        Other5.add(t);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                String ID = document.getId();
                Other6.add(ID);
            }
        }).addOnFailureListener(e -> {

        });
        Others = (Button) rootView.findViewById(R.id.Others);
        Others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Other1.size() == Other2.size() && Other2.size() == Other3.size() && Other3.size() == Other4.size() && Other4.size() == Other5.size() && Other5.size() == Other6.size()) {
                    SharedPreferences.Editor edit1 = getActivity().getSharedPreferences("Other1", MODE_PRIVATE).edit();
                    edit1.putInt("num", Other1.size());
                    for (int i = 0; i < Other1.size(); i++) {
                        edit1.putString("item_" + i, Other1.get(i));
                    }
                    edit1.commit();
                    SharedPreferences.Editor edit2 = getActivity().getSharedPreferences("Other2", MODE_PRIVATE).edit();
                    edit2.putInt("num", Other2.size());
                    for (int i = 0; i < Other2.size(); i++) {
                        edit2.putString("item_" + i, Other2.get(i));
                    }
                    edit2.commit();
                    SharedPreferences.Editor edit3 = getActivity().getSharedPreferences("Other3", MODE_PRIVATE).edit();
                    edit3.putInt("num", Other3.size());
                    for (int i = 0; i < Other3.size(); i++) {
                        edit3.putString("item_" + i, Other3.get(i));
                    }
                    edit3.commit();
                    SharedPreferences.Editor edit4 = getActivity().getSharedPreferences("Other4", MODE_PRIVATE).edit();
                    edit4.putInt("num", Other4.size());
                    for (int i = 0; i < Other4.size(); i++) {
                        edit4.putString("item_" + i, Other4.get(i));
                    }
                    edit4.commit();
                    SharedPreferences.Editor edit5 = getActivity().getSharedPreferences("Other5", MODE_PRIVATE).edit();
                    edit5.putInt("num", Other5.size());
                    for (int i = 0; i < Other5.size(); i++) {
                        edit5.putString("item_" + i, Other5.get(i));
                    }
                    edit5.commit();
                    SharedPreferences.Editor edit6 = getActivity().getSharedPreferences("Other6", MODE_PRIVATE).edit();
                    edit6.putInt("num", Other6.size());
                    for (int i = 0; i < Other6.size(); i++) {
                        edit6.putString("item_" + i, Other6.get(i));
                    }
                    edit6.commit();
                    Intent intent = new Intent(getContext(), OtherList.class);
                    startActivity(intent);
                }
            }
        });
        textView = (TextView) rootView.findViewById(R.id.TextView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        voiceInputButton = rootView.findViewById(R.id.Talk);
        voiceInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        if (Objects.equals(lan, "English")) {
            SearchBar.setHint("Enter the product");
            Crop.setText("Crops");
            Vegetable.setText("Vegetables");
            Vegetable.setTextSize(20);
            Fruit.setText("Fruits");
            Poultry.setText("Poultry");
            DairyProduct.setText("Dairy\nProduct");
            Others.setText("Others");
            textView.setText("The products near you");
        }
        // 初始化 TextToSpeech
        textToSpeech = new TextToSpeech(requireContext(), this);

        return rootView;
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(),
                    "Sorry, your device doesn't support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == requireActivity().RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String spokenText = result.get(0);

                // 设置语音识别结果到EditText中
                EditText searchBar = requireView().findViewById(R.id.SearchBar);
                searchBar.setText(spokenText);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 设置语音引擎的语言为中文（你可以根据需要设置其他语言）
            int result = textToSpeech.setLanguage(Locale.CHINESE);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 处理不支持中文的情况
            } else {
                // 播放你想要的语音
                String textToSpeak = "如果要賣東西，請典擊右上角的圖案，如果要搜尋，請點擊放大鏡的圖案，在最上面偏中間的位置"; // 这里设置要播放的文本
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
                textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params);
            }
        } else {
            // 处理初始化失败的情况
        }
    }

    @Override
    public void onDestroy() {
        // 在销毁页面时释放 TextToSpeech 资源
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        class MyViewHolder extends RecyclerView.ViewHolder {
            public View itemView;
            public ImageView iv;
            public TextView tv1, tv2, tv3;

            public MyViewHolder(View v) {
                super(v);
                itemView = v;

                iv = itemView.findViewById(R.id.iv);
                tv1 = itemView.findViewById(R.id.tv1);
                tv2 = itemView.findViewById(R.id.tv2);
                tv3 = itemView.findViewById(R.id.tv3);
            }
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            MyViewHolder vh = new MyViewHolder(itemView);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Picasso.get().load(url.get(position)).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).fit().into(holder.iv);
            holder.tv1.setText("商品:\n" + name.get(position));
            holder.tv2.setText("賣家:\n" + pname.get(position));
            holder.tv3.setText("價格:\n" + price.get(position) + "$");
            if (lan.equals("English")) {
                holder.tv1.setText("Product:\n" + name.get(position));
                holder.tv2.setText("Seller:\n" + pname.get(position));
                holder.tv3.setText("Price:\n" + price.get(position) + "$");
            }
            holder.iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ID = IDlist.get(position);
                    Intent intent = new Intent();
                    intent.setClass(getContext(), ProductPage.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", ID);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return name.size();
        }

    }
    private void findClosestPeople(String myUserId) {
        // 使用 myUserId 去獲取 distance 數據
        db.collection("distance").document(myUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 獲取你的 ID 對應的距離數據
                        Map<String, Object> distanceData = documentSnapshot.getData();
                        if (distanceData != null && !distanceData.isEmpty()) {
                            // 使用 Map 来保存用户ID和距离
                            Map<String, Float> userDistances = new HashMap<>();

                            // 用于跟踪未完成查询的计数器
                            AtomicInteger queriesRemaining = new AtomicInteger(distanceData.size());

                            // 遍历距离數據，找到最小的兩個距離和對應的用戶 ID
                            for (Map.Entry<String, Object> entry : distanceData.entrySet()) {
                                String otherUserId = entry.getKey();
                                String distance = (String) entry.getValue();

                                // 检查字符串是否为有效数字
                                try {
                                    float distanceValue = Float.parseFloat(distance);
                                    Query query = db.collection(Constants.KEY_COLLECTION_PRODUCT).whereEqualTo(Constants.KEY_PRODUCT_OWNER, otherUserId);
                                    query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            // 只有当用户拥有产品时，才将其距离添加到 Map 中
                                            userDistances.put(otherUserId, distanceValue);
                                        }
                                        // 当一个查询完成时，减少计数器的值
                                        if (queriesRemaining.decrementAndGet() == 0) {
                                            // 在这里处理找到的最近的两个用户
                                            processClosestUsers(userDistances);
                                        }
                                    }).addOnFailureListener(e -> {
                                        if (queriesRemaining.decrementAndGet() == 0) {
                                            processClosestUsers(userDistances);
                                        }
                                    });
                                } catch (NumberFormatException e) {
                                    // 处理无法解析为数字的情况
                                    e.printStackTrace();
                                    queriesRemaining.decrementAndGet();
                                }
                            }
                        }
                    } else {
                        // 處理文檔不存在的情況
                        Log.d("ClosestPeople", "失敗1");
                    }
                })
                .addOnFailureListener(e -> {
                    // 處理失敗的情況
                    Log.d("ClosestPeople", "失敗2");
                });
    }
    private void processClosestUsers(Map<String, Float> userDistances) {
        // 清空列表和临时Map
        name.clear();
        pname.clear();
        price.clear();
        url.clear();
        IDlist.clear();
        productOwnerIds.clear();
        tempUserNames.clear();

        // 获取最近的两个用户
        List<Map.Entry<String, Float>> sortedUsers = new ArrayList<>(userDistances.entrySet());
        sortedUsers.sort(Map.Entry.comparingByValue());

        if (sortedUsers.size() > 0) {
            // 处理最近的用户
            handleUser(sortedUsers.get(0).getKey());

            if (sortedUsers.size() > 1) {
                // 处理第二近的用户
                handleUser(sortedUsers.get(1).getKey());
            }
        }
    }

    private void handleUser(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection(Constants.KEY_COLLECTION_PRODUCT).whereEqualTo(Constants.KEY_PRODUCT_OWNER, userId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                // 添加产品信息
                addProductInfo(document);
            }
        }).addOnFailureListener(e -> {
            Log.e("FirestoreDebug", "Query failed: " + e.getMessage());
        });
    }

    private void addProductInfo(DocumentSnapshot document) {
        name.add(document.getString(Constants.KEY_PRODUCT_NAME));
        price.add(document.getString(Constants.KEY_PRODUCT_PRICE) + "$");
        url.add(document.getString("imageUrl"));
        IDlist.add(document.getId());

        String productOwner = document.getString(Constants.KEY_PRODUCT_OWNER);
        productOwnerIds.add(productOwner);
        handleOwnerInfo(productOwner);
    }

    private void handleOwnerInfo(String ownerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocumentRef = db.collection(Constants.KEY_COLLECTION_USERS).document(ownerId);

        // 每启动一个新查询，增加计数器
        pendingQueries.incrementAndGet();

        userDocumentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> userData = documentSnapshot.getData();
                String name = (String) userData.get("name");
                tempUserNames.put(ownerId, name); // 保存用户名和ID的映射
            } else {
                tempUserNames.put(ownerId, "Unknown");
            }
            checkAllQueriesDone();
        }).addOnFailureListener(e -> {
            tempUserNames.put(ownerId, "Unknown");
            checkAllQueriesDone();
        });
    }

    private void checkAllQueriesDone() {
        // 完成查询，减少计数器
        if (pendingQueries.decrementAndGet() == 0) {
            // 所有查询都完成了
            updatePnameList();
            myAdapter.notifyDataSetChanged();
            Log.d("ClosestUsers", String.valueOf(name.size()) + String.valueOf(pname.size()) + String.valueOf(price.size()) + String.valueOf(url.size()));
        }
    }

    private void updatePnameList() {
        pname.clear();
        for (String ownerId : productOwnerIds) {
            pname.add(tempUserNames.getOrDefault(ownerId, "Unknown"));
        }
    }
}