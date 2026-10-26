# AgriMart｜小農交易平台

<img src="android/app/src/main/res/drawable/firstpic.png" alt="AgriMart" width="140">

大學團隊專題，使用 Android 與 Firebase 開發的小農交易平台。小農可以上架與管理農產品，消費者可依分類、名稱與位置尋找商品、加入購物車，並與賣家即時聊天。付款介面使用 PayPal Sandbox 測試環境。

## 主要功能

| 功能 | 內容 |
|---|---|
| 會員管理 | 註冊、登入、記住登入狀態與修改個人資料。 |
| 商品管理 | 上傳商品照片，設定名稱、說明、價格、數量、分類與交易方式，並查看或下架自己的商品。 |
| 商品探索 | 六類商品列表、名稱搜尋、語音輸入與附近賣家商品。 |
| 購物車 | 加入、查看與移除商品。 |
| 即時聊天 | 從商品頁聯繫賣家，查看對話紀錄與傳送文字訊息。 |
| 位置服務 | 取得使用者位置、計算距離，並透過 Google Maps 選擇地址。 |
| 測試付款 | 商品價格帶入 PayPal Sandbox 付款畫面。 |
| 語言切換 | 中英文介面。 |

## 使用技術

| 項目 | 技術 |
|---|---|
| Android App | Java、XML、AndroidX、View Binding |
| 資料儲存 | Firebase Firestore |
| 圖片儲存 | Firebase Storage |
| 地圖與定位 | Google Maps SDK、Android Location |
| 付款介面 | PayPal Checkout SDK（Sandbox） |
| 付款流程練習 | Kotlin、Retrofit、Stripe PaymentSheet |

## 專案結構

```text
AgriMart-APP/
├── android/       小農交易平台 Android App
├── payment/       獨立的 Stripe 付款流程練習
├── docs/          功能導覽與 Firestore 資料結構
├── README.md
└── .gitignore
```

- [Android 程式導覽](android/README.md)
- [功能與主要檔案](docs/features.md)
- [Firestore 資料結構](docs/firestore-collections.md)
- [Stripe 付款流程練習](payment/README.md)

## 開發環境與設定

主 App 使用 Java 8 語法、Android Gradle Plugin 8.1.0、Gradle 8.0，compile SDK 34，最低與目標 SDK 32。

1. 使用 Android Studio 開啟 `android` 資料夾。
2. 在自己的 Firebase 專案註冊 Android App，套件名稱為 `tw.cgu.b0929035.smallshop`；將下載的 `google-services.json` 放入 `android/app`，並啟用 Firestore 與 Storage。
3. 在個人的 Gradle 設定檔 `~/.gradle/gradle.properties` 加入 Google Maps API key 與 PayPal Sandbox client ID：

```properties
AGRIMART_MAPS_API_KEY=your_google_maps_api_key
AGRIMART_PAYPAL_CLIENT_ID=your_paypal_sandbox_client_id
```

服務設定由個人環境提供，金鑰不提交至 GitHub。Firebase 設定格式可參考 [google-services.example.json](android/app/google-services.example.json)。
