package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.data.local.LocalArtist
import com.example.myapplication.ui.theme.CardDark
import com.example.myapplication.ui.theme.LightGray

@Composable
fun ArtistCard(
    artist: LocalArtist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CardDark),
            contentAlignment = Alignment.Center,
        ) {
            if (artist.imageUri != null) {
                AsyncImage(
                    model = artist.imageUri,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    placeholder = rememberImagePlaceholder(),
                    error = rememberImagePlaceholder(),
                    modifier = Modifier.matchParentSize(),
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = artist.name,
                    tint = LightGray,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "${artist.songCount} songs",
            style = MaterialTheme.typography.bodySmall,
            color = LightGray,
            textAlign = TextAlign.Center,
        )
    }
}
