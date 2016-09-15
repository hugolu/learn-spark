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

### 轉換用戶資訊
```scala
val userRDD = user.map(u => (u(0).toInt, u(1).toInt, if (u(2) == "M") 0 else 1, bcOccupationMap.value(u(3)).toInt, u(4).toInt))
userRDD.first
//> res16: (Int, Int, Int, Int, Int) = (1,24,0,19,85711)
```

### 電影資訊
```scala
val data = sc.textFile("ml-100k/u.item").map(_.split("\\|"))
data.first
//> res17: Array[String] = Array(1, Toy Story (1995), 01-Jan-1995, "", http://us.imdb.com/M/title-exact?Toy%20Story%20(1995), 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
```

### 種類資訊
```scala
val genre = sc.textFile("ml-100k/u.genre").filter(_ != "").map(_.split("\\|")).map(arr => (arr(1).toInt, arr(0))).collectAsMap
//> genre: scala.collection.Map[Int,String] = Map(17 -> War, 8 -> Drama, 11 -> Horror, 2 -> Adventure, 5 -> Comedy, 14 -> Romance, 13 -> Mystery, 4 -> Children's, 16 -> Thriller, 7 -> Documentary, 1 -> Action, 10 -> Film-Noir, 18 -> Western, 9 -> Fantasy, 3 -> Animation, 12 -> Musical, 15 -> Sci-Fi, 6 -> Crime, 0 -> unknown)

val bcGenre = sc.broadcast(genre)
```

### 顯示電影種類
```scala
import collection.mutable.ArrayBuffer

def genreInfo(data: Array[String]): String = {
  val array = data.slice(5, data.length)
  var genres = ArrayBuffer[String]()
  for {
    i <- 0 until array.length
    if array(i) == "1"
  } genres += genre(i)
  genres.mkString(", ")
}

data.take(5).foreach(item => println(f"${item(1)}%20s: ${genreInfo(item)}"))
//>    Toy Story (1995): Animation, Children's, Comedy
//>    GoldenEye (1995): Action, Adventure, Thriller
//>   Four Rooms (1995): Thriller
//>   Get Shorty (1995): Action, Comedy, Drama
//>      Copycat (1995): Crime, Drama, Thriller
```
