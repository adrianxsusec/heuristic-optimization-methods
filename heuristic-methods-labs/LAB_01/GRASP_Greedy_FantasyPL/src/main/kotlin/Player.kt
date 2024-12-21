enum class PlayerPosition {
    GK,
    DEF,
    MID,
    FW
}

class Player(
    val id: String,
    var position: PlayerPosition,
    val name: String,
    val club: String,
    val points: Int,
    val price: Double
) {
    var pointsPerPrice: Double = 0.0

    init {
        calculateFitness()
    }

    private fun calculateFitness() {
        pointsPerPrice = points / price
    }
}

