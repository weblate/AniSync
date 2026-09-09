package com.anisync.android.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Top-bar chrome for a detail screen whose header is a banner. While the bar is transparent the
 * icons sit on artwork of any brightness, so each one carries a scrim disc and turns white; once the
 * bar is opaque they take the theme's colours. Shared so media, character, staff and studio details
 * cannot drift apart.
 */
@Composable
fun bannerChromeTint(overBanner: Boolean): Color = animateColorAsState(
    if (overBanner) Color.White else MaterialTheme.colorScheme.onSurface,
    label = "bannerChromeTint"
).value

/** Scrim disc behind [bannerChromeTint]; transparent once the chrome is off the banner. */
@Composable
fun bannerChromeContainer(overBanner: Boolean): Color = animateColorAsState(
    if (overBanner) Color.Black.copy(alpha = 0.36f) else Color.Transparent,
    label = "bannerChromeScrim"
).value

/** [bannerChromeTint] and [bannerChromeContainer] as `IconButton` colours. */
@Composable
fun bannerChromeColors(overBanner: Boolean): IconButtonColors =
    IconButtonDefaults.iconButtonColors(
        containerColor = bannerChromeContainer(overBanner),
        contentColor = bannerChromeTint(overBanner)
    )
