# 功能與程式導覽

AgriMart 依會員、商品、交易與聊天功能組織畫面，透過 Firestore 儲存資料，並將商品照片上傳至 Firebase Storage。

| 功能 | 內容 | 主要程式 |
|---|---|---|
| 會員 | 註冊、登入、記住登入狀態、顯示個人資料與變更密碼。 | `LoginScreen`、`SignUpPage`、`F4Fragment` |
| 個人資料 | 修改姓名、電話、生日與地址。 | `ReviseInfo` |
| 商品分類 | 穀物、蔬菜、水果、禽類、乳製品與其他，共六類商品列表。 | `F1Fragment`、各類 `*List` |
| 商品搜尋 | 依名稱搜尋商品，並支援語音輸入搜尋文字。 | `F1Fragment`、`SearchList` |
| 語言 | 中英文介面切換。 | `ChooseLanguage` |
| 商品上架 | 填寫商品資料、選擇照片並上傳。 | `Sell` |
| 我的商品 | 列出自己上架的商品、查看詳情與下架。 | `F3Fragment`、`ProductPage2` |
| 定位與地址 | 儲存經緯度、計算距離、顯示附近賣家商品與選擇地址。 | `F1Fragment`、`MapsActivity` |
| 即時聊天 | 對話紀錄、聯絡人列表與買賣雙方文字訊息。 | `F2Fragment`、`ChatActivity` |
| 購物車 | 加入、列出與移除商品。 | `ProductPage`、`ShoppingCart`、`ProductPage3` |
| 測試付款 | 從商品頁帶入價格，操作 PayPal Sandbox 付款流程。 | `PaypalActivity`、`PaypalDetail` |

主程式位於 [`android/app/src/main/java/tw/cgu/b0929035/smallshop`](../android/app/src/main/java/tw/cgu/b0929035/smallshop/)，畫面配置位於 [`android/app/src/main/res/layout`](../android/app/src/main/res/layout/)。
