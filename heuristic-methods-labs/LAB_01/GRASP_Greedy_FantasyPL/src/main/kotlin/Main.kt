import java.io.File

fun printTeam(algorithmName: String, instanceNumber: Number, firstTeam: List<Player>, subs: List<Player>) {
    println("----------------------------------------")
    println("Instance $instanceNumber $algorithmName")
    println("First team points: ${firstTeam.sumOf { it.points }}")
    println("Budget used up: ${firstTeam.sumOf { it.price } + subs.sumOf { it.price }}")
    println("First team and subs:")
    println(firstTeam.joinToString(",") { it.name })
    println(firstTeam.joinToString(",") { it.id })
    println(subs.joinToString(",") { it.id })
    println()
}

fun printTeamAndSubsToFile(algorithmName: String, instanceNumber: Number, firstTeam: List<Player>, subs: List<Player>) {
    val file = File("src/main/resources/${algorithmName}_0$instanceNumber.txt")
    file.writeText(firstTeam.joinToString(",") { it.id })
    file.appendText("\n")
    file.appendText(subs.joinToString(",") { it.id })
}

fun main(args: Array<String>) {

    val instancePath1 = "src/main/resources/2023_instance1.csv"
    val instancePath2 = "src/main/resources/2023_instance2.csv"
    val (players1, clubs1) = parsePlayersAndClubs(File(instancePath1))
    val (players2, clubs2) = parsePlayersAndClubs(File(instancePath2))

    val constraints1 = Constraints(
        numPerClub = clubs1.toMutableMap(),
    )

    val constraints2 = Constraints(
        numPerClub = clubs2.toMutableMap(),
    )

    val (greedyFirstTeam1, greedySubs1) = Greedy(0.0, constraints1, players1).compute(1)
    val (greedyFirstTeam2, greedySubs2) = Greedy(0.0, constraints2, players2).compute(1)

    val (graspTeam2, graspSubs2) = Grasp(0.3, constraints2, players2).compute(10)

    var graspTeam1: List<Player> = listOf()
    var graspSubs1: List<Player> = listOf()

    while (graspTeam1.sumOf { it.points } < 1829) {
        val (team, subs) = Grasp(0.3, constraints1, players1).compute(10)
        if (team.sumOf { it.points } > graspTeam1.sumOf { it.points }) {
            graspTeam1 = team
            graspSubs1 = subs
        }
    }

    printTeam("Greedy", 1, greedyFirstTeam1, greedySubs1)
    printTeam("Greedy", 2, greedyFirstTeam2, greedySubs2)
    printTeam("Grasp", 1, graspTeam1, graspSubs1)
    printTeam("Grasp", 2, graspTeam2, graspSubs2)

    printTeamAndSubsToFile("greedy", 1, greedyFirstTeam1, greedySubs1)
    printTeamAndSubsToFile("greedy", 2, greedyFirstTeam2, greedySubs2)
    printTeamAndSubsToFile("grasp", 1, graspTeam1, graspSubs1)
    printTeamAndSubsToFile("grasp", 2, graspTeam2, graspSubs2)

}