package com.lsk0522.nightstand.feature.donate

import android.app.Activity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class DonateViewModel @Inject constructor(
    private val repository: DonationRepository,
) : ViewModel() {

    val state: StateFlow<DonationState> = repository.state

    val thanks: SharedFlow<Unit> = repository.thanks

    /** Called every time the screen opens; reconnects if the service dropped. */
    fun connect() = repository.start()

    fun donate(activity: Activity, offer: DonationOffer) = repository.donate(activity, offer)
}
