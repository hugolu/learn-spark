# 邏輯迴歸二元分類

## 羅輯回歸分析介紹

### 簡單迴歸分析 (Simple Regression Analysis)
假設變數 y 是自變數 x 所構成的某種現性函數，再加上一個誤差值所得到的值: y = b<sub>0</sub> + b<sub>1</sub>x

如果線性迴歸中「應變數」不是連續變項，而是二分變項，例如[是][否]得到疾病，就要使用 logistic regression。

將 y = b<sub>0</sub> + b<sub>1</sub>x 轉換成 Sigmoid 函數，用來界定某個資料的類別: 

![](https://wikimedia.org/api/rest_v1/media/math/render/svg/a26a3fa3cbb41a3abfe4c7ff88d47f0181489d13)
其中 t = b<sub>0</sub> + b<sub>1</sub>x

![](https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Logistic-curve.svg/600px-Logistic-curve.svg.png)

透過 sigmoid 算出來的值 p (probability) 如果大於 0.5，則歸類為「是」，反之則為「否」

### 複迴歸 (Multiple Regression Analysis)
複迴歸使用超過一個自變量，公式: y = b<sub>0</sub> + b<sub>1</sub>x<sub>1</sub> + b<sub>2</sub>x<sub>2</sub> + ... + b<sub>n</sub>x<sub>n</sub>

轉換成 Sigmoid 函數，t = b<sub>0</sub> + b<sub>1</sub>x<sub>1</sub> + b<sub>2</sub>x<sub>2</sub> + ... + b<sub>n</sub>x<sub>n</sub>

## RunLogisticRegressionWithSGDBinary 專案
