interface Algorithm {
    fun compute(maxIter: Int): Pair<List<Player>, List<Player>>
}

class Greedy(
    private val rclAlpha: Double,
    private val constraints: Constraints,
    private val players: List<Player>
) : Algorithm {
    override fun compute(maxIter: Int): Pair<List<Player>, List<Player>> {
        val localConstraints = constraints.deepCopy()

        val firstTeam = selectFirstTeam(localConstraints, players)
        val subs = selectSubs(firstTeam, localConstraints, players)

        return Pair(firstTeam, subs)
    }

    private fun selectFirstTeam(localConstraints: Constraints, players: List<Player>): List<Player> {
        val firstTeam = mutableListOf<Player>()
        val playersDescendingFitness = players.sortedByDescending { it.pointsPerPrice }.toMutableList()

        while (firstTeam.size < constraints.numFirstTeamPlayers) {
            val rcl = getMaxRcl(playersDescendingFitness) { it.pointsPerPrice }
            val player = rcl.random()

            if (firstTeamPlayerConstraintsSatisfied(player, localConstraints)) {
                firstTeam.add(player)
                localConstraints.budget -= player.price
                localConstraints.numPerPosition[player.position] =
                    localConstraints.numPerPosition[player.position]!! - 1
                localConstraints.numPerClub[player.club] = localConstraints.numPerClub[player.club]!! - 1
                localConstraints.numFirstTeamPlayers -= 1
                if (player.position == PlayerPosition.GK) {
                    localConstraints.gkInFirstTeam -= 1
                }
                if (player.position == PlayerPosition.DEF) {
                    localConstraints.minDefInFirstTeam -= 1
                }
                if (player.position == PlayerPosition.FW) {
                    localConstraints.minAttInFirstTeam -= 1
                }
            }
            playersDescendingFitness.remove(player)
        }
        return firstTeam
    }

    private fun selectSubs(
        firstTeam: List<Player>,
        localConstraints: Constraints,
        players: List<Player>
    ): List<Player> {
        val subs = mutableListOf<Player>()
        val playersAscendingPrice = players.sortedBy { it.price }.toMutableList()

        while (subs.size < constraints.numSubs) {
            val player = playersAscendingPrice.first()

            if (subsPlayerConstraintsSatisfied(firstTeam, player, localConstraints)) {
                subs.add(player)
                localConstraints.numPerPosition[player.position] =
                    localConstraints.numPerPosition[player.position]!! - 1
                localConstraints.numPerClub[player.club] = localConstraints.numPerClub[player.club]!! - 1
                localConstraints.budget -= player.price
                localConstraints.numSubs -= 1
            }
            playersAscendingPrice.remove(player)
        }

        return subs
    }

    private fun firstTeamPlayerConstraintsSatisfied(player: Player, constraints: Constraints): Boolean {
        if (!generalPlayerConstraintsSatisfied(player, constraints)) {
            return false
        }
        if (player.position == PlayerPosition.GK && constraints.gkInFirstTeam == 0) {
            return false
        }
        if (player.position !== PlayerPosition.FW && constraints.numFirstTeamPlayers <= constraints.minAttInFirstTeam) {
            return false
        }
        if (player.position !== PlayerPosition.DEF && constraints.numFirstTeamPlayers <= constraints.minDefInFirstTeam) {
            return false
        }
        return true
    }

    private fun subsPlayerConstraintsSatisfied(
        firstTeam: List<Player>,
        player: Player,
        constraints: Constraints
    ): Boolean {
        if (!generalPlayerConstraintsSatisfied(player, constraints)) {
            return false
        }
        if (firstTeam.contains(player)) {
            return false
        }
        return true
    }

    private fun generalPlayerConstraintsSatisfied(player: Player, constraints: Constraints): Boolean {
        if (player.price > constraints.budget) {
            return false
        }
        if (constraints.numPerPosition[player.position] == 0) {
            return false
        }
        if (constraints.numPerClub[player.club] == 0) {
            return false
        }
        return true
    }

    private fun getMaxRcl(sortedPlayers: List<Player>, property: (Player) -> Double): List<Player> {
        val threshold =
            property(sortedPlayers.first()) - rclAlpha * (property(sortedPlayers.first()) - property(sortedPlayers.last()))

        return sortedPlayers.filter { property(it) >= threshold }
    }
}

class Grasp(
    private val rclAlpha: Double,
    private val constraints: Constraints,
    private val players: List<Player>
) : Algorithm {
    override fun compute(maxIter: Int): Pair<List<Player>, List<Player>> {
        var (currentFirstTeam, currentSubs) = Greedy(rclAlpha, constraints.deepCopy(), players).compute(1)
        for (i in 1..maxIter) {
            val (seededCurrentFirstTeam, seededCurrentSubs) = Greedy(rclAlpha, constraints.deepCopy(), players).compute(
                1
            )
            val (bestNeighboringFirstTeam, bestNeighboringSubs) = localSearch(
                seededCurrentFirstTeam,
                seededCurrentSubs,
                players
            )
            if (bestNeighboringFirstTeam.sumOf { it.points } > currentFirstTeam.sumOf { it.points }) {
                currentFirstTeam = bestNeighboringFirstTeam
                currentSubs = bestNeighboringSubs
            }
        }
        return Pair(currentFirstTeam, currentSubs)
    }

    private fun localSearch(
        firstTeam: List<Player>,
        subs: List<Player>,
        players: List<Player>
    ): Pair<List<Player>, List<Player>> {
        var iter = 0
        var currentBestFirstTeam = firstTeam
        var currentBestSubs = subs
        var currentBestScore = firstTeam.sumOf { it.points }
        do {
            var didSwap = false
            val neighbors = singleSwapNeighborhood(currentBestFirstTeam, currentBestSubs, players)
            for (neighbor in neighbors) {
                val (neighborFirstTeam, neighborSubs) = neighbor
                val neighborScore = neighborFirstTeam.sumOf { it.points }
                if (neighborScore > currentBestScore) {
                    currentBestFirstTeam = neighborFirstTeam
                    currentBestSubs = neighborSubs
                    currentBestScore = neighborScore
                    didSwap = true
                }
            }
            println("Iteration: ${++iter}")
        } while (didSwap)
        println("Local search done")
        return Pair(currentBestFirstTeam, currentBestSubs)
    }

    private fun singleSwapNeighborhood(
        firstTeam: List<Player>,
        subs: List<Player>,
        allPlayers: List<Player>
    ): List<Pair<List<Player>, List<Player>>> {
        val neighbors = mutableListOf<Pair<List<Player>, List<Player>>>()
        val playersSortedByPoints = allPlayers.sortedByDescending { it.points }

        for (player in firstTeam) {
            for (i in 0..10) {
                val playersSamePosition = playersSortedByPoints.filter { it.position == player.position }
                val newFirstTeam = firstTeam.toMutableList()

                if (playersSamePosition[i] in newFirstTeam) {
                    continue
                }

                newFirstTeam.remove(player)
                newFirstTeam.add(playersSamePosition[i])

                if (generalConstraintsSatisfied(newFirstTeam, subs, constraints.deepCopy())) {
                    neighbors.add(Pair(newFirstTeam, subs))
                }
            }
        }
        return neighbors
    }

}