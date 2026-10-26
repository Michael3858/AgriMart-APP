# AgriMart

<img src="android/app/src/main/res/drawable/firstpic.png" alt="AgriMart 小農交易平台" width="140">

大學團隊專題，提供小農上架農產品、消費者搜尋與購買商品的 Android App。會員、商品與聊天資料使用 Firebase Firestore，商品圖片存放於 Firebase Storage。

以本機 `SmallShop` 作為最終版本；其餘來源作為同一專題的對照與實驗資料保存。

## 功能

- 上架商品照片、說明、價格、數量、分類與交易方式。
- 商品分類、文字與語音搜尋，以及中英文介面。
- 儲存使用者位置、計算距離，透過 Google Maps 選擇地址。
- 商品管理、購物車與 PayPal sandbox 付款介面。
- 會員資料修改與買賣雙方聊天。

現有畫面與未接齊的部分見 [SmallShop 功能導覽](docs/smallshop-features.md)。

## 專案結構

| 資料夾 | 內容 |
|---|---|
| [android](android/README.md) | 本專題採用的最終版本，來源為本機 `SmallShop`，包含商品、購物車、定位與聊天等畫面。 |
| [variants/smallshop-test](variants/smallshop-test/README.md) | `Test/SmallShop` 對照資料，保留原有差異，不覆蓋最終主程式。 |
| [prototypes/stripe-payment](prototypes/stripe-payment/README.md) | `Payment` 的獨立 Kotlin／Stripe 實驗，尚未整合主 App，預設停用。 |
| [docs](docs/smallshop-features.md) | 功能導覽、[來源定位](docs/versions.md)與 [Firestore 集合](docs/firestore-collections.md)。 |

## 開發環境

主程式與對照版使用 Java 8 語法、Android Gradle Plugin 8.1.0、Gradle 8.0、compile SDK 34、target SDK 32、最低 SDK 32。外部服務包含 Firebase、Google Maps 與 PayPal sandbox。Stripe 實驗的設定列於各自 README。

## 服務設定

1. 使用 Android Studio 開啟 `android` 資料夾。以 `tw.cgu.b0929035.smallshop` 註冊自己的 Firebase Android App，將下載的 `google-services.json` 放到 `android/app`；[設定範本](android/app/google-services.example.json) 只供格式參考。
2. 準備自己的 Firestore 與 Storage 設定，集合用途見 [Firestore 導覽](docs/firestore-collections.md)。會員登入沿用原程式對 `users` 集合的查詢方式。
3. Maps 與 PayPal 的值放在個人的 Gradle 設定（`~/.gradle/gradle.properties`），鍵名為 `AGRIMART_MAPS_API_KEY`、`AGRIMART_PAYPAL_CLIENT_ID`。PayPal 保留 sandbox 模式，未提供 client ID 時不啟用付款介面。

Firebase 設定流程可參考 [官方文件](https://firebase.google.com/docs/android/setup)。私人 Firebase 設定、原始雲端資料與付款金鑰均未包含在此 repo。

未找到獨立付款後端原始碼，本 repo 不補寫後端。Stripe 實驗保留原本的 API／PaymentSheet 程式供對照，但預設不建立付款請求。伺服器端金鑰應留在後端，不能填入 Android App；相關說明見 [Stripe 官方文件](https://docs.stripe.com/keys)。

原程式保留當時的依賴與購物流程，仍有需處理的相容性項目，例如 `Picasso.get()` 與宣告版本。原來源未附 Firebase 安全規則、索引或雲端資料，完整登入、購物與付款流程需另外設定及測試。
