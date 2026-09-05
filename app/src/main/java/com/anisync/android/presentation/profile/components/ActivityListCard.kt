package com.anisync.android.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anisync.android.R
import com.anisync.android.domain.UserActivity
import com.anisync.android.domain.url
import com.anisync.android.presentation.components.UserAvatar
import com.anisync.android.presentation.components.formatRelativeTimeSeconds
import com.anisync.android.presentation.util.selectedPaneItem

/**
 * A list update as a row rather than a post.
 *
 * "Watched episode 5" is one fact, and it used to wear the whole status-card chrome: a 40dp author
 * header, a tinted box nested inside the card holding the cover, then an engagement footer, for
 * around 150dp of feed. The cover carries the identity here, the fact sits beside it, and the
 * replies and likes share the last line with whoever replied last.
 */
@Composable
internal fun ActivityListCard(
    activity: UserActivity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAuthor: Boolean = true,
    selected: Boolean = false,
    onUserClick: (String) -> Unit = {},
    onMediaClick: (Int) -> Unit = {},
    onLastReplyClick: (activityId: Int, replyId: Int) -> Unit = { _, _ -> },
    onLikeClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .selectedPaneItem(selected, shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 10.dp, end = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActivityCover(activity = activity, onMediaClick = onMediaClick)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (showAuthor) {
                    ActivityAuthorLine(activity = activity, onUserClick = onUserClick)
                }
                Text(
                    text = activity.mediaTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ActivityProgressLine(activity = activity)
                Spacer(Modifier.weight(1f))
                ActivityListFooter(
                    activity = activity,
                    onLastReplyClick = onLastReplyClick,
                    onCommentClick = onClick,
                    onLikeClick = onLikeClick
                )
            }
        }
    }
}

@Composable
private fun ActivityCover(activity: UserActivity, onMediaClick: (Int) -> Unit) {
    val coverModifier = Modifier
        .width(70.dp)
        .aspectRatio(0.7f)
        .clip(RoundedCornerShape(12.dp))
        .let { base ->
            val mediaId = activity.mediaId
            if (mediaId != null) base.clickable { onMediaClick(mediaId) } else base
        }
        .background(MaterialTheme.colorScheme.surfaceVariant)

    if (activity.mediaCoverUrl != null) {
        AsyncImage(
            model = activity.mediaCover.url() ?: activity.mediaCoverUrl,
            contentDescription = activity.mediaTitle,
            modifier = coverModifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = coverModifier, contentAlignment = Alignment.Center) {
            Text(
                text = activity.mediaTitle.take(2).ifBlank { "??" }.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityAuthorLine(activity: UserActivity, onUserClick: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        UserAvatar(
            url = activity.userAvatarUrl,
            contentDescription = activity.userName,
            size = 20.dp,
            modifier = Modifier.clickable { activity.userName?.let(onUserClick) }
        )
        Text(
            text = activity.userName.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .clickable { activity.userName?.let(onUserClick) }
        )
        Text(
            text = formatRelativeTimeSeconds(LocalResources.current, activity.timestamp / 1000L),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}

/** "Watched episode 5", with the number in the accent the rest of the app uses for progress. */
@Composable
private fun ActivityProgressLine(activity: UserActivity) {
    val primary = MaterialTheme.colorScheme.primary
    val text = remember(activity.status, activity.progress, primary) {
        val status = (activity.status ?: "Updated")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        buildAnnotatedString {
            append(status)
            activity.progress?.takeIf { it.isNotBlank() }?.let { progress ->
                append(" ")
                withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) {
                    append(progress)
                }
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        activity.mediaType?.let { MediaTypeLabel(it) }
    }
}

@Composable
private fun ActivityListFooter(
    activity: UserActivity,
    onLastReplyClick: (activityId: Int, replyId: Int) -> Unit,
    onCommentClick: () -> Unit,
    onLikeClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivityLastReply(
            activity = activity,
            onLastReplyClick = onLastReplyClick,
            modifier = Modifier.weight(1f)
        )
        ActivityCounts(
            activity = activity,
            onCommentClick = onCommentClick,
            onLikeClick = onLikeClick,
            compact = true
        )
    }
}

/**
 * "Rin replied 2h ago", or nothing when no one has. Sized down from the status card's version so it
 * can share the row with the counts.
 */
@Composable
internal fun ActivityLastReply(
    activity: UserActivity,
    onLastReplyClick: (activityId: Int, replyId: Int) -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: androidx.compose.ui.unit.Dp = 16.dp
) {
    val replyUserName = activity.replyUserName
    val repliedAt = activity.repliedAt
    if (replyUserName == null || repliedAt == null) {
        Spacer(modifier)
        return
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable {
                activity.lastReplyId?.let { onLastReplyClick(activity.id, it) }
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        UserAvatar(
            url = activity.replyUserAvatarUrl,
            contentDescription = replyUserName,
            size = avatarSize
        )
        Text(
            text = stringResource(
                R.string.activity_last_reply,
                replyUserName,
                formatRelativeTimeSeconds(LocalResources.current, repliedAt)
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Replies and likes, right-aligned so the thumb reaches them without crossing the card. */
@Composable
internal fun ActivityCounts(
    activity: UserActivity,
    onCommentClick: () -> Unit,
    onLikeClick: (() -> Unit)?,
    compact: Boolean = false
) {
    val isLiked = activity.isLiked
    val likeColor = if (isLiked) LikedColor else MaterialTheme.colorScheme.primary
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActivityStatPill(
            icon = Icons.Outlined.ChatBubbleOutline,
            value = activity.replyCount,
            onClick = onCommentClick,
            contentDescription = stringResource(R.string.cd_comments),
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = if (compact) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            }
        )
        ActivityStatPill(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            value = activity.likeCount,
            onClick = onLikeClick,
            contentDescription = stringResource(
                if (isLiked) R.string.cd_unlike else R.string.cd_like
            ),
            contentColor = likeColor,
            containerColor = if (compact) Color.Transparent else likeColor.copy(alpha = 0.1f)
        )
    }
}

/** AniList's own like red, kept out of the theme so it reads the same on every palette. */
internal val LikedColor = Color(0xFFBE123C)
