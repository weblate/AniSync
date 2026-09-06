package com.anisync.android.presentation.feed.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anisync.android.R
import com.anisync.android.domain.FeedFilter
import com.anisync.android.domain.FeedMediaType
import com.anisync.android.domain.FeedScope
import com.anisync.android.presentation.components.EmptyState

/**
 * Nothing came back at all — in practice the network, since the feed is one request.
 *
 * Same mark, same words and same way out as Discover's offline state: two screens failing for one
 * reason should not look like two different problems.
 */
@Composable
fun FeedOfflineState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Public,
        title = stringResource(R.string.feed_empty_offline_title),
        description = stringResource(R.string.feed_empty_offline_desc),
        actionLabel = stringResource(R.string.retry),
        actionIcon = Icons.Default.Refresh,
        onAction = onRetry,
        emblemShape = RoundedCornerShape(22.dp),
        emblemContainer = MaterialTheme.colorScheme.secondaryContainer,
        emblemContent = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    )
}

/**
 * The feed loaded and had nothing to show, which happens for three different reasons.
 *
 * Only the filtered case is the viewer's own doing and undone in one tap, so only it takes the
 * emphasised action — the same rule the library and Discover follow.
 */
@Composable
fun FeedEmptyState(
    scope: FeedScope,
    filter: FeedFilter,
    mediaType: FeedMediaType,
    onSwitchToGlobal: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filterLabel = stringResource(
        when (filter) {
            FeedFilter.ALL -> R.string.feed_filter_all
            FeedFilter.STATUS -> R.string.feed_filter_status
            FeedFilter.LIST -> R.string.feed_filter_list
        }
    )
    val mediaLabel = stringResource(
        when (mediaType) {
            FeedMediaType.ANIME -> R.string.media_type_anime
            FeedMediaType.MANGA -> R.string.media_type_manga
        }
    )

    when {
        filter != FeedFilter.ALL -> EmptyState(
            icon = Icons.Default.Tune,
            title = stringResource(R.string.feed_empty_filtered_title, filterLabel),
            description = if (filter == FeedFilter.LIST) {
                stringResource(R.string.feed_empty_filtered_body, filterLabel, mediaLabel)
            } else {
                stringResource(R.string.feed_empty_filtered_body_single, filterLabel)
            },
            actionLabel = stringResource(R.string.feed_empty_clear_filters),
            actionIcon = Icons.Default.Close,
            onAction = onClearFilters,
            emblemShape = RoundedCornerShape(22.dp),
            emblemContainer = MaterialTheme.colorScheme.secondaryContainer,
            emblemContent = MaterialTheme.colorScheme.onSecondaryContainer,
            actionEmphasised = true,
            animationKey = filter,
            modifier = modifier
        )

        scope == FeedScope.FOLLOWING -> EmptyState(
            icon = Icons.Default.Group,
            title = stringResource(R.string.feed_empty_following_title),
            description = stringResource(R.string.feed_empty_following_desc),
            actionLabel = stringResource(R.string.feed_empty_following_action),
            actionIcon = Icons.Default.Public,
            onAction = onSwitchToGlobal,
            emblemShape = RoundedCornerShape(22.dp),
            emblemContainer = MaterialTheme.colorScheme.secondaryContainer,
            emblemContent = MaterialTheme.colorScheme.onSecondaryContainer,
            animationKey = scope,
            modifier = modifier
        )

        else -> EmptyState(
            icon = Icons.Default.DynamicFeed,
            title = stringResource(R.string.feed_empty_global_title),
            description = stringResource(R.string.feed_empty_global_desc),
            emblemShape = RoundedCornerShape(22.dp),
            emblemContainer = MaterialTheme.colorScheme.secondaryContainer,
            emblemContent = MaterialTheme.colorScheme.onSecondaryContainer,
            animationKey = scope,
            modifier = modifier
        )
    }
}
