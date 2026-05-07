package com.reznick.spitepine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reznick.spitepine.data.model.TreeStatus

// Pin colors per spec §4.1, lifted to small inline chips for list/detail UI.
// Map markers (Phase 4) will reuse these.
private val SpottedGray = Color(0xFF9E9E9E)
private val AskedYellow = Color(0xFFD4A017)
private val GrantedGreen = Color(0xFF2D7D5A)
private val DeniedRed = Color(0xFFB3261E)
private val ExpiredOrange = Color(0xFFCC6F2A)
private val HarvestedBlue = Color(0xFF3F6A99)

fun TreeStatus.color(): Color = when (this) {
    TreeStatus.SPOTTED -> SpottedGray
    TreeStatus.ASKED -> AskedYellow
    TreeStatus.GRANTED -> GrantedGreen
    TreeStatus.DENIED -> DeniedRed
    TreeStatus.EXPIRED -> ExpiredOrange
    TreeStatus.HARVESTED_RECENTLY -> HarvestedBlue
}

fun TreeStatus.label(): String = when (this) {
    TreeStatus.SPOTTED -> "Spotted"
    TreeStatus.ASKED -> "Asked"
    TreeStatus.GRANTED -> "Permission"
    TreeStatus.DENIED -> "Denied"
    TreeStatus.EXPIRED -> "Expired"
    TreeStatus.HARVESTED_RECENTLY -> "Harvested"
}

@Composable
fun StatusBadge(status: TreeStatus, modifier: Modifier = Modifier) {
    Text(
        text = status.label(),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(status.color())
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
