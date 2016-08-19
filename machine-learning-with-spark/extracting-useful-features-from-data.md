# 從資料中提取有用的特徵

特徵 (features) 指那些用於模型訓練的變量。每一行資料包含可供提取到訓練樣本中的各種訊息。幾乎所有機器學習模型都是與「用向量表示的數值特徵」打交道，因為，需要將原始資料轉換為數值。

> 用向量表示的數值特徵: numerical representations in the form of a vector

特徵分成以下幾種：
- 數值特徵 (numerical features) - 實數或整數，如年齡
- 類別特徵 (categorical features) -  可能狀態集合，如性別、職業
- 文本特徵 (text features) - 延伸自資料的文本內容，如電影名稱、描述、評論
- 其他特徵 - 如影像、視頻、音頻、地理位置(經緯度)
