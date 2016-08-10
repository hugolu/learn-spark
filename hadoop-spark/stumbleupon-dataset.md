# StumbleUpon 資料集介紹

## Kaggle 網站介紹
[Kaggle](https://www.kaggle.com/) 是一個數據分析網路競賽平台，也是群眾外包平台。企業與研究者將大數據的問題發布到網站上，像大眾徵求解決方案。

網路上任何人都可以參與大數據問題的競賽：下載問題、分析數據、運用機器學習、資料探勘等知識，建立演算法並解決問題，最後上傳結果。

## StumbleUpon 問題描述
Kaggle 上面有個 “StumbleUpon Evergreen Classification Challeng” 的問題。

問題描述：有些網頁是暫時性的 (如股市漲跌新聞)，有些是長青的 (如理財觀念)，要分辨網頁是暫時還是長青，對於推薦網頁給用戶有很大幫助。

人類看過網頁內容可以分類兩者，但成本高、也不即時。利用機器學習，透過大量網頁資料進行訓練建立模型，使用這個模型預測網頁內容屬於暫時或長青。

## StumbleUpon 資料內容
- 欄位 url, urlid, boilerplate 跟判斷網頁是否暫時或長青關係不大，忽略。
- 欄位 alchemy_category, alchemy_category_score, avglinksize, commonLinkRatio_1, commonLinkRatio_2, commonLinkRatio_3, commonLinkRatio_4, compression_ratio, embed_ratio, frameBased, frameTagRatio, hasDomainLink, html_ratio, image_ratio, is_news, lengthyLinkDomain, linkwordscore, news_front_page, non_markup_alphanum_characters, numberOfLinks integer, numwords_in_url, parametrizedLinkRatio, spelling_errors_ratio 有網頁相關資訊，如分類、連結數目、影像比例。
- 欄位 lebel 有兩個值，1 表示長青 (evengreen)、0 表示暫時 (non-evengreen)

## 下載檢視資料
## 二元分類演算法
