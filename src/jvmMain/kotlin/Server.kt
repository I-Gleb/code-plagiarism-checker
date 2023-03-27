import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*


fun main() {
    embeddedServer(Netty, 9090, module = Application::myApplicationModule).start(wait = true)
}

var analizeResult = "";

fun Application.myApplicationModule() {
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }
    install(Compression) {
        gzip()
    }
    routing {
        get("/") {
            call.respondText(
                this::class.java.classLoader.getResource("index.html")!!.readText(),
                ContentType.Text.Html
            )
        }
        static("/") {
            resources("")
        }
        route("/analizeFile") {
            get {
                call.respond(analizeResult)
            }
            post {
                val file = call.receive<String>()
                //TODO : analize code
                analizeResult = file
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}