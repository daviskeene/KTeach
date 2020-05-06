package Grading

import main.palindrome as palindrome

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

fun main() {
    val (earned, total) = sumScore(cases)
    println(earned)
    println(total)
}
