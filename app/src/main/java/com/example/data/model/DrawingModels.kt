package com.example.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Represents a single point in a drawn stroke.
 */
data class SerializedPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toOffset(): Offset = Offset(x, y)
    companion object {
        fun fromOffset(offset: Offset, pressure: Float = 1.0f): SerializedPoint =
            SerializedPoint(offset.x, offset.y, pressure)
    }
}

/**
 * Tool types supported on the whiteboard canvas.
 */
enum class ToolType {
    PEN,
    CALLIGRAPHY,
    ARROW_PEN,
    COMPUTER_POINTER,
    HIGHLIGHTER,
    ERASER,
    SHAPE_AUTO,
    TEXT_RECOGNIZER,
    MATH_RECOGNIZER,
    RULER,
    COMPASS,
    PROTRACTOR,
    SELECT
}

/**
 * Background pattern for the whiteboard canvas.
 */
enum class CanvasBackgroundStyle(val label: String) {
    BLANK("Blank"),
    GRID("Math Grid"),
    LINES("Lined Paper"),
    DOTS("Dot Grid"),
    ISOMETRIC("Isometric"),
    CHALKBOARD("Chalkboard Green"),
    DARK_OLED("Dark OLED")
}

/**
 * Represents a single stroke drawn on the canvas.
 */
data class DrawnStroke(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<SerializedPoint>,
    val colorArgb: Int = Color.Black.toArgb(),
    val strokeWidth: Float = 4.0f,
    val isHighlighter: Boolean = false,
    val isEraser: Boolean = false,
    val isCalligraphy: Boolean = false,
    val isArrowPen: Boolean = false,
    val isCorrectedShape: Boolean = false,
    val shapeType: ShapeType? = null
)

/**
 * Recognized Geometric Shape Types.
 */
enum class ShapeType {
    LINE,
    CIRCLE,
    ELLIPSE,
    SQUARE,
    RECTANGLE,
    TRIANGLE,
    RIGHT_TRIANGLE,
    ARROW,
    STAR,
    TABLE_GRID,
    FLOWCHART_NODE
}

/**
 * Formatted element placed on canvas (Text, Shape, Math, Graph, Table, Diagram).
 */
sealed class CanvasElement {
    abstract val id: String
    abstract val x: Float
    abstract val y: Float
    abstract val isSelected: Boolean

    data class Text(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val x: Float,
        override val y: Float,
        val text: String,
        val fontSize: Float = 20.0f,
        val colorArgb: Int = Color.Black.toArgb(),
        val isHandwrittenConverted: Boolean = false,
        val originalStrokeIds: List<String> = emptyList(),
        override val isSelected: Boolean = false
    ) : CanvasElement()

    data class Shape(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val x: Float,
        override val y: Float,
        val shapeType: ShapeType,
        val width: Float,
        val height: Float,
        val strokeColorArgb: Int = Color.Black.toArgb(),
        val strokeWidth: Float = 4.0f,
        val fillColorArgb: Int = Color.Transparent.toArgb(),
        val label: String? = null,
        override val isSelected: Boolean = false
    ) : CanvasElement()

    data class MathEquation(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val x: Float,
        override val y: Float,
        val rawExpression: String,
        val formattedLaTeX: String,
        val solutionResult: String? = null,
        val fontSize: Float = 22.0f,
        val colorArgb: Int = Color.Black.toArgb(),
        override val isSelected: Boolean = false
    ) : CanvasElement()

    data class PlotGraph(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val x: Float,
        override val y: Float,
        val functionExpression: String, // e.g. "f(x) = sin(x)"
        val points: List<Pair<Float, Float>>,
        val width: Float = 300.0f,
        val height: Float = 200.0f,
        val lineColorArgb: Int = Color.Blue.toArgb(),
        override val isSelected: Boolean = false
    ) : CanvasElement()

    data class TableGrid(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val x: Float,
        override val y: Float,
        val rows: Int = 3,
        val cols: Int = 3,
        val cellWidth: Float = 100.0f,
        val cellHeight: Float = 50.0f,
        val data: List<List<String>> = List(3) { List(3) { "" } },
        override val isSelected: Boolean = false
    ) : CanvasElement()

    data class FlowchartNode(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val x: Float,
        override val y: Float,
        val nodeType: String = "PROCESS", // PROCESS, DECISION, START_END
        val text: String = "Process",
        val width: Float = 140.0f,
        val height: Float = 70.0f,
        val connectedToIds: List<String> = emptyList(),
        override val isSelected: Boolean = false
    ) : CanvasElement()
}
