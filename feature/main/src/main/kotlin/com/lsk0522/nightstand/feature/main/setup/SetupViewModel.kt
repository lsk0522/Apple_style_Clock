package com.lsk0522.nightstand.feature.main.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.standby.StandbyAttempt
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptLog
import com.lsk0522.nightstand.core.data.standby.StandbyLauncher
import com.lsk0522.nightstand.core.data.system.AppVersion
import com.lsk0522.nightstand.core.data.system.SystemRequirement
import com.lsk0522.nightstand.core.data.system.SystemRequirementId
import android.content.Intent
import com.lsk0522.nightstand.core.data.system.SystemRequirements
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetupUiState(
    val requirements: List<SystemRequirement> = emptyList(),
    val setupSeen: Boolean = true,
    /** The build running on the device, so it can be verified at a glance. */
    val appVersion: String = "",
    /** Why the last power event did or did not bring up the clock. */
    val lastAttempt: StandbyAttempt? = null,
) {
    val blockingCount: Int get() = requirements.count { it.isRequired && !it.isSatisfied }

    /** Nothing required is missing. Optional items may still be off. */
    val isReady: Boolean get() = blockingCount == 0

    fun isSatisfied(id: SystemRequirementId): Boolean =
        requirements.firstOrNull { it.id == id }?.isSatisfied == true

    fun has(id: SystemRequirementId): Boolean = requirements.any { it.id == id }
}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val requirements: SystemRequirements,
    private val appVersion: AppVersion,
    private val standbyLauncher: StandbyLauncher,
    attemptLog: StandbyAttemptLog,
) : ViewModel() {

    /**
     * Bumped whenever the app comes back to the foreground. None of these
     * settings emit a change event, so the only reliable moment to re-read
     * them is when the user returns from the system screen.
     */
    private val refreshes = MutableStateFlow(0)

    val uiState: StateFlow<SetupUiState> =
        combine(
            repository.settings,
            refreshes,
            attemptLog.lastAttempt,
        ) { settings, _, attempt ->
            SetupUiState(
                requirements = requirements.check(),
                setupSeen = settings.setupSeen,
                appVersion = appVersion.display,
                lastAttempt = attempt,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SetupUiState(
                requirements = requirements.check(),
                appVersion = appVersion.display,
            ),
        )

    /**
     * Whether the first-run screen should stand in front of the app.
     *
     * Null until the stored value has actually been read. Defaulting to false
     * would show the tab bar for a frame and then yank it away, which reads as
     * a glitch -- and worse, invites the user to start tapping options that do
     * not work yet.
     */
    val showSetup: StateFlow<Boolean?> = repository.settings
        .map { !it.setupSeen }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null,
        )

    fun refresh() {
        refreshes.value += 1
    }

    fun dismissSetup() {
        viewModelScope.launch { repository.setSetupSeen(true) }
    }

    fun reopenSetup() {
        viewModelScope.launch { repository.setSetupSeen(false) }
    }


    /** Opens the clock straight away, without waiting for a charger. */
    fun launchStandby() = standbyLauncher.launch()

    /** The system screen for [id], or null when it is a runtime permission. */
    fun settingsIntent(id: SystemRequirementId): Intent? = requirements.settingsIntent(id)

    /** Where to send the user when [settingsIntent] is refused by the device. */
    fun fallbackIntent(id: SystemRequirementId): Intent = requirements.fallbackIntent(id)

    fun screenSaverIntent(): Intent = requirements.screenSaverIntent()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
