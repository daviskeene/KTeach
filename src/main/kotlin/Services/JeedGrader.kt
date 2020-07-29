package hello.Services

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import java.io.File

data class JeedSource(val path: String, val contents: String)
data class JeedSubmit(val label: String,
                      val sources: List<JeedSource>, //make something for this
                      val tasks: List<String>)

// ew I need a better way to do this
@JsonIgnoreProperties(ignoreUnknown = true)
data class JeedResponse(val completed: JeedCompleted)
@JsonIgnoreProperties(ignoreUnknown = true)
data class JeedCompleted(val execution: JeedExecution)
@JsonIgnoreProperties(ignoreUnknown = true)
data class JeedExecution(val outputLines: List<JeedExecutionLine>)
@JsonIgnoreProperties(ignoreUnknown = true)
data class JeedExecutionLine(val line: String)

data class StudentSubmit(val studentCode: String)

suspend fun loadFileAsString(filePath: String) = File(filePath).readLines().joinToString("\n")

suspend fun loadFramework(): String {
    val pathName = "grading/Hacky.kt" //TODO: Make less hacky
    return loadFileAsString(pathName)
}

suspend fun loadCases(testName: String): String {
    val pathName = "assignments/$testName/file_test.kt"
    return loadFileAsString(pathName)
}

suspend fun jeedTest(studentSolution: String): List<JeedExecutionLine> {
    val mapper = jacksonObjectMapper() // TODO: Accept as param

    val framework = loadFramework()
    val cases = loadCases("perilous-palindromes")
    val sampleCodeBody = "\n $framework \n\n $studentSolution \n\n $cases \n" //TODO: Give jeed many files
//    println(sampleCodeBody)

    val client = HttpClient()

    // TODO: use jeed directly as a library
    val jeedCodeSubmit = (sampleCodeBody).replace('"', '\"')
    val jeedCallBody = JeedSubmit(
        label = "questionable-kotlin",
        sources = listOf(JeedSource("Main.kt", jeedCodeSubmit)),
        tasks = listOf("execute", "kompile"))
    val jeedResponse = client.post<String>("https://cs125-cloud.cs.illinois.edu/jeed/") {
        header("content-type", "application/json")
        body = mapper.writeValueAsString(jeedCallBody)
    }

    // compile errors? test passes and test fails hmm... there are many paths this can take
    val processedJeedResponse: JeedResponse = mapper.readValue(jeedResponse)
    val execLines = processedJeedResponse.completed.execution.outputLines
    execLines.forEach { println(it.line) }

    return execLines
}