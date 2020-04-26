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

data class AddStudentRequest(
    val doctype: String,
    val first_name: String,
    val last_name: String,
    val pwd : String,
    val email: String,
    val classroom_id: String
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

            var data = getDocumentFromDB(col, doc, db)!!

            call.respond(data)
        }

        post("/api/firestore/add/student/") {
            val request = call.receive<AddStudentRequest>()
            val response = addNewStudent(request.first_name, request.last_name, request.pwd,
                request.email, request.classroom_id)
            call.respond(response!!)
        }


        get("/") {
            call.respondText(hello())
        }
        get("/test") {
            call.respondText("Testing")
        }

        post("/calculate") {
            val request = call.receive<CalculatorRequest>() // Method we are calling on the contents of the request
            val result = when (request.operation) {
                "add" -> request.first + request.second
                "subtract" -> request.first - request.second
                "multiply" -> request.first * request.second
                "divide" -> request.first / request.second
                else -> throw Exception("${request.operation} is not supported.")
            }
            call.respond(Result(request.first, request.second, result, request.operation))
        }

        // Parameters are expressed with {varName} in the url
        get("/{operation}/{first}/{second}") {
            try {
                val operation = call.parameters["operation"]!!
                val first = call.parameters["first"]!!.toInt()
                val second = call.parameters["second"]!!.toInt() // Throws an exception on non-number values
                val result = when (operation) {
                    "add" -> first + second
                    "subtract" -> first - second
                    "multiply" -> first * second
                    "divide" -> first / second
                    else -> throw Exception("$operation is not supported.")
                }

                val addResult = Result(first, second, result, operation)
                call.respond(addResult)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, 8080, module = Application::api).start(wait = true)
}
