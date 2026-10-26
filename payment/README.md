# Stripe 付款流程練習

獨立的 Android 練習專案，使用 Kotlin、Retrofit 與 Stripe PaymentSheet，練習付款介面的呼叫流程、HTTP 請求與回應資料處理。

## 程式導覽

| 檔案 | 內容 |
|---|---|
| [MainActivity.kt](app/src/main/java/com/example/payment/MainActivity.kt) | 付款流程控制與 PaymentSheet 回呼處理。 |
| [ApiInterface.kt](app/src/main/java/com/example/payment/API/ApiInterface.kt) | Customer、Ephemeral Key 與 PaymentIntent 的請求介面。 |
| [Utils.kt](app/src/main/java/com/example/payment/Utils.kt) | 付款設定與啟用開關。 |

## 開發環境

Kotlin 1.8.0、Android Gradle Plugin 8.1.0、Gradle 8.0，compile SDK 34，最低與目標 SDK 33。主要套件為 Stripe Android 20.34.2 與 Retrofit 2.9.0。

使用 Android Studio 開啟本資料夾。此練習預設停用付款請求，不包含服務金鑰。
