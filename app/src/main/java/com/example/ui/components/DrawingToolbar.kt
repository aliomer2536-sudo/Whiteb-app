package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolType

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.Straighten

private val PRESET_COLORS = listOf(
    Color.Black,
    Color(0xFF2563EB), // Blue
    Color(0xFFDC2626), // Red
    Color(0xFF16A34A), // Green
    Color(0xFFCA8A04), // Yellow
    Color(0xFF9333EA), // Purple
    Color(0xFF0D9488), // Teal
    Color(0xFFEA580C), // Orange
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFF84CC16), // Lime
    Color.White
)

@Composable
fun FloatingDrawingToolbar(
    selectedTool: ToolType,
    onToolSelected: (ToolType) -> Unit,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChanged: (Float) -> Unit,
    arrowHeight: Float,
    onArrowHeightChanged: (Float) -> Unit,
    isSmartShapeEnabled: Boolean,
    onSmartShapeToggled: (Boolean) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canRedo: Boolean,
    onRedo: () -> Unit,
    onConvertHandwriting: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenGraphGenerator: () -> Unit,
    onExport: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPalette by remember { mutableStateOf(false) }
    var showWidthSlider by remember { mutableStateOf(false) }
    var showArrowHeightPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .fillMaxWidth()
            .testTag("drawing_toolbar"),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: Primary Drawing Tools & Dedicated Quick Icons (Colors, Size, Arrow Height)
            // Uses horizontalScroll so on ANY phone screen size, nothing is clipped or hidden!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. DEDICATED COLORS ICON BUTTON (Midabada)
                ToolButton(
                    icon = Icons.Default.Palette,
                    description = "Dooro Midab (Colors)",
                    isSelected = showColorPalette,
                    onClick = {
                        showColorPalette = !showColorPalette
                        if (showColorPalette) {
                            showWidthSlider = false
                            showArrowHeightPicker = false
                        }
                    },
                    testTag = "colors_palette_icon_button"
                )

                // Active Color indicator dot
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable {
                            showColorPalette = !showColorPalette
                            if (showColorPalette) {
                                showWidthSlider = false
                                showArrowHeightPicker = false
                            }
                        }
                        .testTag("color_indicator_preview")
                )

                Spacer(modifier = Modifier.width(2.dp))

                // 2. DEDICATED PEN SIZE ICON BUTTON (Cabbirka Qalinka)
                ToolButton(
                    icon = Icons.Default.LineWeight,
                    description = "Pen Size (${strokeWidth.toInt()}dp)",
                    isSelected = showWidthSlider,
                    onClick = {
                        showWidthSlider = !showWidthSlider
                        if (showWidthSlider) {
                            showColorPalette = false
                            showArrowHeightPicker = false
                        }
                    },
                    testTag = "pen_size_icon_button"
                )

                // 3. DEDICATED ARROW HEIGHT ICON BUTTON (Dhererka Falka)
                ToolButton(
                    icon = Icons.Default.Height,
                    description = "Arrow Height (${arrowHeight.toInt()}px)",
                    isSelected = showArrowHeightPicker,
                    onClick = {
                        showArrowHeightPicker = !showArrowHeightPicker
                        if (showArrowHeightPicker) {
                            showColorPalette = false
                            showWidthSlider = false
                        }
                    },
                    testTag = "arrow_height_icon_button"
                )

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Smooth Pen Tool
                ToolButton(
                    icon = Icons.Default.Create,
                    description = "Smooth Pen",
                    isSelected = selectedTool == ToolType.PEN,
                    onClick = { onToolSelected(ToolType.PEN) },
                    testTag = "pen_tool_button"
                )

                // Calligraphy Pen (Far Qurux Badan)
                ToolButton(
                    icon = Icons.Default.Brush,
                    description = "Calligraphy Pen (Far Qurux Badan)",
                    isSelected = selectedTool == ToolType.CALLIGRAPHY,
                    onClick = { onToolSelected(ToolType.CALLIGRAPHY) },
                    testTag = "calligraphy_tool_button"
                )

                // Arrow Pen Tool
                ToolButton(
                    icon = Icons.Default.Polyline,
                    description = "Arrow Line Pen",
                    isSelected = selectedTool == ToolType.ARROW_PEN,
                    onClick = { onToolSelected(ToolType.ARROW_PEN) },
                    testTag = "arrow_pen_tool_button"
                )

                // Computer Mouse Arrow Pointer Tool
                ToolButton(
                    icon = Icons.Default.Navigation,
                    description = "Computer Arrow Pointer (Mouse Arrow)",
                    isSelected = selectedTool == ToolType.COMPUTER_POINTER,
                    onClick = { onToolSelected(ToolType.COMPUTER_POINTER) },
                    testTag = "computer_pointer_tool_button"
                )

                // Highlighter
                ToolButton(
                    icon = Icons.Default.Highlight,
                    description = "Highlighter",
                    isSelected = selectedTool == ToolType.HIGHLIGHTER,
                    onClick = { onToolSelected(ToolType.HIGHLIGHTER) },
                    testTag = "highlighter_tool_button"
                )

                // Eraser
                ToolButton(
                    icon = Icons.Default.Clear,
                    description = "Eraser",
                    isSelected = selectedTool == ToolType.ERASER,
                    onClick = { onToolSelected(ToolType.ERASER) },
                    testTag = "eraser_tool_button"
                )

                // Shape Recognizer
                ToolButton(
                    icon = Icons.Default.Category,
                    description = "Shape Recognizer",
                    isSelected = selectedTool == ToolType.SHAPE_AUTO,
                    onClick = {
                        onToolSelected(ToolType.SHAPE_AUTO)
                        onSmartShapeToggled(true)
                    },
                    testTag = "shape_recognizer_tool_button"
                )

                // Smart Shape AI Assistant Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSmartShapeEnabled) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Shape Assistant",
                        tint = if (isSmartShapeEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSmartShapeEnabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Switch(
                        checked = isSmartShapeEnabled,
                        onCheckedChange = onSmartShapeToggled,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("smart_shape_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Undo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp).testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) MaterialTheme.colorScheme.onSurface else Color.LightGray
                    )
                }

                // Redo
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp).testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) MaterialTheme.colorScheme.onSurface else Color.LightGray
                    )
                }
            }

            // Expanded Color Palette (Opens when Colors Icon is clicked)
            AnimatedVisibility(
                visible = showColorPalette,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dooro Midabka Qalinka (Colors)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PRESET_COLORS.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (currentColor == color) 3.dp else 1.dp,
                                        color = if (currentColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onColorSelected(color)
                                        showColorPalette = false
                                    }
                            )
                        }
                    }
                }
            }

            // Expanded Stroke Thickness Slider & Size Presets (Opens when Pen Size Icon is clicked)
            AnimatedVisibility(
                visible = showWidthSlider,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dooro Cabbirka Qalinka (Pen Size)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        listOf(
                            2f to "Fine (2dp)",
                            6f to "Medium (6dp)",
                            12f to "Bold (12dp)",
                            20f to "Thick (20dp)",
                            32f to "Marker (32dp)"
                        ).forEach { (sizeVal, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (strokeWidth == sizeVal) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onStrokeWidthChanged(sizeVal) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (strokeWidth == sizeVal) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fine", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Slider(
                            value = strokeWidth,
                            onValueChange = onStrokeWidthChanged,
                            valueRange = 2f..36f,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${strokeWidth.toInt()}dp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expanded Arrow Height Selector (Opens when Arrow Height Icon is clicked)
            AnimatedVisibility(
                visible = showArrowHeightPicker,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Dooro Dhererka/Cabbirka Madaxa Falka (Arrow Height / Head Size)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        listOf(
                            20f to "Yar (20px)",
                            28f to "Dhexdhexaad (28px)",
                            40f to "Wayn (40px)",
                            56f to "Aad u Wayn (56px)"
                        ).forEach { (hVal, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (arrowHeight == hVal) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        onArrowHeightChanged(hVal)
                                        showArrowHeightPicker = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (arrowHeight == hVal) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Row 2: Secondary Educational Tools (Handwriting to Text, Math, Ruler, Graph, Export, Clear)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Convert Handwriting
                ToolButton(
                    icon = Icons.Default.TextFields,
                    description = "Handwriting to Text",
                    isSelected = false,
                    onClick = onConvertHandwriting,
                    testTag = "convert_text_button"
                )

                // Scientific Calculator
                ToolButton(
                    icon = Icons.Default.Calculate,
                    description = "Math Calculator",
                    isSelected = false,
                    onClick = onOpenCalculator,
                    testTag = "calculator_button"
                )

                // Function Graph Plotter
                ToolButton(
                    icon = Icons.Default.ShowChart,
                    description = "Graph Plotter",
                    isSelected = false,
                    onClick = onOpenGraphGenerator,
                    testTag = "graph_button"
                )

                // Ruler Tool Toggle
                ToolButton(
                    icon = Icons.Default.Rule,
                    description = "Virtual Ruler",
                    isSelected = selectedTool == ToolType.RULER,
                    onClick = {
                        val newTool = if (selectedTool == ToolType.RULER) ToolType.PEN else ToolType.RULER
                        onToolSelected(newTool)
                    },
                    testTag = "ruler_button"
                )

                // Compass Geometry Tool Toggle
                ToolButton(
                    icon = Icons.Default.Polyline,
                    description = "Compass Tool",
                    isSelected = selectedTool == ToolType.COMPASS,
                    onClick = {
                        val newTool = if (selectedTool == ToolType.COMPASS) ToolType.PEN else ToolType.COMPASS
                        onToolSelected(newTool)
                    },
                    testTag = "compass_button"
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Clear Canvas
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.testTag("clear_canvas_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Canvas",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // Export Notes
                IconButton(
                    onClick = onExport,
                    modifier = Modifier.testTag("export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Notes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .testTag(testTag)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
