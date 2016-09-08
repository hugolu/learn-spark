## distinct

```scala
val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog", "Gnu", "Rat"), 2)

c.count             //= 6
c.collect           //= Array(Gnu, Cat, Rat, Dog, Gnu, Rat)

c.distinct.count    //= 4
c.distinct.collect  //= Array(Dog, Cat, Gnu, Rat)
```

```scala
val a = sc.parallelize(List(1,2,3,4,5,6,7,8,9,10))

a.distinct(2).foreachPartition{ iter => println(iter.toList.mkString(",")) }
//> 4,6,8,10,2
//> 1,3,7,9,5

a.distinct(3).foreachPartition{ iter => println(iter.toList.mkString(",")) }
//> 6,3,9
//> 4,1,7,10
//> 8,5,2
```
