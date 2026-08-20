package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ProfileViewModel

@Composable
fun ChangePasswordScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Check if all fields are filled
    val isFormValid = currentPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmNewPassword.isNotBlank()


    // Handle password change success and pop back stack
    LaunchedEffect(profileViewModel.changePasswordSuccess.value) {
        if (profileViewModel.changePasswordSuccess.value) {
            profileViewModel.resetPasswordChangeSuccess()
            navController.popBackStack()
        }
    }

    // Reset errors when screen is entered
    LaunchedEffect(Unit) {
        profileViewModel.clearErrors()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Change Password",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Change Password Field - Current Password
            Text(
                text = "Current Password",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                placeholder = { Text("Enter your current password") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = profileViewModel.currentPasswordError.value != null,
                supportingText = {
                    profileViewModel.currentPasswordError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation =
                    if (currentPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (currentPasswordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = {
                            currentPasswordVisible = !currentPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle current password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Change Password Field - New Password
            Text(
                text = "New Password",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = { Text("Enter your new password") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = profileViewModel.newPasswordError.value != null,
                supportingText = {
                    profileViewModel.newPasswordError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation =
                    if (newPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (newPasswordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = {
                            newPasswordVisible = !newPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle new password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Change Password Field - Confirm New Password
            Text(
                text = "Confirm New Password",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                placeholder = { Text("Confirm your new password") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = profileViewModel.confirmNewPasswordError.value != null,
                supportingText = {
                    profileViewModel.confirmNewPasswordError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        profileViewModel.changePassword(
                            currentPassword,
                            newPassword,
                            confirmNewPassword
                        )
                    }
                ),
                visualTransformation =
                    if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon =
                        if (confirmPasswordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                    IconButton(
                        onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Toggle confirm password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Change Password Button
            Button(
                onClick = {
                    profileViewModel.changePassword(
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                    )
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Update Password",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}