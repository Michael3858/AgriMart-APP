package tw.cgu.b0929035.smallshop.firebase;

import android.app.Application;

import tw.cgu.b0929035.smallshop.BuildConfig;
import tw.cgu.b0929035.smallshop.ServiceConfig;

import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.UserAction;

public class PaypalDetail extends Application {
    String clientId = BuildConfig.AGRIMART_PAYPAL_CLIENT_ID;
    String returnUrl = "tw.cgu.b0929035.smallshop://paypalpay";

    @Override
    public void onCreate() {
        super.onCreate();
        if (!ServiceConfig.hasPayPalClientId()) {
            return;
        }
        PayPalCheckout.setConfig(new CheckoutConfig(
                this,
                clientId,
                Environment.SANDBOX,
                CurrencyCode.USD,
                UserAction.PAY_NOW,
                returnUrl
        ));
    }
}
