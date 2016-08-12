# 決策數多元分類

## 應用情境
森林管理員希望能運用大數據分析，更節省人力、經費來管理森林，提高森林覆蓋率。

### 定義問題
一座森林中有各種樹種，如熱帶、針葉林...，每塊土地都有適合生長的樹種。希望能預測哪些土地適合哪些樹種，就能在適合的土地種植適合生長的植物。

### 設計模型
海拔、方位、斜率、水源的垂直距離、荒野分類、水源的水平距離、土壤分類等，都會影響適合生長的樹種，整理資料如下表格：

欄位 | 說明
-----|-----
特徵 | 海拔、方位、斜率、水源垂直距離、荒野分類、水源水平距離、土壤分類
標籤 |  <ol><li>Spruce/Fir</li><li>Lodgepole Pine</li><li>Ponderosa Pine</li><li>Cottonwood/Willon</li><li>Aspen</li><li>Douglas-fir</li><li>Krummholz</li></ul>

### 搜集資料
透過森林管理單位長期田野調查，借助現代科技如空拍機、衛星圖，搜集這些資料。

### 分析資料
使用 「決策樹多元分析」(Decision Tree Multi-Class Classification) 建立模型、訓練、評估、預測資料。

### 其他應用情境
研究什麼地點適合開設什麼樣類型的店面:
- 特徵：與捷運站距離、與大學距離、與中學距離、該地點平均年收入、馬路寬度、附近人口數、門口每小時人流量
- 標籤：配合實際調查該地點店面類型，已經三年以上且賺錢的店面類型 (表示適合的店面類型)

## UCI Covertype 資料集介紹

### Machine Learning Repository
Machine Learning Repository 是加州大學爾灣分校 (University of California Irvine) 提供用於研究機器學習的數據庫。
- http://archive.ics.uci.edu/ml/

### UCI Covertype 資料集
Covertype Dataset 森林覆蓋樹種資料集，包含一座森林中有各種樹種的相關資訊。
- http://archive.ics.uci.edu/ml/datasets/Covertype

```shell
$ wget http://archive.ics.uci.edu/ml/machine-learning-databases/covtype/covtype.data.gz
$ gzip -d covtype.data.gz
$ mv covtype.data data/
```

欄位  | 分類 | 說明 
------|------|------
1~10  | 數值特徵 | 海拔、方位、斜率、水源垂直距離、水源水平距離、九點時陰影
11~14 | 分類特徵 | 荒野分類(1-of-k encoding): <ol><li>Rawah Wilderness Area</li><li>Neota Wilderness Area</li><li>Comanche Peak Wilderness Area</li><li>Cache la Proudre Wilderness Area</li></ol>
15~54 | 分類特徵 | 土壤分類(1-of-k encoding):
55    | 標籤欄位 | 森林覆蓋分類: <ol><li>Spruce/Fir</li><li>Lodgepole Pine</li><li>Ponderosa Pine</li><li>Cottonwood/Willon</li><li>Aspen</li><li>Douglas-fir</li><li>Krummholz</li></ul>

> 1-of-k encoding: 如果是 Neota Wilderness Area 則為 0,1,0,0，如果是 Comanche Peak Wilderness Area 則為 0,0,1,0

## 建立 RunDecisionTreeMulti.scala

### 執行 RunDecisionTreeMuti
