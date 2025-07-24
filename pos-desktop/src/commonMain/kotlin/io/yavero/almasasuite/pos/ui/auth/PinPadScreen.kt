package io.yavero.almasasuite.pos.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.yavero.almasasuite.model.UserRole
import io.yavero.almasasuite.model.AuthenticatedUser


@Composable
fun PinPadScreen(
    onAuthenticated: (AuthenticatedUser) -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    val backgroundColor = Color(0xFF121212)
    val surfaceColor = Color(0xFF1E1E1E)
    val primaryColor = Color(0xFF2196F3)
    val onSurfaceColor = Color(0xFFE0E0E0)
    val errorColor = Color(0xFFCF6679)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Text(
                    text = "ALMASA JEWELRY POS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter your PIN to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceColor,
                    textAlign = TextAlign.Center
                )


                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < pin.length) primaryColor else Color.Gray
                                )
                        )
                    }
                }


                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = errorColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }


                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PinButton("1") { addDigit("1", pin) { pin = it } }
                        PinButton("2") { addDigit("2", pin) { pin = it } }
                        PinButton("3") { addDigit("3", pin) { pin = it } }
                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PinButton("4") { addDigit("4", pin) { pin = it } }
                        PinButton("5") { addDigit("5", pin) { pin = it } }
                        PinButton("6") { addDigit("6", pin) { pin = it } }
                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PinButton("7") { addDigit("7", pin) { pin = it } }
                        PinButton("8") { addDigit("8", pin) { pin = it } }
                        PinButton("9") { addDigit("9", pin) { pin = it } }
                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PinButton("C", isSecondary = true) {
                            pin = ""
                            errorMessage = null
                        }
                        PinButton("0") { addDigit("0", pin) { pin = it } }
                        IconButton(
                            onClick = {
                                if (pin.isNotEmpty()) {
                                    pin = pin.dropLast(1)
                                    errorMessage = null
                                }
                            },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = onSurfaceColor
                            )
                        }
                    }
                }


                Button(
                    onClick = {
                        if (pin.length == 4) {
                            isLoading = true
                            errorMessage = null

                            authenticatePin(pin) { result ->
                                isLoading = false
                                when (result) {
                                    is AuthResult.Success -> onAuthenticated(result.user)
                                    is AuthResult.Error -> {
                                        errorMessage = result.message
                                        pin = ""
                                    }
                                }
                            }
                        }
                    },
                    enabled = pin.length == 4 && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            "LOGIN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PinButton(
    text: String,
    isSecondary: Boolean = false,
    onClick: () -> Unit
) {
    val primaryColor = Color(0xFF2196F3)
    val surfaceColor = Color(0xFF2E2E2E)
    val onSurfaceColor = Color(0xFFE0E0E0)

    Button(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSecondary) surfaceColor else primaryColor.copy(alpha = 0.1f)
        ),
        shape = CircleShape
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSecondary) onSurfaceColor else primaryColor
        )
    }
}


private fun addDigit(digit: String, currentPin: String, onPinChange: (String) -> Unit) {
    if (currentPin.length < 4) {
        onPinChange(currentPin + digit)
    }
}


sealed class AuthResult {
    data class Success(val user: AuthenticatedUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}


private fun authenticatePin(pin: String, onResult: (AuthResult) -> Unit) {
    when (pin) {
        "0000" -> onResult(AuthResult.Success(
            AuthenticatedUser("admin-user-id", "System Administrator", UserRole.ADMIN)
        ))
        "1111" -> onResult(AuthResult.Success(
            AuthenticatedUser("manager-user-id", "Store Manager", UserRole.MANAGER)
        ))
        "2222" -> onResult(AuthResult.Success(
            AuthenticatedUser("staff-user-id", "Sales Staff", UserRole.STAFF)
        ))
        else -> onResult(AuthResult.Error("Invalid PIN. Please try again."))
    }
}