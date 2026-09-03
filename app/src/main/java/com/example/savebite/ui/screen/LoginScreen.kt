package com.example.savebite.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.savebite.R
import com.example.savebite.ui.viewmodel.AuthViewModel
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val identifierError by viewModel.loginIdentifierError
    val passwordError by viewModel.loginPasswordError
    val loginError by viewModel.loginError
    val isLoading by viewModel.isLoading

    val context = LocalContext.current
    val successMessage by viewModel.successMessage

    // Clear errors when the screen is first displayed to ensure a fresh state
    LaunchedEffect(Unit) {
        viewModel.clearErrors()
    }

    // Show success message as a Toast when the login is successful
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccessMessage()
        }
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
        Text(
            text = stringResource(R.string.auth_welcome_back),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.auth_login_subtitle),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = identifier,
            onValueChange = {
                identifier = it
                if (identifierError != null) {
                    viewModel.clearLoginIdentifierError()
                }
            },
            label = { Text(stringResource(R.string.auth_email_phone_label)) },
            placeholder = { Text(stringResource(R.string.auth_email_phone_placeholder)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = identifierError != null,
            supportingText = {
                Text(
                    text = identifierError ?: stringResource(R.string.auth_phone_formats_hint),
                    color = if (identifierError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) {
                    viewModel.clearLoginPasswordError()
                }
            },
            label = { Text(stringResource(R.string.auth_password_label)) },
            placeholder = { Text(stringResource(R.string.auth_password_placeholder)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(text = passwordError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val iconRes = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(R.string.content_desc_toggle_password),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.auth_forgot_password),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLoading) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) { onNavigateToForgotPassword() },
            textAlign = TextAlign.End
        )

        // Display general login error if authentication fails
        if (loginError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = loginError!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.login(identifier, password, onLoginSuccess)
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.auth_login_button),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.auth_no_account))
            Text(
                text = stringResource(R.string.auth_signup_link),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToSignup() }
            )
        }
    }
}