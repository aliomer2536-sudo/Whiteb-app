package com.example.data.model

import java.util.UUID

/**
 * Category types for organizing educational notebooks.
 */
enum class NotebookCategory(val displayName: String, val iconName: String) {
    MATH("Mathematics", "functions"),
    GEOMETRY("Geometry & Graphs", "square_foot"),
    PHYSICS("Physics & Science", "science"),
    SOMALI("Somali Language", "translate"),
    ENGLISH("English Language", "spellcheck"),
    GENERAL("General Notes", "edit_note")
}

/**
 * Represents a Whiteboard Notebook lesson file.
 */
data class Notebook(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: NotebookCategory = NotebookCategory.GENERAL,
    val coverColorHex: String = "#1E88E5",
    val pageCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents a Single Page within a Notebook.
 */
data class WhiteboardPage(
    val id: String = UUID.randomUUID().toString(),
    val notebookId: String,
    val pageIndex: Int,
    val title: String = "Page ${pageIndex + 1}",
    val backgroundStyle: CanvasBackgroundStyle = CanvasBackgroundStyle.BLANK,
    val strokes: List<DrawnStroke> = emptyList(),
    val textElements: List<CanvasElement.Text> = emptyList(),
    val shapeElements: List<CanvasElement.Shape> = emptyList(),
    val mathElements: List<CanvasElement.MathEquation> = emptyList(),
    val graphElements: List<CanvasElement.PlotGraph> = emptyList(),
    val tableElements: List<CanvasElement.TableGrid> = emptyList(),
    val flowchartNodes: List<CanvasElement.FlowchartNode> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Format options for exporting whiteboard notes.
 */
enum class ExportFormat(val extension: String, val mimeType: String) {
    PDF("pdf", "application/pdf"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    TEXT("txt", "text/plain")
}
