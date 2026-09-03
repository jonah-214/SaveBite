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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
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
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val usernameError by viewModel.signupUsernameError
    val emailError by viewModel.signupEmailError
    val phoneError by viewModel.signupPhoneError
    val passwordError by viewModel.signupPasswordError
    val isLoading by viewModel.isLoading

    val context = LocalContext.current
    val successMessage by viewModel.successMessage

    // Clear errors when the screen is first displayed to ensure a fresh state
    LaunchedEffect(Unit) {
        viewModel.clearErrors()
    }

    // Show success message as a Toast when the account is created successfully
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
            text = stringResource(R.string.auth_create_account_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.auth_signup_subtitle),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                if (usernameError != null) {
                    viewModel.clearSignupUsernameError()
                }
            },
            label = { Text(stringResource(R.string.auth_username_label)) },
            placeholder = { Text(stringResource(R.string.auth_username_placeholder)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = usernameError != null,
            supportingText = {
                Text(
                    text = usernameError ?: stringResource(R.string.auth_username_hint),
                    color = if (usernameError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) {
                    viewModel.clearSignupEmailError()
                }
            },
            label = { Text(stringResource(R.string.auth_email_label)) },
            placeholder = { Text(stringResource(R.string.auth_email_placeholder)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                if (phoneError != null) {
                    viewModel.clearSignupPhoneError()
                }
            },
            label = { Text(stringResource(R.string.auth_phone_label)) },
            placeholder = { Text(stringResource(R.string.auth_phone_placeholder)) },
            prefix = { Text(stringResource(R.string.auth_phone_prefix)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = phoneError != null,
            supportingText = {
                Text(
                    text = phoneError ?: stringResource(R.string.auth_phone_hint),
                    color = if (phoneError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) {
                    viewModel.clearSignupPasswordError()
                }
            },
            label = { Text(stringResource(R.string.auth_password_label)) },
            placeholder = { Text(stringResource(R.string.auth_password_placeholder)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !isLoading,
            isError = passwordError != null,
            supportingText = {
                Text(
                    text = passwordError ?: stringResource(R.string.auth_password_hint),
                    color = if (passwordError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.signup(username, email, phone, password, onSignUpSuccess)
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
                    text = stringResource(R.string.auth_signup_link),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.auth_already_have_account))
            Text(
                text = stringResource(R.string.auth_login_link),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}