package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CanvasBackgroundStyle

@Composable
fun PageManagerBar(
    currentPageIndex: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onAddPage: () -> Unit,
    onDeletePage: () -> Unit,
    currentBackgroundStyle: CanvasBackgroundStyle,
    onBackgroundStyleSelected: (CanvasBackgroundStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBackgroundPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(12.dp)
            .testTag("page_manager_bar"),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Background Grid selector toggle button
            IconButton(
                onClick = { showBackgroundPicker = !showBackgroundPicker },
                modifier = Modifier.testTag("background_grid_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Background Style",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded Background Style picker row
            if (showBackgroundPicker) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    items(CanvasBackgroundStyle.values()) { style ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (currentBackgroundStyle == style) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    onBackgroundStyleSelected(style)
                                    showBackgroundPicker = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = style.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentBackgroundStyle == style) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Page Navigation (Prev, Page X / Y, Next)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (currentPageIndex > 0) onPageSelected(currentPageIndex - 1) },
                        enabled = currentPageIndex > 0,
                        modifier = Modifier.testTag("prev_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (currentPageIndex > 0) MaterialTheme.colorScheme.onSurface else Color.LightGray
                        )
                    }

                    Text(
                        text = "Page ${currentPageIndex + 1} / $totalPages",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { if (currentPageIndex < totalPages - 1) onPageSelected(currentPageIndex + 1) },
                        enabled = currentPageIndex < totalPages - 1,
                        modifier = Modifier.testTag("next_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            tint = if (currentPageIndex < totalPages - 1) MaterialTheme.colorScheme.onSurface else Color.LightGray
                        )
                    }
                }
            }

            // Action Buttons: Add Page & Delete Page
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onAddPage,
                    modifier = Modifier.testTag("add_page_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Page",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (totalPages > 1) {
                    IconButton(
                        onClick = onDeletePage,
                        modifier = Modifier.testTag("delete_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Page",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
