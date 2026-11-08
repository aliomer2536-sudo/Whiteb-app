package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.MathEngine

private val PRESET_FUNCTIONS = listOf(
    "f(x) = sin(x)",
    "f(x) = cos(x)",
    "f(x) = x^2 - 4",
    "f(x) = 2x + 1",
    "f(x) = sqrt(x)",
    "f(x) = x^3"
)

@Composable
fun GraphGeneratorDialog(
    onDismiss: () -> Unit,
    onInsertGraph: (functionExpr: String, points: List<Pair<Float, Float>>) -> Unit
) {
    var functionExpr by remember { mutableStateOf("f(x) = sin(x)") }
    val points = remember(functionExpr) { MathEngine.generateGraphPoints(functionExpr) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("graph_generator_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2D Math Graph Plotter", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_graph_dialog")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PRESET_FUNCTIONS) { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (functionExpr == preset) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { functionExpr = preset }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (functionExpr == preset) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = functionExpr,
                    onValueChange = { functionExpr = it },
                    label = { Text("Function Expression f(x)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("graph_function_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Graph Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        val w = size.width
                        val h = size.height
                        val originX = w / 2f
                        val originY = h / 2f

                        // Draw Coordinate Axes X and Y
                        drawLine(Color.DarkGray, Offset(0f, originY), Offset(w, originY), strokeWidth = 2f)
                        drawLine(Color.DarkGray, Offset(originX, 0f), Offset(originX, h), strokeWidth = 2f)

                        if (points.isNotEmpty()) {
                            val path = Path()
                            val scaleX = w / 24f
                            val scaleY = h / 24f

                            val p0 = points.first()
                            path.moveTo(originX + p0.first * scaleX, originY - p0.second * scaleY)

                            for (i in 1 until points.size) {
                                val pt = points[i]
                                path.lineTo(originX + pt.first * scaleX, originY - pt.second * scaleY)
                            }

                            drawPath(
                                path = path,
                                color = Color(0xFF38BDF8),
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onInsertGraph(functionExpr, points)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("insert_graph_to_canvas")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert Graph to Whiteboard")
                }
            }
        }
    }
}
