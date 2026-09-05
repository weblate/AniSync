package com.anisync.android.presentation.feed.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anisync.android.R
import com.anisync.android.domain.FeedFilter
import com.anisync.android.domain.FeedMediaType
import com.anisync.android.domain.FeedScope
import com.anisync.android.presentation.components.ConnectedToggleDefaults
import com.anisync.android.presentation.components.ConnectedToggleSegment
import com.anisync.android.presentation.components.MediaTypeToggle
import com.anisync.android.presentation.util.bouncyClickable
import com.anisync.android.presentation.util.rememberHapticFeedback
import com.anisync.android.type.MediaType

private val RailHeight = ConnectedToggleDefaults.Height

/** Chip inset used until the pinned toggle has been measured. */
private val PinnedToggleInsetEstimate = 120.dp

/**
 * The whole feed header: which stream, then what kind of activity in it.
 *
 * The shipped screen spent a full-width scope group and a chip row on this — 114dp of chrome that
 * never scrolled away and shifted under you whenever the media toggle appeared and disappeared.
 * Scope keeps a row of its own (it is the question asked most, and it hosts the overflow), and
 * everything else shares the rail the Library and Discover screens already use: the media toggle
 * pinned at the start, the activity chips scrolling under it.
 */
@Composable
fun FeedRail(
    scope: FeedScope,
    filter: FeedFilter,
    mediaType: FeedMediaType,
    groupListUpdates: Boolean,
    mergeWindowLabel: String,
    onScopeChange: (FeedScope) -> Unit,
    onFilterChange: (FeedFilter) -> Unit,
    onMediaTypeChange: (FeedMediaType) -> Unit,
    onToggleGroupListUpdates: () -> Unit,
    onOpenActivitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeedScopeRow(
            scope = scope,
            groupListUpdates = groupListUpdates,
            mergeWindowLabel = mergeWindowLabel,
            onScopeChange = onScopeChange,
            onToggleGroupListUpdates = onToggleGroupListUpdates,
            onOpenActivitySettings = onOpenActivitySettings
        )
        FeedFilterRail(
            filter = filter,
            mediaType = mediaType,
            onFilterChange = onFilterChange,
            onMediaTypeChange = onMediaTypeChange
        )
    }
}

/**
 * Global or Following, on the connected shapes the media toggle uses, plus the feed's overflow.
 *
 * The two segments split the row rather than hugging their words: the space is theirs, and a wide
 * target is the point of a switch that carries the whole screen.
 */
@Composable
private fun FeedScopeRow(
    scope: FeedScope,
    groupListUpdates: Boolean,
    mergeWindowLabel: String,
    onScopeChange: (FeedScope) -> Unit,
    onToggleGroupListUpdates: () -> Unit,
    onOpenActivitySettings: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp)
            .height(RailHeight),
        horizontalArrangement = Arrangement.spacedBy(ConnectedToggleDefaults.Spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConnectedToggleSegment(
            icon = Icons.Default.Group,
            label = stringResource(R.string.feed_scope_following),
            selected = scope == FeedScope.FOLLOWING,
            leading = true,
            modifier = Modifier.weight(1f),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onScopeChange(FeedScope.FOLLOWING)
            }
        )
        ConnectedToggleSegment(
            icon = Icons.Default.Public,
            label = stringResource(R.string.feed_scope_global),
            selected = scope == FeedScope.GLOBAL,
            leading = false,
            modifier = Modifier.weight(1f),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onScopeChange(FeedScope.GLOBAL)
            }
        )
        Spacer(Modifier.width(4.dp))
        FeedMenuButton(
            groupListUpdates = groupListUpdates,
            mergeWindowLabel = mergeWindowLabel,
            onToggleGroupListUpdates = onToggleGroupListUpdates,
            onOpenActivitySettings = onOpenActivitySettings
        )
    }
}

/**
 * One pinned row carrying both "which media" and "which kind of activity".
 *
 * Media type only narrows list activity — a status carries no media — so the toggle dims under the
 * Status chip instead of vanishing and taking the row's shape with it.
 */
@Composable
private fun FeedFilterRail(
    filter: FeedFilter,
    mediaType: FeedMediaType,
    onFilterChange: (FeedFilter) -> Unit,
    onMediaTypeChange: (FeedMediaType) -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    // The pinned toggle is as wide as its translated labels make it, so the chips are inset by what
    // it measures rather than by a constant that only holds in English.
    var pinnedInset by remember { mutableStateOf(PinnedToggleInsetEstimate) }

    Box(modifier = Modifier.fillMaxWidth().height(RailHeight)) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = pinnedInset, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(FeedFilter.entries, key = { it.name }) { entry ->
                FeedFilterChip(
                    filter = entry,
                    selected = entry == filter,
                    onClick = { onFilterChange(entry) }
                )
            }
        }

        // Pinned media toggle over an opaque plate, so a chip scrolling under it does not show
        // through the seam between the segments, with a short fade at the hand-off.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.onSizeChanged { size ->
                pinnedInset = with(density) { size.width.toDp() }
            }
        ) {
            Row(
                modifier = Modifier
                    .background(background)
                    .height(RailHeight)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaTypeToggle(
                    selected = mediaType.toMediaType(),
                    onSelect = { onMediaTypeChange(it.toFeedMediaType()) },
                    enabled = filter != FeedFilter.STATUS
                )
            }
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(RailHeight)
                    .background(Brush.horizontalGradient(listOf(background, Color.Transparent)))
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(24.dp)
                .height(RailHeight)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, background)))
        )
    }
}

@Composable
private fun FeedFilterChip(
    filter: FeedFilter,
    selected: Boolean,
    onClick: () -> Unit
) {
    val haptic = rememberHapticFeedback()
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "FeedChipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "FeedChipContent"
    )
    val label = stringResource(filter.labelRes())

    Surface(
        color = container,
        shape = CircleShape,
        modifier = Modifier
            .height(RailHeight)
            .bouncyClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                role = Role.Tab,
                clipShape = CircleShape
            )
            .clearAndSetSemantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = label
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = filter.icon(),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = content,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

private fun FeedFilter.icon(): ImageVector = when (this) {
    FeedFilter.ALL -> Icons.Default.DynamicFeed
    FeedFilter.STATUS -> Icons.AutoMirrored.Outlined.Notes
    FeedFilter.LIST -> Icons.AutoMirrored.Filled.ViewList
}

private fun FeedFilter.labelRes(): Int = when (this) {
    FeedFilter.ALL -> R.string.feed_filter_all
    FeedFilter.STATUS -> R.string.feed_filter_status
    FeedFilter.LIST -> R.string.feed_filter_list
}

private fun FeedMediaType.toMediaType(): MediaType = when (this) {
    FeedMediaType.ANIME -> MediaType.ANIME
    FeedMediaType.MANGA -> MediaType.MANGA
}

private fun MediaType.toFeedMediaType(): FeedMediaType = when (this) {
    MediaType.MANGA -> FeedMediaType.MANGA
    else -> FeedMediaType.ANIME
}
