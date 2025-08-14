// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import kotlinx.coroutines.flow.Flow

class OnboardingViewModel(
    app: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

	companion object {
		private const val PRIVACY_ACCEPTED_STATE = "privacy_accepted"
	}

	val isPrivacyAcceptedFlow: Flow<Boolean> = savedStateHandle.getStateFlow(PRIVACY_ACCEPTED_STATE, false)

	fun setPrivacyAccepted(accepted: Boolean) {
		savedStateHandle[PRIVACY_ACCEPTED_STATE] = accepted
	}

	val status: Flow<AuthStateStatus> = AuthManager.status
}
