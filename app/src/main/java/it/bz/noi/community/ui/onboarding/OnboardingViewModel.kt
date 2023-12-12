// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.storage.privacyAcceptedFlow
import it.bz.noi.community.storage.updatePrivacyAccepted
import it.bz.noi.community.utils.savedStateProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

	val isPrivacyAccepted = getApplication<Application>().privacyAcceptedFlow()

	fun setPrivacyAccepted(accepted: Boolean) {
		viewModelScope.launch {
			getApplication<Application>().updatePrivacyAccepted(accepted)
		}
	}

	val status: Flow<AuthStateStatus> = AuthManager.status
}
