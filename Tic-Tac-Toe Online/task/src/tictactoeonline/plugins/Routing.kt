package tictactoeonline.plugins

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.response.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }  // end_of_routing {}
}  // end_Application.confugure()