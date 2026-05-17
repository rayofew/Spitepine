package com.reznick.spitecards.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reznick.spitecards.data.model.Card
import com.reznick.spitecards.data.model.Suit
import com.reznick.spitecards.ui.theme.SpiteCardRed

private val Card.suitSymbol get() = when (suit) {
    Suit.CLUBS -> "♣"
    Suit.DIAMONDS -> "♦"
    Suit.HEARTS -> "♥"
    Suit.SPADES -> "♠"
}

private val Card.color get() = when (suit) {
    Suit.HEARTS, Suit.DIAMONDS -> SpiteCardRed
    else -> Color(0xFF212121)
}

@Composable
fun CardView(
    card: Card,
    modifier: Modifier = Modifier,
    faceDown: Boolean = false,
    selected: Boolean = false,
    width: Dp = 56.dp,
    height: Dp = 80.dp,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(6.dp)
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(if (faceDown) Color(0xFF1A5276) else Color.White, shape)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (!faceDown) {
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = card.rank.display,
                    color = card.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                )
                Text(
                    text = card.suitSymbol,
                    color = card.color,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = card.rank.display,
                    color = card.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun CardBack(modifier: Modifier = Modifier, width: Dp = 56.dp, height: Dp = 80.dp) {
    CardView(
        card = com.reznick.spitecards.data.model.Card(
            com.reznick.spitecards.data.model.Rank.ACE,
            com.reznick.spitecards.data.model.Suit.SPADES
        ),
        modifier = modifier,
        faceDown = true,
        width = width,
        height = height
    )
}
