
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.files.FileReader
import react.FC
import react.Props
import react.dom.html.ReactHTML.p
import react.useEffectOnce
import react.useState

private val scope = MainScope()

private val myClient = HttpClient {}

suspend fun getResult(): String {
    return myClient.get("/analizeFile").body()
}

val App = FC<Props> {
    var analizeResult by useState("")

    useEffectOnce {
        scope.launch {
            analizeResult = getResult()
        }
    }

    InputComponent {
        onSubmit = { file ->
            val reader = FileReader()
            reader.readAsText(file)
            reader.onload = {
                scope.launch {
                    analizeResult = "In process"
                    myClient.post("/analizeFile") {
                        contentType(ContentType.Application.Json)
                        setBody(reader.result.toString())
                    }
                    analizeResult = "Result for ${file.name}: ${getResult()}"
                }
            }
        }
    }
    p {
        +analizeResult
    }
}