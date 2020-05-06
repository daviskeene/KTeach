package Grading

import java.io.ByteArrayOutputStream

import java.io.PrintStream
import main.main as main1

// Set output stream first
private val originalOut: PrintStream? = System.out

// Output stream to hold the result of System.out.println() for my tests.
private val outContent = ByteArrayOutputStream()

fun setup() {
    System.setOut(PrintStream(outContent))
}

fun end() {
    System.setOut(originalOut)
}

val cases = listOf(
    Case("Output says 'Hello, world!'", 1.0) {
        setup() // Set output stream to hold output
        main1() // print hello world
        end() // Re-set output stream to original
        outContent.toString() == "Hello, world!\n"
    }
)

fun main() {
    // Call other main method... somehow?
    val (earned, total) = sumScore(cases)
    println(earned)
    println(total)
}