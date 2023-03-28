

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class Main : CliktCommand() {
    val host: String by option("--host", help = "Host name").default("localhost")
    val port: Int by option("--port", help = "Port number").int().default(9090)

    val db_name: String by option("--db-name", help = "Database name").default("mydb")
    val db_host: String by option("--db-host", help = "Database host").default("localhost")
    val dp_port: Int by option("--db-port", help = "Database port number").int().default(3306)
    val dp_user: String by option("--db-user", help = "Database user name").default("default")
    val dp_password: String by option("--db-password", help = "Database user password").default("")

    val myServer by findOrSetObject {
        embeddedServer(Netty, port, host, module = Application::myApplicationModule)
    }

    override fun run() {
        myServer.start(wait = true)
    }
}
fun main(args: Array<String>) = Main().main(args)

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