package tw.cgu.b0929035.smallshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Objects;

public class FruitList extends AppCompatActivity {

    ImageButton back;

    TextView txv;

    ListView listView;
    ArrayList<String> image = new ArrayList<>();
    ArrayList<String> txv1 = new ArrayList<>();
    ArrayList<String> txv2 = new ArrayList<>();
    ArrayList<String> txv3 = new ArrayList<>();
    ArrayList<String> txv4 = new ArrayList<>();
    ArrayList<String> IDlist = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_list);

        SharedPreferences language = getSharedPreferences("Language", MODE_PRIVATE);
        String lan = language.getString("lan", "");
        SharedPreferences List1 = getSharedPreferences("Fruit1", MODE_PRIVATE);
        int num1 = List1.getInt("num", 0);
        for(int i = 0; i < num1; i++){
            String item1 = List1.getString("item_" + i, null);
            image.add(item1);
        }
        SharedPreferences List2 = getSharedPreferences("Fruit2", MODE_PRIVATE);
        int num2 = List2.getInt("num", 0);
        for(int i = 0; i < num2; i++){
            String item2 = List2.getString("item_" + i, null);
            txv1.add(item2);
        }
        SharedPreferences List3 = getSharedPreferences("Fruit3", MODE_PRIVATE);
        int num3 = List3.getInt("num", 0);
        for(int i = 0; i < num3; i++){
            String item3 = List3.getString("item_" + i, null);
            txv2.add(item3);
        }
        SharedPreferences List4 = getSharedPreferences("Fruit4", MODE_PRIVATE);
        int num4 = List4.getInt("num", 0);
        for(int i = 0; i < num4; i++){
            String item4 = List4.getString("item_" + i, null);
            txv3.add(item4);
        }
        SharedPreferences List5 = getSharedPreferences("Fruit5", MODE_PRIVATE);
        int num5 = List5.getInt("num", 0);
        for(int i = 0; i < num5; i++){
            String item5 = List5.getString("item_" + i, null);
            txv4.add(item5);
        }
        SharedPreferences List6 = getSharedPreferences("Fruit6", MODE_PRIVATE);
        int num6 = List6.getInt("num", 0);
        for(int i = 0; i < num6; i++){
            String item6 = List6.getString("item_" + i, null);
            IDlist.add(item6);
        }
        //Toast.makeText(this, Integer.toString(txv2.size()), Toast.LENGTH_SHORT).show();

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
            txv.setText("Fruits");
        }
        FruitList.MyAdapter myAdapter = new FruitList.MyAdapter(this, image, txv1, txv2, txv3, txv4);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ID = IDlist.get(position);
                Intent intent = new Intent();
                intent.setClass(FruitList.this, ProductPage.class);
                Bundle bundle = new Bundle();
                bundle.putString("ID", ID);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> rimage;
        ArrayList<String> rtxv1;
        ArrayList<String> rtxv2;
        ArrayList<String> rtxv3;
        ArrayList<String> rtxv4;

        MyAdapter (Context context, ArrayList<String> image, ArrayList<String> txv1, ArrayList<String> txv2, ArrayList<String> txv3, ArrayList<String> txv4){
            super(context,R.layout.listview_style2,R.id.iv,image);
            this.context = context;
            this.rimage = image;
            this.rtxv1 = txv1;
            this.rtxv2 = txv2;
            this.rtxv3 = txv3;
            this.rtxv4 = txv4;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.listview_style2,parent,false);
            ImageView imageView = row.findViewById(R.id.iv);
            TextView textView1 = row.findViewById(R.id.txv1);
            TextView textView2 = row.findViewById(R.id.txv2);
            TextView textView3 = row.findViewById(R.id.txv3);
            TextView textView4 = row.findViewById(R.id.txv4);
            Picasso.get().load(rimage.get(position)).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).fit().into(imageView);
            textView1.setText(rtxv1.get(position));
            textView2.setText(rtxv2.get(position));
            textView3.setText(rtxv3.get(position));
            textView4.setText(rtxv4.get(position));
            return row;
        }
    }
}