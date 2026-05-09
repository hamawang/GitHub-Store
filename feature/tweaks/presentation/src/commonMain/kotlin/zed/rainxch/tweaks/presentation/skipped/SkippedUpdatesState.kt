package zed.rainxch.tweaks.presentation.skipped

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SkippedUpdatesState(
    val isLoading: Boolean = true,
    val items: ImmutableList<SkippedAppUi> = persistentListOf(),
)

data class SkippedAppUi(
    val packageName: String,
    val appName: String,
    val skippedTag: String,
    val installedVersion: String,
)
