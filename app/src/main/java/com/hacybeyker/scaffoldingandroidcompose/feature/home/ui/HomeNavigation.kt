package com.hacybeyker.scaffoldingandroidcompose.feature.home.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeNavKey> { HomeScreen() }
}
