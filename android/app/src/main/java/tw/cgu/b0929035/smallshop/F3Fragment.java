package tw.cgu.b0929035.smallshop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import tw.cgu.b0929035.smallshop.utilities.Constants;
import tw.cgu.b0929035.smallshop.utilities.PreferenceManager;

public class F3Fragment extends Fragment {
    TextView txv;

    ListView listView;
    ArrayList<String> image = new ArrayList<>();
    ArrayList<String> txv1 = new ArrayList<>();
    ArrayList<String> txv2 = new ArrayList<>();
    ArrayList<String> IDlist = new ArrayList<>();
    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_f3, container, false);
        SharedPreferences language = requireActivity().getSharedPreferences("Language", Context.MODE_PRIVATE);
        String lan = language.getString("lan", "");
        txv = (TextView) rootView.findViewById(R.id.txv);
        if(Objects.equals(lan, "English")){
            txv.setText("My Products");
        }
        listView = (ListView) rootView.findViewById(R.id.listview);
        MyAdapter myAdapter = new MyAdapter(getContext(), image, txv1, txv2);
        listView.setAdapter(myAdapter);
        FirebaseFirestore firebasedatabase = FirebaseFirestore.getInstance();
        CollectionReference Ref = firebasedatabase.collection(Constants.KEY_COLLECTION_PRODUCT);
        preferenceManager = new PreferenceManager(getContext());
        String userID = preferenceManager.getString(Constants.KEY_USER_ID);
        Query quer = Ref.whereEqualTo(Constants.KEY_PRODUCT_OWNER, userID);
        quer.get().addOnSuccessListener(queryDocumentSnapshots -> {
            image.clear();
            txv1.clear();
            txv2.clear();
            IDlist.clear();
            for (DocumentSnapshot document : queryDocumentSnapshots){
                image.add(document.getString("imageUrl"));
                txv1.add(document.getString(Constants.KEY_PRODUCT_NAME));
                txv2.add(document.getString(Constants.KEY_PRODUCT_PRICE) + "$");
                IDlist.add(document.getId());
            }
            myAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {

        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ID = IDlist.get(position);
                Intent intent = new Intent();
                intent.setClass(getContext(), ProductPage2.class);
                Bundle bundle = new Bundle();
                bundle.putString("ID", ID);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        return rootView;
    }
    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> rimage;
        ArrayList<String> rtxv1;
        ArrayList<String> rtxv2;

        MyAdapter (Context context, ArrayList<String> image, ArrayList<String> txv1, ArrayList<String> txv2){
            super(context,R.layout.listview_style1,R.id.iv,image);
            this.context = context;
            this.rimage = image;
            this.rtxv1 = txv1;
            this.rtxv2 = txv2;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.listview_style1,parent,false);
            ImageView imageView = row.findViewById(R.id.iv);
            TextView textView1 = row.findViewById(R.id.txv1);
            TextView textView2 = row.findViewById(R.id.txv2);
            Picasso.get().load(rimage.get(position)).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).fit().into(imageView);
            textView1.setText(rtxv1.get(position));
            textView2.setText(rtxv2.get(position));
            return row;
        }
    }
}