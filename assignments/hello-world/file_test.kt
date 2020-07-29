import java.io.ByteArrayOutputStream

fun setup(newOut: java.io.ByteArrayOutputStream) {
    System.setOut(PrintStream(newOut))
}

fun end(orginalOut: java.io.PrintStream?) {
    System.setOut(originalOut)
}

// TODO: Fix this later
fun main() {
    private val originalOut: PrintStream? = System.out

    // Output stream to hold the result of System.out.println() for my tests.
    private val outContent = ByteArrayOutputStream()

    val cases = listOf(
        // Set output stream first

        Case("Output says 'Hello, world!'", 1.0) {
            setup(outContent) // Set output stream to hold output
            hello() // print hello world
            end(originalOut) // Re-set output stream to original
            outContent.toString() == "Hello, world!\n"
        }
    )
    // Call other main method... somehow?
    val (earned, total) = sumScore(cases)
    println(earned)
    println(total)
}