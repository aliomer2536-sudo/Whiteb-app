package com.example.engine

import com.example.data.model.DrawnStroke
import com.example.data.model.SerializedPoint
import com.example.data.model.ShapeType
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Geometric shape recognition result.
 */
data class RecognizedShapeResult(
    val shapeType: ShapeType,
    val boundsX: Float,
    val boundsY: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val label: String? = null
)

/**
 * On-device heuristic engine to recognize and correct hand-drawn geometric shapes,
 * arrows, lines, flowcharts, and tables.
 */
object ShapeRecognitionEngine {

    /**
     * Analyzes a drawn stroke and returns a corrected shape if confidence exceeds threshold.
     */
    fun analyzeStroke(points: List<SerializedPoint>): RecognizedShapeResult? {
        if (points.size < 4) return null

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        val width = max(maxX - minX, 10.0f)
        val height = max(maxY - minY, 10.0f)
        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f

        // Calculate total arc length
        var totalLength = 0f
        for (i in 0 until points.size - 1) {
            totalLength += hypot(points[i + 1].x - points[i].x, points[i + 1].y - points[i].y)
        }

        val start = points.first()
        val end = points.last()
        val startEndDist = hypot(end.x - start.x, end.y - start.y)

        // 1. Line check: start to end distance is almost equal to total length
        val linearity = if (totalLength > 0) startEndDist / totalLength else 0f
        if (linearity > 0.90f) {
            // Check if arrow head exists at the end
            val isArrow = isArrowPattern(points)
            val shape = if (isArrow) ShapeType.ARROW else ShapeType.LINE
            return RecognizedShapeResult(
                shapeType = shape,
                boundsX = minX,
                boundsY = minY,
                width = width,
                height = height,
                confidence = linearity
            )
        }

        // 2. Closed shape check: start and end are close together
        val isClosed = startEndDist < totalLength * 0.25f || startEndDist < 50f
        val corners = detectCorners(points)

        if (isClosed) {
            when (corners.size) {
                3 -> {
                    // Triangle
                    val isRight = isRightTriangle(corners)
                    val shape = if (isRight) ShapeType.RIGHT_TRIANGLE else ShapeType.TRIANGLE
                    return RecognizedShapeResult(
                        shapeType = shape,
                        boundsX = minX,
                        boundsY = minY,
                        width = width,
                        height = height,
                        confidence = 0.88f
                    )
                }
                4 -> {
                    // Rectangle or Square
                    val aspectRatio = width / height
                    val isSquare = aspectRatio in 0.85f..1.15f
                    val shape = if (isSquare) ShapeType.SQUARE else ShapeType.RECTANGLE
                    return RecognizedShapeResult(
                        shapeType = shape,
                        boundsX = minX,
                        boundsY = minY,
                        width = width,
                        height = height,
                        confidence = 0.90f
                    )
                }
                5 -> {
                    // Star or Pentagon
                    return RecognizedShapeResult(
                        shapeType = ShapeType.STAR,
                        boundsX = minX,
                        boundsY = minY,
                        width = width,
                        height = height,
                        confidence = 0.82f
                    )
                }
                else -> {
                    // Circle or Ellipse check
                    val isCircleOrEllipse = checkCircleOrEllipse(points, centerX, centerY, width, height)
                    if (isCircleOrEllipse) {
                        val aspectRatio = width / height
                        val isCircle = aspectRatio in 0.82f..1.18f
                        val shape = if (isCircle) ShapeType.CIRCLE else ShapeType.ELLIPSE
                        return RecognizedShapeResult(
                            shapeType = shape,
                            boundsX = minX,
                            boundsY = minY,
                            width = width,
                            height = height,
                            confidence = 0.89f
                        )
                    }
                }
            }
        }

        // 3. Arrow check (if unclosed line with tip)
        if (isArrowPattern(points)) {
            return RecognizedShapeResult(
                shapeType = ShapeType.ARROW,
                boundsX = minX,
                boundsY = minY,
                width = width,
                height = height,
                confidence = 0.85f
            )
        }

        return null
    }

