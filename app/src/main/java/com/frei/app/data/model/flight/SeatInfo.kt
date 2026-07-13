package com.frei.app.data.model.flight

enum class SeatClass { STANDARD, PREMIUM }
enum class SeatStatus { AVAILABLE, OCCUPIED, SELECTED }

data class SeatInfo(
    val seatNumber: String,   // "14C"
    val row: Int,
    val column: String,       // A, B, C, D, E, F
    val seatClass: SeatClass,
    val status: SeatStatus
) {
    val extraPrice: Double get() = if (seatClass == SeatClass.PREMIUM) 450.0 else 0.0
}

/**
 * Deterministic mock seat map generator, keyed off flightId so the same
 * flight always renders the same layout/occupancy within a session.
 * Swap this out later if/when a real seat-map API is wired in.
 */
object SeatMapGenerator {
    private val columns = listOf("A", "B", "C", "D", "E", "F")

    fun generate(flightId: String, rows: Int = 16): List<List<SeatInfo>> {
        val random = kotlin.random.Random(flightId.hashCode())
        return (1..rows).map { row ->
            val isPremium = row <= 2
            columns.map { col ->
                val occupied = !isPremium && random.nextInt(100) < 22
                SeatInfo(
                    seatNumber = "$row$col",
                    row = row,
                    column = col,
                    seatClass = if (isPremium) SeatClass.PREMIUM else SeatClass.STANDARD,
                    status = if (occupied) SeatStatus.OCCUPIED else SeatStatus.AVAILABLE
                )
            }
        }
    }
}