data class Constraints(
    var budget: Double = 100.0,
    var numPerPosition: MutableMap<PlayerPosition, Int> = mutableMapOf(
        PlayerPosition.GK to 2,
        PlayerPosition.DEF to 5,
        PlayerPosition.MID to 5,
        PlayerPosition.FW to 3
    ),
    var numPerClub: MutableMap<String, Int>,
    var numFirstTeamPlayers: Int = 11,
    var numSubs: Int = 4,
    var gkInFirstTeam: Int = 1,
    var minDefInFirstTeam: Int = 3,
    var minAttInFirstTeam: Int = 1
) {
    fun deepCopy(): Constraints {
        return Constraints(
            budget,
            numPerPosition.toMutableMap(),
            numPerClub.toMutableMap(),
            numFirstTeamPlayers,
            numSubs,
            gkInFirstTeam,
            minDefInFirstTeam,
            minAttInFirstTeam
        )
    }
}

fun generalConstraintsSatisfied(firstTeam: List<Player>, subs: List<Player>, constraints: Constraints): Boolean {
    val cost = firstTeam.sumOf { it.price } + subs.sumOf { it.price }
    if (cost > constraints.budget) {
        return false
    }
    val numPerPosition = mutableMapOf(
        PlayerPosition.GK to 0,
        PlayerPosition.DEF to 0,
        PlayerPosition.MID to 0,
        PlayerPosition.FW to 0
    )
    val numPerClub = mutableMapOf<String, Int>()
    for (player in firstTeam) {
        numPerPosition[player.position] = numPerPosition[player.position]!! + 1
        numPerClub.putIfAbsent(player.club, 0)
        numPerClub[player.club] = numPerClub[player.club]!! + 1
    }
    for (player in subs) {
        numPerPosition[player.position] = numPerPosition[player.position]!! + 1
        numPerClub.putIfAbsent(player.club, 0)
        numPerClub[player.club] = numPerClub[player.club]!! + 1
    }
    for (position in numPerPosition.keys) {
        if (numPerPosition[position]!! > constraints.numPerPosition[position]!!) {
            return false
        }
    }
    for (club in numPerClub.keys) {
        if (numPerClub[club]!! > constraints.numPerClub[club]!!) {
            return false
        }
    }
    if (firstTeam.filter { it.position == PlayerPosition.GK }.size != constraints.gkInFirstTeam) {
        return false
    }
    if (firstTeam.filter { it.position == PlayerPosition.DEF }.size < constraints.minDefInFirstTeam) {
        return false
    }
    if (firstTeam.filter { it.position == PlayerPosition.FW }.size < constraints.minAttInFirstTeam) {
        return false
    }

    return true
}