    /**
     * Detects intersecting strokes to form a Table Grid (multiple horizontal and vertical lines).
     */
    fun detectTableGrid(strokes: List<DrawnStroke>): RecognizedShapeResult? {
        if (strokes.size < 4) return null

        val horizontalLines = mutableListOf<DrawnStroke>()
        val verticalLines = mutableListOf<DrawnStroke>()

        for (stroke in strokes) {
            val result = analyzeStroke(stroke.points)
            if (result?.shapeType == ShapeType.LINE) {
                if (result.width > result.height * 2.5f) {
                    horizontalLines.add(stroke)
                } else if (result.height > result.width * 2.5f) {
                    verticalLines.add(stroke)
                }
            }
        }

        if (horizontalLines.size >= 2 && verticalLines.size >= 2) {
            val allPoints = strokes.flatMap { it.points }
            val minX = allPoints.minOf { it.x }
            val maxX = allPoints.maxOf { it.x }
            val minY = allPoints.minOf { it.y }
            val maxY = allPoints.maxOf { it.y }

            val rows = max(horizontalLines.size - 1, 2)
            val cols = max(verticalLines.size - 1, 2)

            return RecognizedShapeResult(
                shapeType = ShapeType.TABLE_GRID,
                boundsX = minX,
                boundsY = minY,
                width = maxX - minX,
                height = maxY - minY,
                confidence = 0.92f,
                label = "$rows x $cols Grid"
            )
        }
        return null
    }

    private fun detectCorners(points: List<SerializedPoint>): List<SerializedPoint> {
        val corners = mutableListOf<SerializedPoint>()
        if (points.size < 6) return corners

        // Sample points to find sharp direction changes (angles)
        val step = max(points.size / 20, 2)
        for (i in step until points.size - step step step) {
            val pPrev = points[i - step]
            val pCurr = points[i]
            val pNext = points[i + step]

            val angle1 = atan2(pCurr.y - pPrev.y, pCurr.x - pPrev.x)
            val angle2 = atan2(pNext.y - pCurr.y, pNext.x - pCurr.x)

            var diff = abs(angle2 - angle1)
            if (diff > Math.PI) diff = (2 * Math.PI - diff).toFloat()

            // Sharp turn (between 45 deg and 135 deg)
            if (diff > 0.75f && diff < 2.3f) {
                if (corners.isEmpty() || hypot(pCurr.x - corners.last().x, pCurr.y - corners.last().y) > 30f) {
                    corners.add(pCurr)
                }
            }
        }
        return corners
    }

    private fun isRightTriangle(corners: List<SerializedPoint>): Boolean {
        if (corners.size != 3) return false
        val (a, b, c) = corners
        val ab2 = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
        val bc2 = (b.x - c.x) * (b.x - c.x) + (b.y - c.y) * (b.y - c.y)
        val ca2 = (c.x - a.x) * (c.x - a.x) + (c.y - a.y) * (c.y - a.y)

        val sides = listOf(ab2, bc2, ca2).sorted()
        return abs((sides[0] + sides[1]) - sides[2]) / sides[2] < 0.25f
    }

    private fun checkCircleOrEllipse(
        points: List<SerializedPoint>,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float
    ): Boolean {
        val radiusX = width / 2f
        val radiusY = height / 2f
        if (radiusX <= 0 || radiusY <= 0) return false

        var errorSum = 0f
        for (p in points) {
            val dx = (p.x - centerX) / radiusX
            val dy = (p.y - centerY) / radiusY
            val norm = sqrt(dx * dx + dy * dy)
            errorSum += abs(norm - 1.0f)
        }
        val avgError = errorSum / points.size
        return avgError < 0.28f
    }

    private fun isArrowPattern(points: List<SerializedPoint>): Boolean {
        if (points.size < 6) return false
        val end = points.last()
        val nearEnd = points[points.size - 4]

        val mainAngle = atan2(nearEnd.y - points.first().y, nearEnd.x - points.first().x)
        val tipAngle = atan2(end.y - nearEnd.y, end.x - nearEnd.x)

        var angleDiff = abs(tipAngle - mainAngle)
        if (angleDiff > Math.PI) angleDiff = (2 * Math.PI - angleDiff).toFloat()

        return angleDiff > 0.8f && angleDiff < 2.5f
    }
}
