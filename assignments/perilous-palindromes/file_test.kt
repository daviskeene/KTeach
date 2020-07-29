// by the power of string concat

fun main() {
    val cases = listOf(
        Case("racecar", 1.0) {
            val racecar: String = "racecar"
            palindrome(racecar)
        },
        Case("Racecar (case-sensitive)", 1.50) {
            palindrome("Racecar")
        },
        Case("Burger King", 1.0) {
            !palindrome("burger king")
        },
        Case("Longer Palindrome", .50) {
            palindrome("Rats live on no evil star")
        },
        Case("Empty String", 1.0) {
            palindrome("")
        },
        Case("Null check", 1.50) {
            !palindrome(null)
        }
    )

    val (earned, total) = sumScore(cases)
    println("Earned: $earned")
    println("Total: $total")
}
