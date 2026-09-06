package com.anisync.android.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anisync.android.R
import com.anisync.android.domain.UserActivity
import com.anisync.android.presentation.components.AppModalBottomSheet
import com.anisync.android.presentation.components.UserAvatar
import com.anisync.android.presentation.components.formatRelativeTimeSeconds
import com.anisync.android.presentation.util.openUrl
import com.anisync.android.presentation.util.shareActivity

/**
 * Everything a card's overflow offers, off the card and into a sheet.
 *
 * The header used to carry share and subscribe as permanent icon buttons next to the author. Only
 * subscribe stayed there — it is a state you read at a glance — and the rest moved here, where each
 * entry can afford a full label. Reporting is a hand-off: AniList takes reports on its website and
 * exposes no mutation for them, so the entry opens the activity there rather than pretending.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityActionsSheet(
    activity: UserActivity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var confirmReport by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val url = activityUrl(activity.id)

    AppModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(
                    url = activity.userAvatarUrl,
                    contentDescription = activity.userName,
                    size = 36.dp
                )
                Column {
                    Text(
                        text = activity.userName.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatRelativeTimeSeconds(
                            LocalResources.current,
                            activity.timestamp / 1000L
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            SheetAction(
                icon = Icons.Default.Share,
                label = stringResource(R.string.cd_share),
                onClick = {
                    onDismiss()
                    shareActivity(context, activity.id)
                }
            )
            SheetAction(
                icon = Icons.Default.OpenInNew,
                label = stringResource(R.string.activity_open_on_anilist),
                onClick = {
                    onDismiss()
                    openUrl(context, url)
                }
            )
            SheetAction(
                icon = Icons.Default.Flag,
                label = stringResource(R.string.activity_report),
                supporting = stringResource(R.string.activity_report_supporting),
                onClick = { confirmReport = true }
            )

            if (onEditClick != null) {
                SheetAction(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.edit),
                    onClick = {
                        onDismiss()
                        onEditClick()
                    }
                )
            }
            if (onDeleteClick != null) {
                SheetAction(
                    icon = Icons.Default.Delete,
                    label = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { confirmDelete = true }
                )
            }
        }
    }

    if (confirmReport) {
        AlertDialog(
            onDismissRequest = { confirmReport = false },
            icon = { Icon(Icons.Default.Flag, contentDescription = null) },
            title = { Text(stringResource(R.string.activity_report_title)) },
            text = { Text(stringResource(R.string.activity_report_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReport = false
                    onDismiss()
                    openUrl(context, url)
                }) {
                    Text(stringResource(R.string.activity_report_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReport = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (confirmDelete && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.activity_delete_confirm_title)) },
            text = { Text(stringResource(R.string.activity_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDismiss()
                    onDeleteClick()
                }) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    supporting: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (tint == MaterialTheme.colorScheme.error) {
                    tint
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun activityUrl(activityId: Int) = "https://anilist.co/activity/$activityId"
