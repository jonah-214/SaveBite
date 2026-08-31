package com.example.savebite.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ProfileViewModel
import com.example.savebite.utils.Validators

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
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Check if any field is touched
    val isChanged = currentPassword.isNotEmpty() ||
            newPassword.isNotEmpty() ||
            confirmNewPassword.isNotEmpty()

    // Check if all fields are filled and the new password meets format rules
    val isFormValid = currentPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmNewPassword.isNotBlank() &&
            Validators.validatePassword(newPassword) == null

    // Handle password change success and pop back stack
    LaunchedEffect(profileViewModel.changePasswordSuccess.value) {
        if (profileViewModel.changePasswordSuccess.value) {
            navController.popBackStack()
        }
    }

    // Reset errors when screen is entered
    LaunchedEffect(Unit) {
        profileViewModel.clearErrors()
    }

    // Intercept back button by showing discard changes dialog if there are unsaved changes
    BackHandler(enabled = isChanged) {
        showDiscardDialog = true
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(text = "Discard Changes?") },
            text = { Text(text = "You have unsaved changes. Are you sure you want to discard them and go back?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Change Password",
                showBackButton = true,
                onBackClick = {
                    if (isChanged) {
                        showDiscardDialog = true
                    } else {
                        navController.popBackStack()
                    }
                }
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
                onValueChange = {
                    currentPassword = it
                    profileViewModel.clearCurrentPasswordError()
                },
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
                    val iconRes =
                        if (currentPasswordVisible) R.drawable.visibility
                        else R.drawable.visibility_off
                    IconButton(
                        onClick = {
                            currentPasswordVisible = !currentPasswordVisible
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Toggle current password visibility",
                            modifier = Modifier.size(24.dp)
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
                onValueChange = {
                    newPassword = it
                    profileViewModel.clearNewPasswordError()
                },
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
                    val iconRes =
                        if (newPasswordVisible) R.drawable.visibility
                        else R.drawable.visibility_off
                    IconButton(
                        onClick = {
                            newPasswordVisible = !newPasswordVisible
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Toggle new password visibility",
                            modifier = Modifier.size(24.dp)
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
                onValueChange = {
                    confirmNewPassword = it
                    profileViewModel.clearConfirmNewPasswordError()
                },
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
                    val iconRes =
                        if (confirmPasswordVisible) R.drawable.visibility
                        else R.drawable.visibility_off
                    IconButton(
                        onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Toggle confirm password visibility",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Password Requirements Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Password Requirements",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val requirements = listOf(
                        "At least 6 characters",
                        "One uppercase letter (A-Z)",
                        "One lowercase letter (a-z)",
                        "One number (0-9)"
                    )

                    requirements.forEach { requirement ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isRequirementMet(requirement, newPassword))
                                    Color(0xFF4CAF50)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = requirement,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isRequirementMet(requirement, newPassword))
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

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
                enabled = isFormValid && !profileViewModel.isLoading.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (profileViewModel.isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Update Password",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun isRequirementMet(
    requirement: String,
    password: String
): Boolean {
    return when (requirement) {
        "At least 6 characters" -> password.length >= 6
        "One uppercase letter (A-Z)" -> password.any { it.isUpperCase() }
        "One lowercase letter (a-z)" -> password.any { it.isLowerCase() }
        "One number (0-9)" -> password.any { it.isDigit() }
        else -> false
    }
}
