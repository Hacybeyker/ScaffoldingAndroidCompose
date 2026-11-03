package com.hacybeyker.scaffoldingandroidcompose.feature.home.ui

import com.hacybeyker.scaffoldingandroidcompose.feature.home.domain.model.Greeting

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(val greeting: Greeting) : HomeUiState
}
