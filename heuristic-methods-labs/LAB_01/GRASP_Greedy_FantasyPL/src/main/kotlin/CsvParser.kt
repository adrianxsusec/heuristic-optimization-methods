import java.io.File
import java.nio.charset.Charset

fun parsePlayersAndClubs(file: File): Pair<List<Player>, Map<String, Int>> {
    val clubMap = mutableMapOf<String, Int>()
    val players = file.readLines(Charset.forName("ISO-8859-1")).map { line ->
        val parts = line.split(",")
        val id = parts[0]
        val position = PlayerPosition.valueOf(parts[1])
        val name = parts[2]
        val club = parts[3]
        val points = parts[4].toInt()
        val price = parts[5].toDouble()
        clubMap.putIfAbsent(club, 3)
        Player(id, position, name, club, points, price)
    }
    return Pair(players, clubMap)
}