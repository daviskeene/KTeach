import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

import com.google.cloud.firestore.FirestoreOptions


fun hello(): String {
    return "Hello, world!"
}

data class Result(val first: Int, val second: Int, val result: Int, val operation: String)

data class CalculatorRequest(
    val operation: String,
    val first: Int,
    val second: Int
)

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
                "student" -> addNewStudent(request.first_name, request.last_name, request.pwd,
                    request.email, request.classroom_id)
                "teacher" -> addNewTeacher(request.first_name, request.last_name, request.pwd,
                    request.email)
                "classroom" -> addNewClassroom(request.email, request.pwd)
                "assignment" -> addNewAssignment(request.classroom_id, request.title, request.description,
                    request.problem, request.tests)
                else -> throw Exception("$doctype cannot be added.")
            }
            response?.let { it1 -> call.respond(it1) }
        }

        get("/") {
            call.respondText(hello())
        }
    }
}

fun main() {
    embeddedServer(Netty, 8080, module = Application::api).start(wait = true)
}
