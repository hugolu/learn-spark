## variance [Double], sampleVariance [Double]

定義
```
def variance(): Double
```

範例
```
scala> val a = sc.parallelize(List(9.1, 1.0, 1.2, 2.1, 1.3, 5.0, 2.0, 2.1, 7.4, 7.5, 7.6, 8.8, 10.0, 8.9, 5.5), 3)

scala> a.variance
res42: Double = 10.605333333333332
```