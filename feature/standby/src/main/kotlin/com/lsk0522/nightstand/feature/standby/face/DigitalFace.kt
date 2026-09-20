package com.lsk0522.nightstand.feature.standby.face

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.R as DesignR
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType

/**
 * Hour stacked over minute, with the date and charge alongside.
 *
 * The two halves are laid out as one block and centred together. Giving the
 * date column a weight instead pushes them to opposite screen edges, where
 * they stop reading as a single clock.
 */
@Composable
fun DigitalFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usable = maxHeight - VerticalMargin * 2
        val digitSize = with(LocalDensity.current) { (usable * DigitRatio).toSp() }
        val style = digitStyle(digitSize)

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = SideMargin, vertical = VerticalMargin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = data.now.format(weekdayFormatter()),
                    style = NightstandType.Title1,
                    color = palette.textPrimary,
                )
                Text(
                    text = data.now.format(dateFormatter()),
                    style = NightstandType.Title3,
                    color = palette.textSecondary,
                )
                if (data.batteryPercent != null) {
                    Spacer(Modifier.height(10.dp))
                    BatteryLine(data)
                }
            }

            Spacer(Modifier.width(ColumnGap))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data.now.format(hourFormatter(data.use24Hour)),
                    style = style,
                    color = palette.textPrimary,
                )
                Text(
                    text = data.now.format(MinuteFormat),
                    style = style,
                    color = palette.textPrimary,
                )
            }
        }
    }
}

@Composable
internal fun BatteryLine(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby
    val percent = data.batteryPercent ?: return

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (data.charging) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_bolt),
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = "$percent%",
            style = NightstandType.Title3,
            color = if (data.charging) palette.accent else palette.textTertiary,
        )
    }
}

private const val DigitRatio = 0.42f
private val SideMargin = 40.dp
private val VerticalMargin = 24.dp
private val ColumnGap = 44.dp
