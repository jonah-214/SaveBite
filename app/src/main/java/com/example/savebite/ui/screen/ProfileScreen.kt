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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.savebite.ui.navigation.AppRoutes
import com.example.savebite.ui.navigation.AppTopBar
import com.example.savebite.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    navController: NavHostController,
    profileViewModel: ProfileViewModel
) {
    var notificationEnabled by remember { mutableStateOf(true) }
    val user = profileViewModel.user.value

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Profile & Settings",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // Profile Header - Name, email, phone
            ProfileHeaderCard(
                username = user?.username ?: "Loading...",
                email = user?.email ?: "",
                phone = user?.phone ?: ""
            )

            Spacer(Modifier.height(20.dp))

            // Account Settings tab
            SectionLabel("Account Settings")
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
                }
            )

            Spacer(Modifier.height(12.dp))

            // Preferences tab
            SectionLabel("Preferences")
            PreferencesCard(
                notificationEnabled = notificationEnabled,
                onNotificationToggle = { notificationEnabled = it },
            )

            // Support tab
            Spacer(Modifier.height(12.dp))
            SectionLabel("Support")
            SupportCard(
                onAboutUsClick = { /* TODO */ }
            )

            Spacer(Modifier.height(24.dp))
            LogoutButton(
                onClick = {
                    profileViewModel.logout {
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileHeaderCard(
    username: String,
    email: String,
    phone: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
            // Avatar Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name, email, phone
            Column {
                Text(
                    text = username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = phone,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AccountSettingsCard(
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Manage Profile & Password Settings
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsRow(
                icon = Icons.Default.Person,
                label = "Edit Profile",
                subtitle = "Change profile picture, number, E-mail",
                onClick = onEditProfileClick
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsRow(
                icon = Icons.Default.Lock,
                label = "Change Password",
                subtitle = "Update and strengthen account security",
                onClick = onChangePasswordClick
            )
        }
    }
}

@Composable
fun PreferencesCard(
    notificationEnabled: Boolean,
    onNotificationToggle: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Notification Preference
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsRow(
                icon = Icons.Default.Notifications,
                label = "Notification",
                subtitle = "Customize your notification preferences",
                onClick = { onNotificationToggle(!notificationEnabled) },
                trailing = {
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = onNotificationToggle
                    )
                }
            )
        }
    }
}

@Composable
fun SupportCard(
    onAboutUsClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
                icon = Icons.Default.Info,
                label = "About Us",
                subtitle = "Learn more about SaveBite",
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
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Settings Row Labels & Subtitles
            Column {
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
        trailing()
    }
}

@Composable
fun LogoutButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = "Logout",
            fontWeight = FontWeight.Bold
        )
    }
}