package com.example.engine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.CanvasBackgroundStyle
import com.example.data.model.ExportFormat
import com.example.data.model.ShapeType
import com.example.data.model.WhiteboardPage
import java.io.File
import java.io.FileOutputStream

/**
 * On-device Export Engine for generating PDF, PNG, JPG files and sharing/printing notes.
 */
object ExportEngine {

    /**
     * Renders a WhiteboardPage to an Android Bitmap.
     */
    fun renderPageToBitmap(
        page: WhiteboardPage,
        width: Int = 1080,
        height: Int = 1920
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background
        val bgPaint = Paint().apply {
            color = when (page.backgroundStyle) {
                CanvasBackgroundStyle.CHALKBOARD -> AndroidColor.parseColor("#1B4332")
                CanvasBackgroundStyle.DARK_OLED -> AndroidColor.parseColor("#121212")
                else -> AndroidColor.WHITE
            }
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw grid lines if needed
        if (page.backgroundStyle == CanvasBackgroundStyle.GRID || page.backgroundStyle == CanvasBackgroundStyle.LINES) {
            val gridPaint = Paint().apply {
                color = AndroidColor.parseColor("#33888888")
                strokeWidth = 1.5f
                style = Paint.Style.STROKE
            }
            val step = 60f
            var y = step
            while (y < height) {
                canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
                y += step
            }
            if (page.backgroundStyle == CanvasBackgroundStyle.GRID) {
                var x = step
                while (x < width) {
                    canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
                    x += step
                }
            }
        }

        // Draw Strokes
        for (stroke in page.strokes) {
            if (stroke.points.size < 2) continue
            val strokePaint = Paint().apply {
                color = stroke.colorArgb
                strokeWidth = stroke.strokeWidth
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
                if (stroke.isHighlighter) {
                    alpha = 100
                }
            }

            val path = Path()
            path.moveTo(stroke.points.first().x, stroke.points.first().y)
            for (i in 1 until stroke.points.size) {
                path.lineTo(stroke.points[i].x, stroke.points[i].y)
            }
            canvas.drawPath(path, strokePaint)
        }

        // Draw Shape Elements
        for (shape in page.shapeElements) {
            val shapePaint = Paint().apply {
                color = shape.strokeColorArgb
                strokeWidth = shape.strokeWidth
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            val fillPaint = Paint().apply {
                color = shape.fillColorArgb
                style = Paint.Style.FILL
            }

            val rect = RectF(shape.x, shape.y, shape.x + shape.width, shape.y + shape.height)
            if (shape.fillColorArgb != AndroidColor.TRANSPARENT) {
                canvas.drawRect(rect, fillPaint)
            }

            when (shape.shapeType) {
                ShapeType.CIRCLE, ShapeType.ELLIPSE -> canvas.drawOval(rect, shapePaint)
                ShapeType.SQUARE, ShapeType.RECTANGLE -> canvas.drawRect(rect, shapePaint)
                ShapeType.TRIANGLE -> {
                    val path = Path().apply {
                        moveTo(rect.centerX(), rect.top)
                        lineTo(rect.left, rect.bottom)
                        lineTo(rect.right, rect.bottom)
                        close()
                    }
                    canvas.drawPath(path, shapePaint)
                }
                ShapeType.ARROW -> {
                    canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), shapePaint)
                    canvas.drawLine(rect.right, rect.centerY(), rect.right - 20f, rect.centerY() - 15f, shapePaint)
                    canvas.drawLine(rect.right, rect.centerY(), rect.right - 20f, rect.centerY() + 15f, shapePaint)
                }
                else -> canvas.drawRect(rect, shapePaint)
            }
        }

        // Draw Text Elements
        for (textElem in page.textElements) {
            val textPaint = Paint().apply {
                color = textElem.colorArgb
                textSize = textElem.fontSize * 1.5f
                isAntiAlias = true
            }
            canvas.drawText(textElem.text, textElem.x, textElem.y + textElem.fontSize, textPaint)
        }

        // Draw Math Equations
        for (mathElem in page.mathElements) {
            val mathPaint = Paint().apply {
                color = mathElem.colorArgb
                textSize = mathElem.fontSize * 1.5f
                isAntiAlias = true
            }
            val textToDraw = mathElem.solutionResult?.let { "${mathElem.rawExpression} = $it" } ?: mathElem.rawExpression
            canvas.drawText(textToDraw, mathElem.x, mathElem.y + mathElem.fontSize, mathPaint)
        }

        return bitmap
    }

    /**
     * Exports a page or notebook to PDF/PNG/JPG/TEXT file.
     */
    fun exportPage(
        context: Context,
        page: WhiteboardPage,
        format: ExportFormat
    ): File? {
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "${page.title.replace(" ", "_")}_${System.currentTimeMillis()}.${format.extension}"
        val outputFile = File(outputDir, fileName)

        return try {
            when (format) {
                ExportFormat.PNG -> {
                    val bitmap = renderPageToBitmap(page)
                    FileOutputStream(outputFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    outputFile
                }
                ExportFormat.JPG -> {
                    val bitmap = renderPageToBitmap(page)
                    FileOutputStream(outputFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    outputFile
                }
                ExportFormat.PDF -> {
                    val pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(1080, 1920, 1).create()
                    val pdfPage = pdfDocument.startPage(pageInfo)
                    val bitmap = renderPageToBitmap(page, 1080, 1920)
                    pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(pdfPage)

                    FileOutputStream(outputFile).use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    outputFile
                }
                ExportFormat.TEXT -> {
                    val sb = StringBuilder()
                    sb.appendLine("=== ${page.title} ===")
                    sb.appendLine("Date: ${java.util.Date(page.updatedAt)}")
                    sb.appendLine()
                    sb.appendLine("--- TEXT NOTES ---")
                    page.textElements.forEach { sb.appendLine("• ${it.text}") }
                    sb.appendLine()
                    sb.appendLine("--- MATH EQUATIONS ---")
                    page.mathElements.forEach { sb.appendLine("• ${it.rawExpression} ${it.solutionResult?.let { r -> "= $r" } ?: ""}") }
                    
                    outputFile.writeText(sb.toString())
                    outputFile
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates an Intent to share exported note file.
     */
    fun createShareIntent(context: Context, file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
