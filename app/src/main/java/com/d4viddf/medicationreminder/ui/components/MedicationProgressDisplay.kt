package com.d4viddf.medicationreminder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
// import androidx.compose.material3.WavyProgressIndicatorDefaults // No es estrictamente necesario si defines todo manualmente
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke 
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d4viddf.medicationreminder.ui.colors.MedicationColor

// Data class ProgressDetails (sin cambios)
data class ProgressDetails(
    val taken: Int,
    val remaining: Int,
    val totalFromPackage: Int,
    val progressFraction: Float,
    val displayText: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MedicationProgressDisplay(
    progressDetails: ProgressDetails?,
    colorScheme: MedicationColor,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnimation && progressDetails != null) progressDetails.progressFraction else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "ProgressAnimation"
    )

    LaunchedEffect(progressDetails) {
        startAnimation = false
        startAnimation = true
    }

    val density = LocalDensity.current
    val desiredStrokeWidth = 12.dp
    val desiredStrokeWidthPx = with(density) { desiredStrokeWidth.toPx() }
    val indicatorSize = 220.dp

    val accessibilityDescription = if (progressDetails != null && progressDetails.totalFromPackage > 0) {
        stringResource(
            id = com.d4viddf.medicationreminder.R.string.medication_progress_display_acc,
            progressDetails.taken,
            progressDetails.totalFromPackage,
            progressDetails.remaining
        )
    } else if (progressDetails != null && progressDetails.displayText != stringResource(id = com.d4viddf.medicationreminder.R.string.info_not_available_short)) { // Assuming "N/A" corresponds to a general "not available"
        stringResource(
            id = com.d4viddf.medicationreminder.R.string.medication_progress_display_not_package_based_acc,
            progressDetails.remaining, // This seems to be a count, ensure it's what's expected for %1$s
            progressDetails.taken // This seems to be a count, ensure it's what's expected for %2$s
        )
    } else {
        stringResource(id = com.d4viddf.medicationreminder.R.string.medication_progress_display_info_not_available_acc)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = indicatorSize + 16.dp) // Altura mínima basada en el tamaño del indicador + padding
            .padding(vertical = 8.dp)
            .semantics { contentDescription = accessibilityDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center, // Centra el contenido del Box (la Column con los textos)
            modifier = Modifier.size(indicatorSize)
        ) {
            CircularWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.progressBarColor,
                trackColor = colorScheme.progressBackColor,
                stroke = Stroke(width = desiredStrokeWidthPx),
                trackStroke = Stroke(width = desiredStrokeWidthPx),
                wavelength = 52.dp,
                waveSpeed = 4.dp,
                gapSize = 0.dp
            )

            // Column para agrupar "Progreso" y los números, y centrar esta Column dentro del Box
            if (progressDetails != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = com.d4viddf.medicationreminder.R.string.medication_progress_display_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.textColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progressDetails.displayText,
                        fontSize = if (progressDetails.displayText == stringResource(id = com.d4viddf.medicationreminder.R.string.info_not_available_short)) 48.sp else 32.sp, // Compare with resource
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.textColor,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}