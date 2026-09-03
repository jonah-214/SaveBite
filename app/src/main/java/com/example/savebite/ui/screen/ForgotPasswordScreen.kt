package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.savebite.R
import com.example.savebite.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    val emailError by viewModel.forgotPasswordEmailError
    val resetEmailSent by viewModel.resetEmailSent
    val isLoading by viewModel.isLoading

    // Clear errors when the screen is first displayed to ensure a clean state
    LaunchedEffect(Unit) {
        viewModel.clearErrors()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!resetEmailSent) {
            // Request Email to send reset link
            Text(
                text = stringResource(R.string.auth_forgot_password_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_forgot_password_subtitle),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) {
                        viewModel.clearForgotPasswordEmailError()
                    }
                },
                label = { Text(stringResource(R.string.auth_email_label)) },
                placeholder = { Text(stringResource(R.string.auth_email_placeholder)) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick =  { viewModel.sendPasswordReset(email) },
                enabled = !isLoading,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.auth_send_reset_link), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.auth_cancel), color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // Confirmation message after email is sent
            Text(
                text = stringResource(R.string.auth_check_email_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_reset_sent_message, email),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onBackToLogin,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = stringResource(R.string.auth_back_to_login), fontWeight = FontWeight.Bold)
            }
        }
    }
}