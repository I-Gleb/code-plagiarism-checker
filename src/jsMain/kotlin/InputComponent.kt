import org.w3c.dom.HTMLFormElement
import react.*
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import react.dom.events.ChangeEventHandler
import react.dom.events.FormEventHandler
import react.dom.html.InputType
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input

external interface InputProps : Props {
    var onSubmit: (File) -> Unit
}

val InputComponent = FC<InputProps> { props ->
    val (file, setFile) = useState(File(arrayOf(), ""))

    val submitHandler: FormEventHandler<HTMLFormElement> = {
        it.preventDefault()
        props.onSubmit(file)
    }

    val changeHandler: ChangeEventHandler<HTMLInputElement> = {
        setFile(it.target.files!!.item(0)!!)
    }

    form {
        onSubmit = submitHandler
        input {
            type = InputType.file
            onChange = changeHandler
        }
        input {
            type = InputType.submit
            value = "Submit"
        }
    }
}