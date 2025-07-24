package io.yavero.almasasuite.pos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.pos.localization.Language
import io.yavero.almasasuite.pos.localization.StringResources
import io.yavero.almasasuite.pos.localization.getString
import io.yavero.almasasuite.pos.localization.rememberLocalizationManager


@Composable
fun CompactLanguageSwitcher(
    modifier: Modifier = Modifier
) {
    val localizationManager = rememberLocalizationManager()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()

    IconButton(
        onClick = { localizationManager.toggleLanguage() },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = getString(StringResources.SWITCH_LANGUAGE),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (currentLanguage) {
                    Language.ENGLISH -> "ع"
                    Language.ARABIC -> "EN"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}