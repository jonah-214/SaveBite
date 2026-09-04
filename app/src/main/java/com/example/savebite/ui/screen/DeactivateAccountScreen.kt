package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ProfileViewModel

@Composable
fun DeactivateAccountScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val deleteKeyword = stringResource(R.string.deactivate_account_type_delete_keyword)
    val isDeleteConfirmed = deleteConfirmText == deleteKeyword

    // Reset errors when screen is entered
    LaunchedEffect(Unit) {
        profileViewModel.clearErrors()
    }

    // On success, drop the whole back stack down to Log in — there's no "Profile" to go back to
    // once the account is deactivated and the session cleared.
    val deactivateAndNavigateToLogin: () -> Unit = {
        profileViewModel.deactivateAccount(password) {
            navController.navigate(AppRoutes.LOGIN) {
                popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.deactivate_account_title),
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

            // Warning card — explains what deactivating does and doesn't do, up front
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.info),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.deactivate_account_warning_heading),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.deactivate_account_warning_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password confirmation — deactivating is destructive-ish, so require re-entering
            // the password rather than a plain "Are you sure?" dialog, same idea as Change Password.
            Text(
                text = stringResource(R.string.deactivate_account_password_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    profileViewModel.clearDeactivatePasswordError()
                },
                placeholder = { Text(stringResource(R.string.deactivate_account_password_placeholder)) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = profileViewModel.deactivatePasswordError.value != null,
                supportingText = {
                    profileViewModel.deactivatePasswordError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    val iconRes =
                        if (passwordVisible) R.drawable.visibility
                        else R.drawable.visibility_off
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(R.string.content_desc_toggle_deactivate_password),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Type-to-confirm — an extra deliberate step before a destructive-ish action,
            // same idea as GitHub's "type the repo name to delete it".
            Text(
                text = stringResource(R.string.deactivate_account_type_delete_label),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = deleteConfirmText,
                onValueChange = { deleteConfirmText = it },
                placeholder = { Text(stringResource(R.string.deactivate_account_type_delete_placeholder)) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                isError = deleteConfirmText.isNotEmpty() && !isDeleteConfirmed,
                supportingText = {
                    if (deleteConfirmText.isNotEmpty() && !isDeleteConfirmed) {
                        Text(
                            text = stringResource(R.string.deactivate_account_type_delete_mismatch),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (password.isNotBlank() && isDeleteConfirmed) showConfirmDialog = true
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Deactivate Account button — opens the final confirmation dialog rather than
            // deactivating right away.
            Button(
                onClick = { showConfirmDialog = true },
                enabled = password.isNotBlank() && isDeleteConfirmed && !profileViewModel.isLoading.value,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (profileViewModel.isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    Text(
                        text = stringResource(R.string.deactivate_account_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Final confirmation dialog — the last chance to back out before the account is
    // actually deactivated and the session cleared.
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(text = stringResource(R.string.deactivate_account_confirm_dialog_title)) },
            text = { Text(text = stringResource(R.string.deactivate_account_confirm_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        deactivateAndNavigateToLogin()
                    }
                ) {
                    Text(
                        stringResource(R.string.deactivate_account_confirm_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.outline)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
