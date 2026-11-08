package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CanvasBackgroundStyle
import com.example.data.model.CanvasElement
import com.example.data.model.DrawnStroke
import com.example.data.model.Notebook
import com.example.data.model.NotebookCategory
import com.example.data.model.WhiteboardPage

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: NotebookCategory,
    val coverColorHex: String,
    val pageCount: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Notebook = Notebook(
        id = id,
        title = title,
        category = category,
        coverColorHex = coverColorHex,
        pageCount = pageCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(notebook: Notebook): NotebookEntity = NotebookEntity(
            id = notebook.id,
            title = notebook.title,
            category = notebook.category,
            coverColorHex = notebook.coverColorHex,
            pageCount = notebook.pageCount,
            createdAt = notebook.createdAt,
            updatedAt = notebook.updatedAt
        )
    }
}

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val notebookId: String,
    val pageIndex: Int,
    val title: String,
    val backgroundStyle: CanvasBackgroundStyle,
    val strokes: List<DrawnStroke>,
    val textElements: List<CanvasElement.Text>,
    val shapeElements: List<CanvasElement.Shape>,
    val mathElements: List<CanvasElement.MathEquation>,
    val graphElements: List<CanvasElement.PlotGraph>,
    val tableElements: List<CanvasElement.TableGrid>,
    val flowchartNodes: List<CanvasElement.FlowchartNode>,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): WhiteboardPage = WhiteboardPage(
        id = id,
        notebookId = notebookId,
        pageIndex = pageIndex,
        title = title,
        backgroundStyle = backgroundStyle,
        strokes = strokes,
        textElements = textElements,
        shapeElements = shapeElements,
        mathElements = mathElements,
        graphElements = graphElements,
        tableElements = tableElements,
        flowchartNodes = flowchartNodes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(page: WhiteboardPage): PageEntity = PageEntity(
            id = page.id,
            notebookId = page.notebookId,
            pageIndex = page.pageIndex,
            title = page.title,
            backgroundStyle = page.backgroundStyle,
            strokes = page.strokes,
            textElements = page.textElements,
            shapeElements = page.shapeElements,
            mathElements = page.mathElements,
            graphElements = page.graphElements,
            tableElements = page.tableElements,
            flowchartNodes = page.flowchartNodes,
            createdAt = page.createdAt,
            updatedAt = page.updatedAt
        )
    }
}
