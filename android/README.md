# AgriMart Android App

小農交易平台主程式，包含會員、商品、購物車、聊天、定位與 PayPal Sandbox 付款介面。

## 程式導覽

| 檔案 | 內容 |
|---|---|
| [FrontPage.java](app/src/main/java/tw/cgu/b0929035/smallshop/FrontPage.java) | 主畫面與分頁切換。 |
| [LoginScreen.java](app/src/main/java/tw/cgu/b0929035/smallshop/LoginScreen.java)、[SignUpPage.java](app/src/main/java/tw/cgu/b0929035/smallshop/SignUpPage.java) | 會員登入與註冊。 |
| [F1Fragment.java](app/src/main/java/tw/cgu/b0929035/smallshop/F1Fragment.java) | 商品分類、搜尋與附近商品。 |
| [Sell.java](app/src/main/java/tw/cgu/b0929035/smallshop/Sell.java) | 商品資料填寫與照片上傳。 |
| [ShoppingCart.java](app/src/main/java/tw/cgu/b0929035/smallshop/ShoppingCart.java) | 購物車列表與商品頁面導向。 |
| [ChatActivity.java](app/src/main/java/tw/cgu/b0929035/smallshop/Activity/ChatActivity.java) | 即時文字聊天。 |
| [MapsActivity.java](app/src/main/java/tw/cgu/b0929035/smallshop/MapsActivity.java) | 地圖與地址選擇。 |
| [PaypalActivity.java](app/src/main/java/tw/cgu/b0929035/smallshop/Activity/PaypalActivity.java) | PayPal Sandbox 訂單建立與付款操作。 |
| [Constants.java](app/src/main/java/tw/cgu/b0929035/smallshop/utilities/Constants.java) | Firestore 集合與欄位名稱。 |
| [ServiceConfig.java](app/src/main/java/tw/cgu/b0929035/smallshop/ServiceConfig.java) | Maps 與 PayPal 的環境設定檢查。 |

## 開發環境

- Java 8 語法
- Android Gradle Plugin 8.1.0、Gradle 8.0
- compile SDK 34；最低與目標 SDK 32

## 專案設定

Firebase、Google Maps 與 PayPal Sandbox 的設定步驟見 [專題首頁](../README.md#開發環境與設定)。Firebase Android 套件名稱為 `tw.cgu.b0929035.smallshop`，設定檔放在 `app/google-services.json`。

地圖與付款頁面會先檢查對應的服務設定；PayPal 使用 Sandbox 環境。

若依賴來源使用 CardinalCommerce 私有 Maven 儲存庫，帳號與密碼由個人的 Gradle 設定提供，鍵名為 `AGRIMART_MAVEN_USERNAME`、`AGRIMART_MAVEN_PASSWORD`。
