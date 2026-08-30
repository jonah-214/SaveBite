package com.example.savebite.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ProfileViewModel

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    val user = profileViewModel.user.value
    var username by remember { mutableStateOf(user?.username ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Check if details were changed and fields are not blank
    val isChanged = username != (user?.username ?: "") ||
            email != (user?.email ?: "") ||
            phone != (user?.phone ?: "")

    // Check if all fields are filled
    val canSave = isChanged &&
            username.isNotBlank() &&
            email.isNotBlank() &&
            phone.isNotBlank()

    // Reset errors when screen is entered
    LaunchedEffect(Unit) {
        profileViewModel.clearErrors()
    }

    // Update fields when user changes
    LaunchedEffect(user) {
        if (user != null) {
            username = user.username
            email = user.email
            phone = user.phone
        }
    }

    // Update fields when update is successful
    LaunchedEffect(
        profileViewModel.updateSuccess.value
    ) {
        if (profileViewModel.updateSuccess.value) {
            navController.popBackStack()
        }
    }

    // Intercept back button by showing discard dialog if changes were made
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

    // Edit Profile Screen
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Profile",
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
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar with edit badge
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                val context = LocalContext.current
                var menuExpanded by remember { mutableStateOf(false) }

                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes != null) {
                            profileViewModel.uploadAvatar(bytes)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.avatarUrl != null) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle),
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }

                // Edit badge
                FloatingActionButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit profile picture",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (user?.avatarUrl != null) "Replace Picture" else "Add Picture") },
                        onClick = {
                            menuExpanded = false
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                    if (user?.avatarUrl != null) {
                        DropdownMenuItem(
                            text = { Text("Remove Picture") },
                            onClick = {
                                menuExpanded = false
                                profileViewModel.removeAvatar()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Edit Profile Field - Username
            Text(
                text = "Username",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    profileViewModel.clearUsernameError()
                },
                placeholder = { Text("Enter your new username") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                isError = profileViewModel.usernameError.value != null,
                supportingText = {
                    profileViewModel.usernameError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Edit Profile Field - Email
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    profileViewModel.clearEmailError()
                },
                placeholder = { Text("Enter your new email address") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = profileViewModel.emailError.value != null,
                supportingText = {
                    profileViewModel.emailError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Edit Profile Field - Phone
            Text(
                text = "Phone Number",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    profileViewModel.clearPhoneError()
                },
                placeholder = { Text("Enter your new phone number") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        profileViewModel.updateProfile(
                            username,
                            email,
                            phone
                        )
                    }
                ),
                isError = profileViewModel.phoneError.value != null,
                supportingText = {
                    profileViewModel.phoneError.value?.let { errorMsg ->
                        Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Save Changes Button
            Button(
                onClick = {
                    profileViewModel.updateProfile(
                        username,
                        email,
                        phone
                    )
                },
                enabled = canSave && !profileViewModel.isLoading.value,
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
                        text = "Save Changes",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}