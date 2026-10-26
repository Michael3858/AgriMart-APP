# AgriMart 最終主程式

採用本機 SmallShop 資料夾作為最終版本，包含商品分類、上架、會員資料、聊天、地圖及 PayPal Sandbox 整合。

## 主要檔案

| 檔案 | 內容 |
|---|---|
| [FrontPage.java](app/src/main/java/tw/cgu/b0929035/smallshop/FrontPage.java) | 主畫面與分頁切換。 |
| [utilities/Constants.java](app/src/main/java/tw/cgu/b0929035/smallshop/utilities/Constants.java) | Firestore 集合與欄位名稱。 |
| [ServiceConfig.java](app/src/main/java/tw/cgu/b0929035/smallshop/ServiceConfig.java) | 私人 Maps 與 PayPal Sandbox 設定是否已提供。 |

## 開發環境與設定

Java 8 語法、compile SDK 34、target／最低 SDK 32；Android Gradle Plugin 8.1.0、Gradle 8.0。

使用前需從自己的 Firebase 專案取得 `app/google-services.json`；[google-services.example.json](app/google-services.example.json) 只示範結構，沒有有效服務設定。Firestore 規則、索引與雲端資料未包含於此專案。

Google Maps 與 PayPal Sandbox 分別讀取個人 Gradle 設定中的 `AGRIMART_MAPS_API_KEY`、`AGRIMART_PAYPAL_CLIENT_ID`。未設定 Maps key 時，地圖頁面會顯示提示並關閉，不初始化 MapView。未設定 PayPal client ID 時，不初始化 Checkout，付款畫面會顯示提示並關閉。環境保留為 Sandbox，尚未驗證完整交易流程。
