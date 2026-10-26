package tw.cgu.b0929035.smallshop.Activity;

import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.OrderRequest;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButtonContainer;

import java.util.ArrayList;

import tw.cgu.b0929035.smallshop.R;
import tw.cgu.b0929035.smallshop.ServiceConfig;

public class PaypalActivity extends AppCompatActivity {

    PaymentButtonContainer PaypalBtn;
    String TAG = "MyTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ServiceConfig.hasPayPalClientId()) {
            Toast.makeText(this, "PayPal Sandbox client ID 未設定", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_paypal);

        Bundle bundle = getIntent().getExtras();
        String ID = bundle.getString("Price");

        PaypalBtn = findViewById(R.id.payment_button_container);

        PaypalBtn.setup(
                createOrderActions -> {
                    ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                    purchaseUnits.add(
                            new PurchaseUnit.Builder()
                                    .amount(
                                            new Amount.Builder()
                                                    .currencyCode(CurrencyCode.TWD)
                                                    .value(ID)
                                                    .build()
                                    )
                                    .build()
                    );
                    OrderRequest order = new OrderRequest(
                            OrderIntent.CAPTURE,
                            new AppContext.Builder()
                                    .userAction(UserAction.PAY_NOW)
                                    .build(),
                            purchaseUnits
                    );
                    createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null); // Removed the unnecessary casting
                },
                approval -> {
                    approval.getOrderActions().capture(
                            result -> {
                                Log.i(TAG, String.format("CaptureOrderResult: %s", result));
                            }
                    );
                }
        );
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}