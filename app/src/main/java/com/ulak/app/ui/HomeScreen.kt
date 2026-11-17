package com.ulak.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ulak.app.R
import com.ulak.app.ui.theme.SafeGreen
import com.ulak.app.ui.theme.SosRed
import com.ulak.app.ui.theme.UlakTheme

@Composable
fun HomeScreen() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard()

            // The two emergency buttons share all free space, so they stay large.
            ActionButton(
                text = stringResource(R.string.button_safe),
                color = SafeGreen,
                onClick = { /* Phase 2 */ }
            )
            ActionButton(
                text = stringResource(R.string.button_sos),
                color = SosRed,
                onClick = { /* Phase 2 */ }
            )

            OutlinedButton(
                onClick = { /* Phase 4 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(stringResource(R.string.button_start_communication), fontSize = 18.sp)
            }

            LastMessageCard()
        }
    }
}

@Composable
private fun StatusCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.status_title),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.status_off),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.status_off_detail),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ColumnScope.ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)
    ) {
        Text(text = text, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LastMessageCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.last_message_title),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(R.string.last_message_empty),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    UlakTheme {
        HomeScreen()
    }
}
