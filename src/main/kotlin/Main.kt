import Grading.computeScore
import Services.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
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
import jdk.jfr.Percentage
import java.io.File
import java.io.IOError
import java.io.IOException
import java.util.concurrent.TimeUnit


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

data class Results(val cases: List<String>, val percentage: Double)

fun Application.api() { // Extension function for Application called adder()
    install(ContentNegotiation) {
        gson { }
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
        }

        post("/api/upload/") {
            val multipart = call.receiveMultipart()
            var studentID = ""
            var kotlinFile: File? = null

            multipart.forEachPart { part ->
                if (part is PartData.FormItem) {
                    if (part.name == "id") {
                        studentID = part.value
                    }
                } else if (part is PartData.FileItem) {
                    val ext = File(part.originalFileName).extension
                    val name = if (File(part.originalFileName).name.contains("test")) "file_test.$ext" else "file.$ext"
                    val file = File(
                        "src/main/kotlin/Grading/",
                        name
                    )
                    part.streamProvider().use { its -> file.outputStream().buffered().use {its.copyTo(it)} }
                    kotlinFile = file
                }
            }
            // Need to call compilation outside of server, use bash scripts to do so
            "./compile.sh".runCommand()
            val results = "./run.sh".runCommand()
            val tests = results?.split("\n")!!
            // remove temp files
            "./clean.sh".runCommand()
            call.respond(Results(tests.subList(0, tests.size - 1), tests.last().toDouble()))
        }

        get("/") {
            call.respondText(hello())
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
