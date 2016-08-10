# 決策樹二元分類

使用決策樹演算法訓練資料，以特徵 (features) 與欄位 (label) 建立決策樹。

- 濕度 < 60
  - 氣壓 < 1：晴
  - 氣壓 > 1：雨
- 濕度 > 60
  - 氣壓 < 1：雨
  - 氣壓 > 1：晴

使用歷史資料執行訓練建立決策樹，決策樹不可能無限成長，必須限制最大分支與深度，所以要設定以下參數：
- maxBins - 節點最大分支數目
- maxDepth - 決策樹最大深度
- Impurity - 分裂節點的方式
  - Gini - 對每種特徵欄位分隔點計算評估，選擇分裂最小的 Gini 指數方式
  - Entropy - 對每種特徵欄位分隔點計算評估，選擇分裂最小的 Entropy 方式

## Classification 專案
```shell
$ mkdir Classification
$ cd Classification/
$ mkdir -p src/main/scala
$ mkdir data
$ cp /vagrant/train.tsv /vagrant/test.tsv data/   # 將 train.tsv, test.tsv 複製到 data/
```

### 建立 RunDecisionTreeBinary.scala
```shell
$ vi src/main/scala/RunDecisionTreeBinary.scala
```

RunDecisionTreeBinary.scala:
```scala
```

### 執行 RunDecisionTreeBinary.scala

### 調校訓練參數
