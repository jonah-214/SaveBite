package com.example.savebite.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.savebite.R
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ProfileViewModel
import com.example.savebite.ui.viewmodel.ThemeViewModel
import com.example.savebite.utils.ThemeMode

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel,
    themeViewModel: ThemeViewModel
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val themeMode by themeViewModel.themeMode.collectAsState()
    val user = profileViewModel.user.value
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar text resolved here (composable scope) so it can be reused inside the
    // non-composable LaunchedEffect lambdas below.
    val profileUpdatedMessage = stringResource(R.string.profile_update_success)
    val profileUpdatedEmailPendingMessage = stringResource(R.string.profile_update_success_email_pending)
    val passwordChangedMessage = stringResource(R.string.profile_password_change_success)

    // Show success message when profile is updated
    LaunchedEffect(profileViewModel.updateSuccess.value) {
        if (profileViewModel.updateSuccess.value) {
            val message = if (profileViewModel.emailConfirmationPending.value) {
                profileUpdatedEmailPendingMessage
            } else {
                profileUpdatedMessage
            }
            snackbarHostState.showSnackbar(message)
            profileViewModel.resetUpdateSuccess()
        }
    }

    // Show success message when password is changed
    LaunchedEffect(profileViewModel.changePasswordSuccess.value) {
        if (profileViewModel.changePasswordSuccess.value) {
            snackbarHostState.showSnackbar(passwordChangedMessage)
            profileViewModel.resetPasswordChangeSuccess()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.profile_settings_title),
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // Profile Header - Avatar, Name, email, phone
            ProfileHeaderCard(
                username = user?.username ?: stringResource(R.string.profile_loading_username),
                email = user?.email ?: "",
                phone = user?.phone ?: "",
                avatarUrl = user?.avatarUrl
            )

            Spacer(Modifier.height(20.dp))

            // Account Settings tab
            SectionLabel(stringResource(R.string.profile_section_account_settings))
            AccountSettingsCard(
                onEditProfileClick = {
                    navController.navigate(AppRoutes.EDIT_PROFILE) {
                        launchSingleTop = true
                    }
                },
                onChangePasswordClick = {
                    navController.navigate(AppRoutes.CHANGE_PASSWORD) {
                        launchSingleTop = true
                    }
                },
                onDeactivateAccountClick = {
                    navController.navigate(AppRoutes.DEACTIVATE_ACCOUNT) {
                        launchSingleTop = true
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // Preferences tab
            SectionLabel(stringResource(R.string.profile_section_preferences))
            PreferencesCard(
                notificationEnabled = profileViewModel.notificationEnabled.value,
                onNotificationToggle = { profileViewModel.setNotificationEnabled(it) },
                themeMode = themeMode,
                onThemeModeChange = { themeViewModel.setThemeMode(it) },
                onRecipePreferencesClick = {
                    navController.navigate(AppRoutes.EDIT_RECIPE_PREFERENCES) {
                        launchSingleTop = true
                    }
                }
            )

            // Support tab
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.profile_section_support))
            SupportCard(
                onAboutUsClick = {
                    navController.navigate(AppRoutes.ABOUT_US) {
                        launchSingleTop = true
                    }
                }
            )

            // Logout button
            Spacer(Modifier.height(24.dp))
            LogoutButton(
                isLoading = profileViewModel.isLoading.value,
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout_dialog_title)) },
            text = { Text(stringResource(R.string.profile_logout_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        profileViewModel.logout {
                            navController.navigate(AppRoutes.LOGIN) {
                                popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.profile_logout_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun ProfileHeaderCard(
    username: String,
    email: String,
    phone: String,
    avatarUrl: String? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Display
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = stringResource(R.string.content_desc_profile_picture),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.account_circle),
                        contentDescription = stringResource(R.string.content_desc_avatar),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name, email, phone
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = phone,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AccountSettingsCard(
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeactivateAccountClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsRow(
                icon = R.drawable.person,
                label = stringResource(R.string.profile_edit_profile_label),
                subtitle = stringResource(R.string.profile_edit_profile_subtitle),
                onClick = onEditProfileClick
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsRow(
                icon = R.drawable.lock,
                label = stringResource(R.string.profile_change_password_label),
                subtitle = stringResource(R.string.profile_change_password_subtitle),
                onClick = onChangePasswordClick
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsRow(
                icon = R.drawable.remove_circle,
                label = stringResource(R.string.profile_deactivate_account_label),
                subtitle = stringResource(R.string.profile_deactivate_account_subtitle),
                onClick = onDeactivateAccountClick
            )
        }
    }
}

@Composable
fun PreferencesCard(
    notificationEnabled: Boolean,
    onNotificationToggle: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onRecipePreferencesClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsRow(
                icon = R.drawable.notification_settings,
                label = stringResource(R.string.profile_notification_label),
                subtitle = stringResource(R.string.profile_notification_subtitle),
                onClick = { onNotificationToggle(!notificationEnabled) },
                trailing = {
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = onNotificationToggle
                    )
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsRow(
                icon = R.drawable.chef_hat,
                label = stringResource(R.string.profile_recipe_preferences_label),
                subtitle = stringResource(R.string.profile_recipe_preferences_subtitle),
                onClick = onRecipePreferencesClick
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            AppearanceExpandableRow(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange
            )
        }
    }
}

@Composable
fun AppearanceExpandableRow(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Collapsed row - same look as other SettingsRow entries
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.dark_mode),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.profile_appearance_label),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = themeMode.displayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                painterResource(id = if (expanded) R.drawable.arrow_up else R.drawable.arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Expanded options list
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 34.dp, bottom = 8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    ThemeOptionRow(
                        label = mode.displayName(),
                        selected = mode == themeMode,
                        onClick = {
                            onThemeModeChange(mode)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.check),
                contentDescription = stringResource(R.string.content_desc_selected),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SupportCard(
    onAboutUsClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Support Settings
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsRow(
                icon = R.drawable.info,
                label = stringResource(R.string.profile_about_us_label),
                subtitle = stringResource(R.string.profile_about_us_subtitle),
                onClick = onAboutUsClick
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsRow(
    icon: Int,
    label: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {
        Icon(
            painterResource(id = R.drawable.arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // Reserves space for trailing() first, instead of letting a long subtitle grow
            // past the screen edge and push the trailing icon/switch off-screen.
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Settings Row Labels & Subtitles
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        trailing()
    }
}

@Composable
fun LogoutButton(
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.logout),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.profile_logout),
                fontWeight = FontWeight.Bold
            )
        }
    }
}