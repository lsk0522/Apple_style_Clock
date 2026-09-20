package com.lsk0522.nightstand.feature.main

import androidx.lifecycle.ViewModel
import com.lsk0522.nightstand.feature.widgets.StandbyWidgetHost
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Hands the widget host to a Composable that only wants to read from it.
 *
 * `StandbyWidgetHost` is a singleton, but a plain Composable has no injection
 * point and a ViewModel is the cheapest one that exists.
 */
@HiltViewModel
class DeveloperHostHolder @Inject constructor(
    val host: StandbyWidgetHost,
) : ViewModel()
