package Grading

data class TestResult(var score: Double, var maxScore: Double)
data class Case(val name: String, val weight: Double = 1.0, val code: () -> Boolean) {
    fun test(): TestResult {
        val result = code()
        val resultDisplay = if (result) "PASSED" else "FAILED"
        println("Test $name -> $resultDisplay ($weight)")

        val score = if (result) weight else 0.0
        return TestResult(score, weight)
    }
}

fun sumScore(cases: List<Case>) : Pair<Double, Double> {
    var score = 0.0
    var totalScore = 0.0
    for (case in cases) {
        val result = case.test()
        score += result.score
        totalScore += result.maxScore
    }
    return Pair(score, totalScore)
}

fun computeScore(cases: List<Case>) : Double {
    val (score, total) = sumScore(cases)
    return 100 * (score / total)
}