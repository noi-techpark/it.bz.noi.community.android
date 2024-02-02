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
import it.bz.noi.community.storage.getPrivacyAcceptedFlow
import it.bz.noi.community.storage.setPrivacyAccepted
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class OnboardingViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

	@OptIn(FlowPreview::class)
	val isPrivacyAcceptedFlow = getApplication<Application>().getPrivacyAcceptedFlow().debounce(125)

	fun setPrivacyAccepted(accepted: Boolean) {
		viewModelScope.launch {
			getApplication<Application>().setPrivacyAccepted(accepted)
		}
	}

	val status: Flow<AuthStateStatus> = AuthManager.status
}
