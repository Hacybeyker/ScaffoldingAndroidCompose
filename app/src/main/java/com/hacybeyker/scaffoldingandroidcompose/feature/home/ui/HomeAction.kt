package com.hacybeyker.scaffoldingandroidcompose.feature.home.ui

sealed interface HomeAction {
    data class SubmitName(val name: String) : HomeAction
}
