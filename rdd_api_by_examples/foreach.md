## foreach

定義
```
def foreach(f: T => Unit)
```

範例
```
scala> val c = sc.parallelize(List("cat", "dog", "tiger", "lion", "gnu", "crocodile", "ant", "whale", "dolphin", "spider"), 3)

scala> c.foreach(x => println(x + "s are yummy"))
cats are yummy
dogs are yummy
tigers are yummy
lions are yummy
gnus are yummy
crocodiles are yummy
ants are yummy
whales are yummy
dolphins are yummy
spiders are yummy
```