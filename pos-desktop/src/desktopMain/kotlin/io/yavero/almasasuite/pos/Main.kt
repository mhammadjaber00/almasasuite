package io.yavero.almasasuite.pos

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.yavero.almasasuite.model.AuthenticatedUser
import io.yavero.almasasuite.pos.ui.auth.PinPadScreen
import io.yavero.almasasuite.pos.ui.main.AuthenticatedApp


fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1280.dp, 800.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "ALMASA JEWELRY",
        state = windowState
    ) {
        AlmasaPosApplication()
    }
}


@Composable
fun AlmasaPosApplication() {
    var authenticatedUser by remember { mutableStateOf<AuthenticatedUser?>(null) }

    if (authenticatedUser == null) {
        PinPadScreen(
            onAuthenticated = { user ->
                authenticatedUser = user
            }
        )
    } else {
        AuthenticatedApp(
            user = authenticatedUser!!,
            onLogout = {
                authenticatedUser = null
            }
        )
    }
}