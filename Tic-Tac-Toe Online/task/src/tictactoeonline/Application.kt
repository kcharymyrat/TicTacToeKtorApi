package tictactoeonline

import io.ktor.application.*
import io.ktor.routing.*
import tictactoeonline.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}






