package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.model.ToolType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VirtualToolsOverlay(
    activeTool: ToolType,
    modifier: Modifier = Modifier
) {
    if (activeTool != ToolType.RULER && activeTool != ToolType.COMPASS && activeTool != ToolType.PROTRACTOR) return

    var rulerOffset by remember { mutableStateOf(Offset(200f, 400f)) }
    var rulerRotation by remember { mutableFloatStateOf(0f) }

    var compassCenter by remember { mutableStateOf(Offset(500f, 600f)) }
    var compassRadius by remember { mutableFloatStateOf(180f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(activeTool) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    if (activeTool == ToolType.RULER) {
                        rulerOffset += pan
                        rulerRotation += rotation
                    } else if (activeTool == ToolType.COMPASS) {
                        compassCenter += pan
                        compassRadius = (compassRadius * zoom).coerceIn(60f, 500f)
                    }
                }
            }
            .testTag("virtual_tools_overlay")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (activeTool) {
                ToolType.RULER -> {
                    // Draw Virtual Ruler
                    rotate(degrees = rulerRotation, pivot = rulerOffset) {
                        val rulerWidth = 600f
                        val rulerHeight = 100f
                        val rect = Rect(rulerOffset.x, rulerOffset.y, rulerOffset.x + rulerWidth, rulerOffset.y + rulerHeight)

                        // Body fill
                        drawRect(
                            color = Color(0xDDFFFBEB),
                            topLeft = rect.topLeft,
                            size = rect.size
                        )
                        // Border
                        drawRect(
                            color = Color(0xFFD97706),
                            topLeft = rect.topLeft,
                            size = rect.size,
                            style = Stroke(width = 3f)
                        )

                        // Centimeter tick marks
                        val cmStep = 40f
                        var cm = 0
                        var x = rulerOffset.x + 20f
                        while (x <= rulerOffset.x + rulerWidth - 20f) {
                            val markLen = if (cm % 5 == 0) 35f else 20f
                            drawLine(
                                color = Color(0xFF92400E),
                                start = Offset(x, rulerOffset.y),
                                end = Offset(x, rulerOffset.y + markLen),
                                strokeWidth = if (cm % 5 == 0) 3f else 1.5f
                            )
                            x += cmStep / 2f
                            cm++
                        }
                    }
                }
                ToolType.COMPASS -> {
                    // Draw Virtual Compass & Radius Circle Preview
                    drawCircle(
                        color = Color(0xFF2563EB),
                        center = compassCenter,
                        radius = compassRadius,
                        style = Stroke(width = 3f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                    )

                    // Compass legs
                    drawCircle(
                        color = Color(0xFFDC2626),
                        center = compassCenter,
                        radius = 8f
                    )

                    val needleOffset = Offset(
                        compassCenter.x + compassRadius * cos(0.0).toFloat(),
                        compassCenter.y + compassRadius * sin(0.0).toFloat()
                    )
                    drawLine(
                        color = Color(0xFF1E3A8A),
                        start = compassCenter,
                        end = needleOffset,
                        strokeWidth = 4f
                    )
                    drawCircle(
                        color = Color(0xFF1E3A8A),
                        center = needleOffset,
                        radius = 12f
                    )
                }
                ToolType.PROTRACTOR -> {
                    // Draw Semi-circle Protractor
                    val radius = 220f
                    val center = Offset(400f, 700f)

                    drawArc(
                        color = Color(0xCCEFF6FF),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    drawArc(
                        color = Color(0xFF2563EB),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3f)
                    )

                    // Degree ticks
                    for (deg in 0..180 step 10) {
                        val rad = Math.toRadians((180 - deg).toDouble())
                        val tickLen = if (deg % 30 == 0) 25f else 15f
                        val startP = Offset(
                            (center.x + (radius - tickLen) * cos(rad)).toFloat(),
                            (center.y - (radius - tickLen) * sin(rad)).toFloat()
                        )
                        val endP = Offset(
                            (center.x + radius * cos(rad)).toFloat(),
                            (center.y - radius * sin(rad)).toFloat()
                        )
                        drawLine(
                            color = Color(0xFF1E40AF),
                            start = startP,
                            end = endP,
                            strokeWidth = 2f
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
