package com.d4viddf.medicationreminder.ui.components

import com.d4viddf.medicationreminder.data.MedicationSearchResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.d4viddf.medicationreminder.R
import com.d4viddf.medicationreminder.ui.theme.AppTheme

@Composable
fun MedicationSearchResultCard(
    medicationResult: MedicationSearchResult,
    onClick: () -> Unit,
    isSelected: Boolean, // New parameter
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer) // Changed for better visibility
        } else {
            CardDefaults.cardColors() // Default colors
        }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Replace with actual image loading logic once API is known
            // Using Coil's AsyncImage for future integration
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(medicationResult.imageUrl) // Use the actual image URL
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_pill_placeholder),
                error = painterResource(R.drawable.ic_pill_placeholder),
                contentDescription = medicationResult.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicationResult.name,
                    style = MaterialTheme.typography.titleMedium,
                    // fontWeight = FontWeight.Bold, // fontWeight is part of titleMedium by default
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp)) // Spacer for visual separation
                Text(
                    text = medicationResult.labtitular ?: "Unknown Laboratory", // Display labtitular
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationSearchResultCardPreview() {
    AppTheme {
        val sampleResult = MedicationSearchResult(
            name = "Aspirin 100mg Tablets (Sample)",
            description = "For pain relief",
            atcCode = "N02BA01",
            safetyNotes = "Not for children under 12.",
            administrationRoutes = listOf("Oral"),
            dosage = "1 tablet",
            documentUrls = emptyList(),
            nregistro = "12345",
            labtitular = "Sample Pharma Inc.", // Updated labtitular
            comercializado = true,
            requiereReceta = false,
            generico = true,
            imageUrl = "https://example.com/aspirin_image.jpg"
        )
        MedicationSearchResultCard(
            medicationResult = sampleResult,
            onClick = {},
            isSelected = false // Added isSelected for preview
        )
        Spacer(Modifier.height(8.dp)) // Spacer for second preview
        MedicationSearchResultCard(
            medicationResult = sampleResult.copy(name = "Aspirin 100mg (Selected)"),
            onClick = {},
            isSelected = true // Added isSelected for preview (selected state)
        )
    }
}
