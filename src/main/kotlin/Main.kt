package hello

import Services.*
import com.fasterxml.jackson.databind.SerializationFeature
import hello.Services.jeedTest
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
//import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.jackson.jackson
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

fun hello(): String {
    return "Hello, world!"
}

data class AddItemRequest(
    val doctype: String,
    val first_name: String,
    val last_name: String,
    val pwd: String,
    val email: String,
    val classroom_id: String,
    val title: String,
    val description: String,
    val problem: String,
    val test: String,
    val deadline: String
)

data class Student(val first_name: String, val last_name: String, val classroom_id: String, val email: String)

data class Results(val cases: List<String>, val grade: List<Double>)

data class IndexData(val items: List<Int>)

data class LoginRequest(val email: String, val password: String)

data class UpdateGradebookRequest(val classroom_id: String, val student_id : String, val assignment_id : String,
                                  val pointsScored : Double, val pointsTotal : Double)

data class GradeJob(
    val submit: String,
    val testName: String
)

data class Request (
    val id : String,
    val quantity: Int,
    val isTrue: Boolean
)

fun Application.api() { // Extension function for Application called adder()
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(CORS) {
        anyHost()
        allowCredentials
        header(HttpHeaders.AccessControlAllowOrigin)
        header("Content-Type")
        header(HttpHeaders.AccessControlAllowCredentials)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.AccessControlAllowMethods)
    }

    routing {

        // Test
        get("/") {
            call.respondText(hello())
        }

        // To test if post end point is working
        post("/"){
            val request = call.receive<Request>()
            call.respond(request)
        }

        // Retrieves data from a firestore document
        get("/api/firestore/{collection}/{document}") {
            try {
                val db = Constants.db

                val col = call.parameters["collection"]!!
                val doc = call.parameters["document"]!!

                var data : MutableMap<String, Any>? = if (col == "Classrooms") getClassroomVerbose(doc) else getDocumentFromDB(col, doc, db)

                if (data != null) {
                    call.respond(data)
                } else {
                    call.respondText("Sorry, but there is no document under $col/$doc in firebase at this time.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/api/assignments/{id}") {
            val student_id = call.parameters["id"]!!
            val assignments = availableAssignments(student_id)
            call.respond(assignments)
        }

        get("/api/firestore/test") {
            getDB()
        }

        // Add documents to firestore
        post("/api/firestore/add/{doctype}/") {
            val doctype = call.parameters["doctype"]!!
            try {
                val request = call.receive<AddItemRequest>()
            val response = when (doctype) {
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
                    request.problem, request.test, request.deadline
                )
                else -> throw Exception("$doctype cannot be added.")
            }
            if (response == null) {
                call.respond({
                    HttpStatusCode.BadRequest
                    "Invalid Input"
                })
            }
                call.respond(response!!)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        // Login endpoint
        post("/api/login/") {
            val request = call.receive<LoginRequest>()
            val result = login(request.email, request.password)
            if (result == null) {
                call.respond({
                    HttpStatusCode.Unauthorized
                    "Incorrect email or password"
                })
            }
            call.respond(result!!)
        }

        // Update firestore documents
        post("/api/firestore/update/{col}/{doc}") {
            val doctype = call.parameters["col"]!!
            val id = call.parameters["doc"]!!
            try {
                val request = call.receive<AddItemRequest>()
                val response = when (doctype) {
                    "Students" -> updateStudent(
                        id, request.first_name, request.last_name, request.pwd,
                        request.email
                    )
                    "Teachers" -> updateTeacher(
                        id, request.first_name, request.last_name, request.pwd,
                        request.email
                    )
                    "Assignments" -> updateAssignment(
                        id, request.title, request.description,
                        request.problem, request.test, request.deadline
                    )
                    else -> throw Exception("$doctype cannot be updated.")
                }
                if (response == null) {
                    call.respond({
                        HttpStatusCode.BadRequest
                        "Invalid Input"
                    })
                }
                call.respond(response!!);
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }


        }

        // Delete documents
        post("/api/firestore/delete/{col}/{doc}") {
            val doctype = call.parameters["col"]!!
            val id = call.parameters["doc"]!!
            try {
                Constants.db.collection(doctype).document(id).delete()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest,
                    "Could not delete document")
            }
            call.respond(HttpStatusCode.OK,
            "Document successfully deleted")
        }

        post("/api/firestore/update/gradebook") {
            try {
                val request = call.receive<UpdateGradebookRequest>()
                updateGradebook(request.classroom_id, request.student_id, request.assignment_id,
                    request.pointsScored, request.pointsTotal)
                call.respond({
                    HttpStatusCode.OK
                    "Update successful!"
                })
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                call.respond({
                    HttpStatusCode.BadRequest
                    "Something went wrong."
                })
            }
        }

        // Upload user solutions
        post("/api/upload/{student_id}") {
            val multipart = call.receiveMultipart()
            var studentID = call.parameters["student_id"]!!
            var testFile: File? = null
            var testDirectory: String? = null

            // Make a directory for the files
            val path = "src/main/kotlin/Grading/$studentID"
            "mkdir $path".runCommand()
            try {
                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val ext = File(part.originalFileName).extension
                        // Verify upload is a kotlin file
                        if (ext != "kt") {
                            throw IOException("File is not a kotlin file!")
                        }
                        val name = "file.kt"
                        val file = File(
                            path,
                            name
                        )
                        part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyTo(it) } }
                    }
                    if (part is PartData.FormItem) {
                        if (part.name == "test") {
                            testDirectory = part.value
                        }
                    }
                }
                var classPath : String? = null

                // Check to see if we uploaded a test file, or if we need to download one
                if (testDirectory == null) {
                    classPath = "Grading.FileKt"
                } else {
                    downloadFile(testDirectory!!, path)
                    classPath = "Grading.File_testKt"
                }
                // Need to call compilation outside of server, use bash scripts to do so

                val compiled = "./compile.sh $studentID".runCommand()
                println(compiled)
                val results = "./run.sh $studentID $classPath".runCommand()
                val (cases, numbers) = splitGradingOutput(results!!, classPath == "Grading.File_testKt")
                println(results)
                "./clean.sh $studentID".runCommand()
                if (numbers.isEmpty() && classPath == "Grading.File_testKt") {
                    call.respond(
                        Results(listOf("Invalid submission format! Please check the following:",
                            "Dont throw any exceptions (unless its a part of the assignment).",
                            "Don't change the name of any pre-declared functions, and be sure to spell things correctly!",
                            "Often, putting your code into an IDE will reveal any errors you can't catch with the naked eye.",
                            "Double check your formatting, parenthesis / brackets, or anything else that would cause your code to fail."), listOf(0.0, 1.0))
                    )
                } else {
                    call.respond(Results(cases, numbers))
                }
                // remove temp files
            } catch (e: java.lang.Exception) {
                println("something went wrong :/")
                e.printStackTrace()
                call.respond(
                    Results(listOf("Something went wrong! Either invalid file type no test file uploaded by teacher."), listOf(0.0, 1.0))
                )
            }
        }

        // Jeed test user solutions
        post("/api/jtest/{student_id}}") {
            val studentID: String = call.parameters["student_id"] ?: "INVALID"
            val toGrade = call.receive<GradeJob>();
            println(toGrade.submit)
            println("still working")
            val resultLines = jeedTest(toGrade.submit, toGrade.testName)
            println("jeed test ran for $studentID")
            call.respond(resultLines)
        }
    }
}

fun main() {
    embeddedServer(Netty, watchPaths = listOf("/"), port = 8080, module = Application::api).start(wait = true)
}

fun String.runCommand(): String? {
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

fun splitGradingOutput(output: String, isGrading: Boolean): Pair<List<String>, List<Double>> {
    // Grading numbers are only used in the last two test cases.
    if (isGrading) {
        val items = output.split("\n")
        var numbers = items
            .filter { it.toDoubleOrNull() != null }
            .map { it.toDouble() }
        var cases = items.filter { it.toDoubleOrNull() == null }
        return Pair(cases, numbers)
    } else {
        return Pair(output.split("\n"), listOf(0.0, 1.0))
    }
}

fun downloadFile(url: String, path: String) : String? {
    return "wget $url -P $path".runCommand()
}
