# ml-100

## README

### u.data
10000 個評論，來自 943 位使用者對 1682 部電影的評論
- user id
- item id
- rating
- timestamp

### u.item
影片資訊
- movie id
- movie title 
- release date 
- video release date
- IMDb URL
- unknown | Action | Adventure | Animation | Children's | Comedy | Crime | Documentary | Drama | Fantasy | Film-Noir | Horror | Musical | Mystery | Romance | Sci-Fi | Thriller | War | Western |

> 最後19個欄位是影片的種類，1表示屬於此類，0表示不屬於此類

### u.genre
電影種類列表

### u.user
用戶統計資訊
- user id
- age
- gender
- occupation
- zip code

### u.occupation
用戶職業列表

## 隨便看看

### 用戶資訊
```scala
val user = sc.textFile("ml-100k/u.user").map(line => line.split("\\|"))
user.first
//> res2: Array[String] = Array(1, 24, M, technician, 85711)
```

### 職業資訊
```scala
val occupation = sc.textFile("ml-100k/u.occupation")
occupation.collect
//> res4: Array[String] = Array(administrator, artist, doctor, educator, engineer, entertainment, executive, healthcare, homemaker, lawyer, librarian, marketing, none, other, programmer, retired, salesman, scientist, student, technician, writer)

val occupationMap = occupation.zipWithIndex.collectAsMap
//> res6: scala.collection.Map[String,Long] = Map(scientist -> 17, writer -> 20, doctor -> 2, healthcare -> 7, administrator -> 0, educator -> 3, homemaker -> 8, none -> 12, artist -> 1, salesman -> 16, executive -> 6, programmer -> 14, engineer -> 4, librarian -> 10, technician -> 19, retired -> 15, entertainment -> 5, marketing -> 11, student -> 18, lawyer -> 9, other -> 13)
val bcOccupationMap = sc.broadcast(occupationMap)
```

## 轉換用戶資訊
```scala
val userRDD = user.map(u => (u(0).toInt, u(1).toInt, if (u(2) == "M") 0 else 1, bcOccupationMap.value(u(3)).toInt, u(4).toInt))
userRDD.first
//> res16: (Int, Int, Int, Int, Int) = (1,24,0,19,85711)
```
