package tw.cgu.b0929035.smallshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class FrontPage extends AppCompatActivity {

    FragmentManager fmgr;
    F1Fragment f1;
    F2Fragment f2;
    F3Fragment f3;
    F4Fragment f4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);

        fmgr = getSupportFragmentManager();
        f1 = new F1Fragment();
        f2 = new F2Fragment();
        f3 = new F3Fragment();
        f4 = new F4Fragment();

        FragmentTransaction transaction = fmgr.beginTransaction();
        transaction.add(R.id.container, f1);
        transaction.commit();
    }

    public void setF1(View view) {
        FragmentTransaction transaction = fmgr.beginTransaction();
        transaction.replace(R.id.container, f1);
        transaction.commit();
    }
    public void setF2(View view) {
        FragmentTransaction transaction = fmgr.beginTransaction();
        transaction.replace(R.id.container, f2);
        transaction.commit();
    }
    public void setF3(View view) {
        FragmentTransaction transaction = fmgr.beginTransaction();
        transaction.replace(R.id.container, f3);
        transaction.commit();
    }
    public void setF4(View view) {
        FragmentTransaction transaction = fmgr.beginTransaction();
        transaction.replace(R.id.container, f4);
        transaction.commit();
    }
}