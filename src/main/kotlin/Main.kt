import Services.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode


fun hello(): String {
    return "Hello, world!"
}

data class AddItemRequest(
    val doctype: String,
    val first_name: String,
    val last_name: String,
    val pwd : String,
    val email: String,
    val classroom_id: String,
    val title : String,
    val description : String,
    val problem : String,
    val tests : String
)

data class Student(val first_name: String, val last_name: String, val classroom_id: String, val email: String)

data class Results(val cases: List<String>, val grade: List<Double>)

data class IndexData(val items: List<Int>)

data class LoginRequest(val email: String, val password: String)

fun Application.api() { // Extension function for Application called adder()
    install(ContentNegotiation) {
        gson { }
    }

    install(CORS) {
        anyHost()
        header(HttpHeaders.AccessControlAllowOrigin)
        header("Content-Type")
        header(HttpHeaders.AccessControlAllowCredentials)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowMethods)
    }

    routing {

        // Retrieves data from a firestore document
        get("/api/firestore/{collection}/{document}") {
            val db = Constants.db

            val col = call.parameters["collection"]!!
            val doc = call.parameters["document"]!!

            var data = getDocumentFromDB(col, doc, db)

            if (data != null) {
                call.respond(data)
            }
            else {
                call.respondText("Sorry, but there is no document under $col/$doc in firebase at this time.")
            }
        }

        // Add stuents and teachers
        post("/api/firestore/add/{doctype}/") {
            val doctype = call.parameters["doctype"]!!
            try {
                val request = call.receive<AddItemRequest>()
            val response = when(doctype) {
                "student" -> addNewStudent(
                    request.first_name, request.last_name, request.pwd,
                    request.email, request.classroom_id
                )
                "teacher" -> addNewTeacher(
                    request.first_name, request.last_name, request.pwd,
                    request.email
                )
                "classroom" -> addNewClassroom(request.email, request.pwd, false)
                "assignment" -> addNewAssignment(
                    request.classroom_id, request.title, request.description,
                    request.problem, request.tests
                )
                else -> throw Exception("$doctype cannot be added.")
            }
            response?.let { it1 -> call.respond(it1) }
            } catch (e: java.lang.Exception) {
                e.printStackTrace();
            }
        }


        post("/api/login/") {
            val request = call.receive<LoginRequest>()
            val result = login(request.email, request.password)
            if (result == null) {
                call.respond({
                    HttpStatusCode.Unauthorized
                    "Incorrect email or password"
                })
            }
            result?.let { it1 -> call.respond(it1) }
        }

        post("/api/upload/{student_id}") {
            val multipart = call.receiveMultipart()
            var studentID = call.parameters["student_id"]!!
            var testFile: File? = null

            // Make a directory for the files
            val path = "src/main/kotlin/Grading/$studentID"
            "mkdir $path".runCommand()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val ext = File(part.originalFileName).extension
                    val name = if (File(part.originalFileName).name.contains("test")) "file_test_$studentID.$ext" else "file_$studentID.$ext"
                    val file = File(
                        path,
                        name
                    )
                    part.streamProvider().use { its -> file.outputStream().buffered().use {its.copyTo(it)} }
                    if (name.contains("test")) {
                        testFile = file
                    }
                }
            }
            // Check to see if we uploaded a test file, or if we need to download one
            if (testFile == null) {
                downloadFile("https://raw.githubusercontent.com/daviskeene/KTeach/master/file_test.kt", path)
            }

            // Need to call compilation outside of server, use bash scripts to do so
            "./compile.sh $studentID".runCommand()
            val results = "./run.sh $studentID".runCommand()
            val (cases, numbers) = splitGradingOutput(results!!)
            // remove temp files
            "./clean.sh $studentID".runCommand()
            call.respond(Results(cases, numbers))
        }
    }
}

fun main() {
    embeddedServer(Netty, watchPaths = listOf("/"), port = 8080, module = Application::api).start(wait = true)
}

fun String.runCommand() : String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

fun splitGradingOutput(output: String) : Pair<List<String>, List<Double>> {
    // Grading numbers are only used in the last two test cases.
    val items = output.split("\n")
    var numbers = items
        .filter { it.toDoubleOrNull() != null }
        .map { it.toDouble() }
    var cases = items.filter { it.toDoubleOrNull() == null }
    return Pair(cases, numbers)
}

fun downloadFile(url: String, path: String) {
    "wget $url -P $path".runCommand()
}