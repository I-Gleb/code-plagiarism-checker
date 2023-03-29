

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.concurrent.TimeUnit

class Storage private constructor(private val dataSource: HikariDataSource) {
    companion object {
        private var instance : Storage? = null
        fun  getInstance(dataSource: HikariDataSource = HikariDataSource()): Storage {
            if (instance == null)  // NOT thread safe!
                instance = Storage(dataSource)

            return instance!!
        }
        fun filesWithToken(token: String): List<String> {
            return getInstance().filesWithToken(token)
        }
    }
    fun filesWithToken(token: String): List<String> {
        try {
            dataSource.connection.use { conn ->
                val statement = conn.prepareStatement("SELECT file FROM tokens WHERE token=?")

                statement.setString(1, token)

                val res = mutableListOf<String>()
                statement.executeQuery().use { rs ->
                    while (rs.next()) {
                        res.add(rs.getString(1))
                    }
                }

                return res
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }
}

var analizeResult = "";

class Main : CliktCommand() {
    val host: String by option("--host", help = "Host name").default("localhost")
    val port: Int by option("--port", help = "Port number").int().default(9090)

    val dbName: String by option("--db-name", help = "Database name").default("mydb")
    val dbHost: String by option("--db-host", help = "Database host").default("localhost")
    val dbPort: Int by option("--db-port", help = "Database port number").int().default(3306)
    val dbUser: String by option("--db-user", help = "Database user name").default("default")
    val dbPassword: String by option("--db-password", help = "Database user password").default("")

    override fun run() {
        Storage.getInstance(HikariDataSource(HikariConfig().apply {
            username = dbUser
            password = dbPassword
            jdbcUrl = "jdbc:mysql://${dbHost}:${dbPort}/${dbName}?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false"

        }))
        embeddedServer(Netty, port, host, module = Application::myApplicationModule).start(wait = true)
    }
}
fun main(args: Array<String>) = Main().main(args)

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
                try {
                    analizeResult = analizeCode(file)
                }
                catch (e: Exception) {
                    analizeResult = e.toString()
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
fun getTokens(code: String): List<String>? {
    try {
        val inputFile = File("tmp.tokens")
        inputFile.createNewFile()
        inputFile.writeText(code)
        val proc = ProcessBuilder("pygmentize", "-f", "raw", "-g", "tmp.tokens")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(5, TimeUnit.MINUTES)
        inputFile.delete()
        return proc.inputStream.bufferedReader().readText().split("\n")
    } catch(e: IOException) {
        e.printStackTrace()
        return null
    }
}
fun analizeCode(code: String): String {
    val tokens = getTokens(code) ?: throw Exception("Couldn't parse the file into tokens")

    val regex = """(Token\.Name[\.\w]*).*'(.*)'""".toRegex()
    val nameTokens = tokens.filter {
        regex.matches(it)
    }.map {
        val (tokenType, tokenString) = regex.matchEntire(it)!!.destructured
        "$tokenType.$tokenString"
    }.distinct()

    val countTokens = mutableMapOf<String, Int>()
    nameTokens.forEach { token ->
        Storage.filesWithToken(token).forEach { fileName ->
            countTokens[fileName] = (countTokens[fileName] ?: 0) + 1
        }
    }

    val similarFiles = countTokens.filter{
        it.value >= nameTokens.size * 0.85
    }.map { it.key }

    if (similarFiles.isEmpty()) {
        return "OK"
    }
    else {
        return similarFiles.get(0)
    }
}