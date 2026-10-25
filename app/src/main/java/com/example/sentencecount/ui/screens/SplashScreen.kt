package com.example.sentencecount.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var line1Visible by remember { mutableStateOf(false) }
    var line2Visible by remember { mutableStateOf(false) }
    var line3Visible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        line1Visible = true
        delay(250)
        line2Visible = true
        delay(250)
        line3Visible = true
        delay(200)
        subtitleVisible = true
        delay(350) // Total ~1.2 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SentenceCount",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Line 1
            AnimatedVisibility(
                visible = line1Visible,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(
                        modifier = Modifier.width(80.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This is one.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line 2
            AnimatedVisibility(
                visible = line2Visible,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(
                        modifier = Modifier.width(130.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "This is another!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line 3
            AnimatedVisibility(
                visible = line3Visible,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { 20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(
                        modifier = Modifier.width(170.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "And another?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Subtitle
            AnimatedVisibility(
                visible = subtitleVisible,
                enter = fadeIn(tween(250))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SENTENCE ANALYSIS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 2.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
