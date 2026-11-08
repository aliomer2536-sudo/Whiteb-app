package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CanvasBackgroundStyle
import com.example.data.model.CanvasElement
import com.example.data.model.DrawnStroke
import com.example.data.model.ExportFormat
import com.example.data.model.SerializedPoint
import com.example.data.model.ShapeType
import com.example.data.model.ToolType
import com.example.engine.ExportEngine
import com.example.ui.components.ExportDialog
import com.example.ui.components.FloatingDrawingToolbar
import com.example.ui.components.GraphGeneratorDialog
import com.example.ui.components.PageManagerBar
import com.example.ui.components.ScientificCalculatorDialog
import com.example.ui.components.VirtualToolsOverlay
import com.example.ui.viewmodel.WhiteboardViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteboardScreen(
    viewModel: WhiteboardViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val pages by viewModel.pages.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    val selectedTool by viewModel.selectedTool.collectAsState()
    val currentColor by viewModel.currentColor.collectAsState()
    val strokeWidth by viewModel.strokeWidth.collectAsState()
    val arrowHeight by viewModel.arrowHeight.collectAsState()
    val isSmartShapeEnabled by viewModel.isSmartShapeEnabled.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    var showCalculatorDialog by remember { mutableStateOf(false) }
    var showGraphDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showTextLanguageDialog by remember { mutableStateOf(false) }

    // Real-time gesture path points for stroke being drawn
    val currentPoints = remember { mutableStateListOf<SerializedPoint>() }

    // Auto dismiss notification banner after 3 seconds
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(3000)
            viewModel.clearNotification()
        }
    }

    var activeCursorOffset by remember { mutableStateOf<Offset?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentPage?.title ?: "Whiteboard Canvas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_to_home_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("whiteboard_canvas_container")
        ) {
            // Interactive Drawing Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool, currentColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                activeCursorOffset = offset
                                currentPoints.clear()
                                if (selectedTool != ToolType.COMPUTER_POINTER) {
                                    currentPoints.add(SerializedPoint.fromOffset(offset))
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                activeCursorOffset = change.position
                                if (selectedTool != ToolType.COMPUTER_POINTER) {
                                    currentPoints.add(SerializedPoint.fromOffset(change.position))
                                }
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty() && selectedTool != ToolType.COMPUTER_POINTER) {
                                    val stroke = DrawnStroke(
                                        points = currentPoints.toList(),
                                        colorArgb = currentColor.toArgb(),
                                        strokeWidth = strokeWidth,
                                        isHighlighter = selectedTool == ToolType.HIGHLIGHTER,
                                        isEraser = selectedTool == ToolType.ERASER,
                                        isCalligraphy = selectedTool == ToolType.CALLIGRAPHY,
                                        isArrowPen = selectedTool == ToolType.ARROW_PEN
                                    )
                                    viewModel.addStroke(stroke)
                                    currentPoints.clear()
                                }
                            }
                        )
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 1. Draw Canvas Background Color & Grid Pattern
                val bgStyle = currentPage?.backgroundStyle ?: CanvasBackgroundStyle.BLANK
                val bgColor = when (bgStyle) {
                    CanvasBackgroundStyle.CHALKBOARD -> Color(0xFF1B4332)
                    CanvasBackgroundStyle.DARK_OLED -> Color(0xFF121212)
                    else -> Color.White
                }
                drawRect(color = bgColor, size = size)

                // Grid/Lined lines
                if (bgStyle == CanvasBackgroundStyle.GRID || bgStyle == CanvasBackgroundStyle.LINES) {
                    val lineColor = if (bgStyle == CanvasBackgroundStyle.CHALKBOARD || bgStyle == CanvasBackgroundStyle.DARK_OLED) {
                        Color.White.copy(alpha = 0.15f)
                    } else {
                        Color(0xFFE2E8F0)
                    }
                    val step = 60f
                    var y = step
                    while (y < canvasHeight) {
                        drawLine(lineColor, Offset(0f, y), Offset(canvasWidth, y), strokeWidth = 1.5f)
                        y += step
                    }
                    if (bgStyle == CanvasBackgroundStyle.GRID) {
                        var x = step
                        while (x < canvasWidth) {
                            drawLine(lineColor, Offset(x, 0f), Offset(x, canvasHeight), strokeWidth = 1.5f)
                            x += step
                        }
                    }
                }

                // 2. Render Saved Strokes
                currentPage?.strokes?.forEach { stroke ->
                    if (stroke.points.size >= 2) {
                        val path = createSmoothPath(stroke.points)
                        val strokeColor = Color(stroke.colorArgb).copy(alpha = if (stroke.isHighlighter) 0.4f else 1.0f)
                        val actualWidth = if (stroke.isCalligraphy) stroke.strokeWidth * 1.3f else stroke.strokeWidth

                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(
                                width = actualWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                join = androidx.compose.ui.graphics.StrokeJoin.Round
                            )
                        )

                        if (stroke.isArrowPen || stroke.shapeType == ShapeType.ARROW) {
                            drawArrowHead(this, stroke.points, strokeColor, actualWidth, arrowHeight)
                        }
                    }
                }

                // 3. Render AI Shape Elements
                currentPage?.shapeElements?.forEach { shape ->
                    val strokeColor = Color(shape.strokeColorArgb)
                    val rect = Rect(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height)

                    when (shape.shapeType) {
                        ShapeType.CIRCLE, ShapeType.ELLIPSE -> {
                            drawOval(
                                color = strokeColor,
                                topLeft = rect.topLeft,
                                size = rect.size,
                                style = Stroke(width = shape.strokeWidth)
                            )
                        }
                        ShapeType.SQUARE, ShapeType.RECTANGLE -> {
                            drawRect(
                                color = strokeColor,
                                topLeft = rect.topLeft,
                                size = rect.size,
                                style = Stroke(width = shape.strokeWidth)
                            )
                        }
                        ShapeType.TRIANGLE, ShapeType.RIGHT_TRIANGLE -> {
                            val path = Path().apply {
                                moveTo(rect.centerLeft.x + rect.width / 2f, rect.top)
                                lineTo(rect.left, rect.bottom)
                                lineTo(rect.right, rect.bottom)
                                close()
                            }
                            drawPath(path = path, color = strokeColor, style = Stroke(width = shape.strokeWidth))
                        }
                        ShapeType.ARROW -> {
                            drawLine(strokeColor, Offset(rect.left, rect.center.y), Offset(rect.right, rect.center.y), strokeWidth = shape.strokeWidth)
                            drawLine(strokeColor, Offset(rect.right, rect.center.y), Offset(rect.right - 25f, rect.center.y - 18f), strokeWidth = shape.strokeWidth)
                            drawLine(strokeColor, Offset(rect.right, rect.center.y), Offset(rect.right - 25f, rect.center.y + 18f), strokeWidth = shape.strokeWidth)
                        }
                        else -> {
                            drawRect(strokeColor, topLeft = rect.topLeft, size = rect.size, style = Stroke(width = shape.strokeWidth))
                        }
                    }
                }

                // 4. Render 2D Function Graphs
                currentPage?.graphElements?.forEach { graph ->
                    val originX = graph.x + graph.width / 2f
                    val originY = graph.y + graph.height / 2f

                    // Graph Container & Axis
                    drawRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(graph.x, graph.y),
                        size = Size(graph.width, graph.height)
                    )
                    drawLine(Color.Gray, Offset(graph.x, originY), Offset(graph.x + graph.width, originY), strokeWidth = 2f)
                    drawLine(Color.Gray, Offset(originX, graph.y), Offset(originX, graph.y + graph.height), strokeWidth = 2f)

                    if (graph.points.isNotEmpty()) {
                        val path = Path()
                        val scaleX = graph.width / 24f
                        val scaleY = graph.height / 24f
                        val p0 = graph.points.first()
                        path.moveTo(originX + p0.first * scaleX, originY - p0.second * scaleY)

                        for (i in 1 until graph.points.size) {
                            val pt = graph.points[i]
                            path.lineTo(originX + pt.first * scaleX, originY - pt.second * scaleY)
                        }
                        drawPath(path = path, color = Color(0xFF38BDF8), style = Stroke(width = 3.5f))
                    }
                }

                // 5. Draw Active Stroke Preview (Currently Drawing)
                if (currentPoints.size >= 2) {
                    val activePath = createSmoothPath(currentPoints)
                    val strokeColor = currentColor.copy(alpha = if (selectedTool == ToolType.HIGHLIGHTER) 0.4f else 1.0f)
                    val actualWidth = if (selectedTool == ToolType.CALLIGRAPHY) strokeWidth * 1.3f else strokeWidth

                    drawPath(
                        path = activePath,
                        color = strokeColor,
                        style = Stroke(
                            width = actualWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )

                    if (selectedTool == ToolType.ARROW_PEN) {
                        drawArrowHead(this, currentPoints, strokeColor, actualWidth, arrowHeight)
                    }
                }

                // 6. Draw Computer Mouse Arrow Cursor Pointer
                val cursorLoc = activeCursorOffset
                if (cursorLoc != null || selectedTool == ToolType.COMPUTER_POINTER) {
                    val pos = cursorLoc ?: Offset(canvasWidth / 2f, canvasHeight / 2f)
                    drawComputerArrowCursor(this, pos, currentColor)
                }
            }

            // 7. Render Text Elements Overlay (Handwriting Converted Text & Typed Text)
            currentPage?.textElements?.forEach { textElem ->
                Card(
                    modifier = Modifier
                        .offset(x = textElem.x.dp, y = textElem.y.dp)
                        .testTag("text_element_${textElem.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (textElem.isHandwrittenConverted) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Converted Text",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = textElem.text,
                            fontSize = textElem.fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(textElem.colorArgb)
                        )
                    }
                }
            }

            // 8. Render Math Equations Overlay
            currentPage?.mathElements?.forEach { mathElem ->
                Card(
                    modifier = Modifier
                        .offset(x = mathElem.x.dp, y = mathElem.y.dp)
                        .testTag("math_element_${mathElem.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Text(
                            text = mathElem.rawExpression,
                            fontSize = mathElem.fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        mathElem.solutionResult?.let { result ->
                            Text(
                                text = "= $result",
                                fontSize = (mathElem.fontSize * 0.9f).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Virtual Ruler, Compass & Protractor Tool Overlays
            VirtualToolsOverlay(activeTool = selectedTool)

            // Top Floating Drawing Toolbar
            FloatingDrawingToolbar(
                selectedTool = selectedTool,
                onToolSelected = { viewModel.setSelectedTool(it) },
                currentColor = currentColor,
                onColorSelected = { viewModel.setCurrentColor(it) },
                strokeWidth = strokeWidth,
                onStrokeWidthChanged = { viewModel.setStrokeWidth(it) },
                arrowHeight = arrowHeight,
                onArrowHeightChanged = { viewModel.setArrowHeight(it) },
                isSmartShapeEnabled = isSmartShapeEnabled,
                onSmartShapeToggled = { viewModel.toggleSmartShape(it) },
                canUndo = canUndo,
                onUndo = { viewModel.undo() },
                canRedo = canRedo,
                onRedo = { viewModel.redo() },
                onConvertHandwriting = { showTextLanguageDialog = true },
                onOpenCalculator = { showCalculatorDialog = true },
                onOpenGraphGenerator = { showGraphDialog = true },
                onExport = { showExportDialog = true },
                onClear = { viewModel.clearCanvas() },
                modifier = Modifier.align(Alignment.TopCenter)
            )



            // Notification Banner Toast
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp)
            ) {
                notificationMessage?.let { msg ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Bottom Page Manager Bar
            PageManagerBar(
                currentPageIndex = currentPageIndex,
                totalPages = pages.size.coerceAtLeast(1),
                onPageSelected = { viewModel.selectPage(it) },
                onAddPage = { viewModel.addPage() },
                onDeletePage = { viewModel.deleteCurrentPage() },
                currentBackgroundStyle = currentPage?.backgroundStyle ?: CanvasBackgroundStyle.BLANK,
                onBackgroundStyleSelected = { viewModel.setBackgroundStyle(it) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Modals
    if (showCalculatorDialog) {
        ScientificCalculatorDialog(
            onDismiss = { showCalculatorDialog = false },
            onInsertMathToCanvas = { expr, result ->
                viewModel.insertMathEquation(expr, result)
            },
            onInsertFormulaToCanvas = { formula ->
                viewModel.insertFormula(formula)
            }
        )
    }

    if (showGraphDialog) {
        GraphGeneratorDialog(
            onDismiss = { showGraphDialog = false },
            onInsertGraph = { functionExpr, points ->
                viewModel.insertGraph(functionExpr, points)
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportFormatSelected = { format ->
                val exportedFile = viewModel.exportCurrentPage(context, format)
                if (exportedFile != null) {
                    val shareIntent = ExportEngine.createShareIntent(context, exportedFile, format.mimeType)
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Whiteboard Note"))
                }
            }
        )
    }

    if (showTextLanguageDialog) {
        com.example.ui.components.TextLanguageDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = { viewModel.setSelectedLanguage(it) },
            onConvertHandwriting = { lang -> viewModel.convertHandwritingToText(lang) },
            onInsertCustomText = { text -> viewModel.addCustomText(text) },
            onDismiss = { showTextLanguageDialog = false }
        )
    }
}

private fun createSmoothPath(points: List<SerializedPoint>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    if (points.size == 1) {
        path.moveTo(points.first().x, points.first().y)
        return path
    }
    if (points.size == 2) {
        path.moveTo(points.first().x, points.first().y)
        path.lineTo(points.last().x, points.last().y)
        return path
    }

    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size - 1) {
        val midX = (points[i].x + points[i + 1].x) / 2f
        val midY = (points[i].y + points[i + 1].y) / 2f
        path.quadraticTo(points[i].x, points[i].y, midX, midY)
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}

private fun drawArrowHead(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    points: List<SerializedPoint>,
    color: Color,
    width: Float,
    headLen: Float = 28f
) {
    if (points.size < 2) return
    val pLast = points.last()
    val pPrev = points[kotlin.math.max(0, points.size - 4)]
    val angle = kotlin.math.atan2((pLast.y - pPrev.y).toDouble(), (pLast.x - pPrev.x).toDouble())

    val x1 = pLast.x - headLen * kotlin.math.cos(angle - kotlin.math.PI / 5).toFloat()
    val y1 = pLast.y - headLen * kotlin.math.sin(angle - kotlin.math.PI / 5).toFloat()

    val x2 = pLast.x - headLen * kotlin.math.cos(angle + kotlin.math.PI / 5).toFloat()
    val y2 = pLast.y - headLen * kotlin.math.sin(angle + kotlin.math.PI / 5).toFloat()

    drawScope.drawLine(color, Offset(pLast.x, pLast.y), Offset(x1, y1), strokeWidth = width)
    drawScope.drawLine(color, Offset(pLast.x, pLast.y), Offset(x2, y2), strokeWidth = width)
}

private fun drawComputerArrowCursor(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    position: Offset,
    cursorColor: Color = Color.White
) {
    val x = position.x
    val y = position.y

    val cursorPath = Path().apply {
        moveTo(x, y)
        lineTo(x, y + 42f)
        lineTo(x + 11f, y + 31f)
        lineTo(x + 21f, y + 48f)
        lineTo(x + 28f, y + 44f)
        lineTo(x + 18f, y + 27f)
        lineTo(x + 30f, y + 27f)
        close()
    }

    // Outer dark border/shadow
    drawScope.drawPath(
        path = cursorPath,
        color = Color(0xFF0F172A),
        style = Stroke(width = 4f)
    )
    // Inner fill
    drawScope.drawPath(
        path = cursorPath,
        color = Color.White
    )
    // Red indicator tip dot
    drawScope.drawCircle(
        color = Color(0xFFEF4444),
        center = Offset(x, y),
        radius = 4.5f
    )
}
