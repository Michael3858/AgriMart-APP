# Firestore 資料結構

App 使用以下 Firestore 集合處理會員、商品、聊天與位置資料。

| 集合 | 用途 | 主要程式 |
|---|---|---|
| `users` | 會員資料與登入查詢。 | [LoginScreen](../android/app/src/main/java/tw/cgu/b0929035/smallshop/LoginScreen.java)、[SignUpPage](../android/app/src/main/java/tw/cgu/b0929035/smallshop/SignUpPage.java) |
| `products` | 商品名稱、照片網址、價格、數量、分類與上架者。 | [Sell](../android/app/src/main/java/tw/cgu/b0929035/smallshop/Sell.java) |
| `chat` | 買賣雙方訊息。 | [ChatActivity](../android/app/src/main/java/tw/cgu/b0929035/smallshop/Activity/ChatActivity.java) |
| `conversations` | 對話摘要與最後訊息。 | [ChatActivity](../android/app/src/main/java/tw/cgu/b0929035/smallshop/Activity/ChatActivity.java)、[F2Fragment](../android/app/src/main/java/tw/cgu/b0929035/smallshop/F2Fragment.java) |
| `ShoppingCart` | 使用者加入購物車的商品。 | [ProductPage](../android/app/src/main/java/tw/cgu/b0929035/smallshop/ProductPage.java)、[F1Fragment](../android/app/src/main/java/tw/cgu/b0929035/smallshop/F1Fragment.java) |
| `userLocations` | 使用者經緯度。 | [F1Fragment](../android/app/src/main/java/tw/cgu/b0929035/smallshop/F1Fragment.java) |
| `distance` | 使用者之間的距離資料。 | [F1Fragment](../android/app/src/main/java/tw/cgu/b0929035/smallshop/F1Fragment.java) |
| `Productdistance` | 商品頁面使用的距離資料。 | [ProductPage](../android/app/src/main/java/tw/cgu/b0929035/smallshop/ProductPage.java)、[ProductPage3](../android/app/src/main/java/tw/cgu/b0929035/smallshop/ProductPage3.java) |

集合名稱區分大小寫。商品照片存放在 Firebase Storage，圖片網址寫入對應的商品文件。

常用欄位名稱整理於 [Constants.java](../android/app/src/main/java/tw/cgu/b0929035/smallshop/utilities/Constants.java)，各畫面負責對應資料的讀取與更新。
