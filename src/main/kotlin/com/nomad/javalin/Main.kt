package com.nomad.javalin

import com.nomad.javalin.controller.UserController
import io.javalin.Javalin
import io.javalin.core.security.RouteRole
import io.javalin.core.util.Header
import io.javalin.http.Context
import io.javalin.plugin.rendering.vue.JavalinVue
import io.javalin.plugin.rendering.vue.VueComponent

enum class Role : RouteRole { ANYONE, LOGGED_IN }

fun main() {

    val app = Javalin.create { config ->
        config.enableWebjars()
        config.accessManager { handler, ctx, permittedRoles ->
            when {
                Role.ANYONE in permittedRoles -> handler.handle(ctx)
                Role.LOGGED_IN in permittedRoles && anyUsernameProvided(ctx) -> handler.handle(ctx)
                else -> ctx.status(401).header(Header.WWW_AUTHENTICATE, "Basic")
            }
        }
    }.start(7000)



    app.get("/", VueComponent("hello-world"), Role.ANYONE)
    app.get("/users", VueComponent("user-overview"), Role.ANYONE)
    app.get("/users/{user-id}", VueComponent("user-profile"), Role.LOGGED_IN)

    app.get("/api/users", UserController::getAll, Role.ANYONE)
    app.get("/api/users/{user-id}", UserController::getOne, Role.LOGGED_IN)

    app.error(404, "html", VueComponent("not-found"))

//    JavalinVue.stateFunction = { ctx -> mapOf("currentUser" to ctx.basicAuthCredentials()?.username) }
}

fun anyUsernameProvided(ctx: Context) = ctx.basicAuthCredentials()?.username?.isNotBlank() == true

