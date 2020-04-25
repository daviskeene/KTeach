import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication

class TestMain : StringSpec({
    "should retrieve root path properly" {
        withTestApplication(Application::calculator) {
            handleRequest(HttpMethod.Get, "/add/1/2").apply {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }

    "should accept post calculate request" {
        withTestApplication(Application::calculator) {
            handleRequest(HttpMethod.Post, "/calculate") {
                addHeader("content-type", "application/json")
                setBody("""
{
    "operation": "add",
    "first": 4,
    "second": 2
}
                """.trimIndent())
            }.apply {
                response.status() shouldBe HttpStatusCode.OK
            }
        }
    }

    "should serialize json properly" {
        withTestApplication(Application::calculator) {
            handleRequest(HttpMethod.Post, "/calcluate") {
                addHeader("content-type", "application/json")
                setBody("""
{
    "operation": "add",
    "first": 2,
    "second": 3
}
                    
                """.trimIndent())
            }
        }
    }
})
