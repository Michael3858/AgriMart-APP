package tw.cgu.b0929035.smallshop;

/** Optional private configuration for the preserved PayPal Sandbox integration. */
public final class ServiceConfig {
    private ServiceConfig() {
    }

    public static boolean isMapsConfigured() {
        return !BuildConfig.AGRIMART_MAPS_API_KEY.trim().isEmpty();
    }

    public static boolean hasPayPalClientId() {
        return !BuildConfig.AGRIMART_PAYPAL_CLIENT_ID.trim().isEmpty();
    }
}
