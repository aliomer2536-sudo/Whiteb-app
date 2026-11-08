package com.example

import com.example.data.model.SerializedPoint
import com.example.data.model.ShapeType
import com.example.engine.HandwritingRecognitionEngine
import com.example.engine.MathEngine
import com.example.engine.RecognitionLanguage
import com.example.engine.ShapeRecognitionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhiteboardEnginesTest {

    @Test
    fun testStraightLineRecognition() {
        val linePoints = listOf(
            SerializedPoint(0f, 0f),
            SerializedPoint(50f, 0f),
            SerializedPoint(100f, 0f),
            SerializedPoint(150f, 0f),
            SerializedPoint(200f, 0f)
        )
        val result = ShapeRecognitionEngine.analyzeStroke(linePoints)
        assertNotNull(result)
        assertEquals(ShapeType.LINE, result?.shapeType)
    }

    @Test
    fun testCircleRecognition() {
        val circlePoints = mutableListOf<SerializedPoint>()
        val radius = 100f
        val centerX = 200f
        val centerY = 200f
        for (deg in 0..360 step 15) {
            val rad = Math.toRadians(deg.toDouble())
            val x = (centerX + radius * Math.cos(rad)).toFloat()
            val y = (centerY + radius * Math.sin(rad)).toFloat()
            circlePoints.add(SerializedPoint(x, y))
        }
        val result = ShapeRecognitionEngine.analyzeStroke(circlePoints)
        assertNotNull(result)
        assertEquals(ShapeType.CIRCLE, result?.shapeType)
    }

    @Test
    fun testMathCalculatorEvaluation() {
        assertEquals("7", MathEngine.evaluateExpression("3 + 4"))
        assertEquals("25", MathEngine.evaluateExpression("5 * 5"))
        assertEquals("5", MathEngine.evaluateExpression("sqrt(25)"))
    }

    @Test
    fun testGraphPointGeneration() {
        val points = MathEngine.generateGraphPoints("sin(x)", -5f, 5f, 1f)
        assertTrue(points.isNotEmpty())
    }

    @Test
    fun testHandwritingSomaliRecognition() {
        val strokes = listOf(
            com.example.data.model.DrawnStroke(
                points = listOf(
                    SerializedPoint(0f, 0f),
                    SerializedPoint(300f, 0f),
                    SerializedPoint(300f, 50f)
                )
            )
        )
        val result = HandwritingRecognitionEngine.recognizeStrokes(strokes, RecognitionLanguage.SOMALI)
        assertNotNull(result.recognizedText)
        assertTrue(result.recognizedText.isNotEmpty())
    }
}
