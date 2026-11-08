package com.example.data.repository

import android.content.Context
import com.example.data.database.NotebookEntity
import com.example.data.database.PageEntity
import com.example.data.database.WhiteboardDatabase
import com.example.data.model.CanvasBackgroundStyle
import com.example.data.model.CanvasElement
import com.example.data.model.DrawnStroke
import com.example.data.model.Notebook
import com.example.data.model.NotebookCategory
import com.example.data.model.WhiteboardPage
import com.example.engine.MathEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WhiteboardRepository(context: Context) {

    private val db = WhiteboardDatabase.getDatabase(context)
    private val notebookDao = db.notebookDao()
    private val pageDao = db.pageDao()

    val allNotebooks: Flow<List<Notebook>> = notebookDao.getAllNotebooks().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun initializeDefaultNotebooksIfEmpty() = withContext(Dispatchers.IO) {
        val existing = notebookDao.getNotebookById("default_math")
        if (existing == null) {
            val mathNotebook = Notebook(
                id = "default_math",
                title = "Mathematics & Algebra",
                category = NotebookCategory.MATH,
                coverColorHex = "#2563EB",
                pageCount = 2
            )
            notebookDao.insertNotebook(NotebookEntity.fromDomain(mathNotebook))

            // Page 1: Math Formulas Demo
            val mathPage1 = WhiteboardPage(
                id = "math_page_1",
                notebookId = "default_math",
                pageIndex = 0,
                title = "Pythagorean Theorem",
                backgroundStyle = CanvasBackgroundStyle.GRID,
                mathElements = listOf(
                    CanvasElement.MathEquation(
                        x = 100f,
                        y = 120f,
                        rawExpression = "a² + b² = c²",
                        formattedLaTeX = "a^2 + b^2 = c^2",
                        solutionResult = "c = √(3² + 4²) = 5"
                    ),
                    CanvasElement.MathEquation(
                        x = 100f,
                        y = 220f,
                        rawExpression = "Area = ½ * b * h",
                        formattedLaTeX = "A = \\frac{1}{2} b h",
                        solutionResult = "30 cm²"
                    )
                ),
                graphElements = listOf(
                    CanvasElement.PlotGraph(
                        x = 100f,
                        y = 350f,
                        functionExpression = "f(x) = sin(x)",
                        points = MathEngine.generateGraphPoints("sin(x)")
                    )
                )
            )

            // Page 2: Geometry Diagram Demo
            val mathPage2 = WhiteboardPage(
                id = "math_page_2",
                notebookId = "default_math",
                pageIndex = 1,
                title = "Geometric Shapes & Angles",
                backgroundStyle = CanvasBackgroundStyle.DOTS,
                shapeElements = listOf(
                    CanvasElement.Shape(
                        x = 80f,
                        y = 150f,
                        shapeType = com.example.data.model.ShapeType.TRIANGLE,
                        width = 240f,
                        height = 180f,
                        label = "Right Triangle ABC"
                    ),
                    CanvasElement.Shape(
                        x = 380f,
                        y = 150f,
                        shapeType = com.example.data.model.ShapeType.CIRCLE,
                        width = 200f,
                        height = 200f,
                        label = "Radius r = 7"
                    )
                )
            )

            pageDao.insertPages(listOf(PageEntity.fromDomain(mathPage1), PageEntity.fromDomain(mathPage2)))

            // Create Somali & General Notebooks
            val somaliNotebook = Notebook(
                id = "default_somali",
                title = "Darsiga Xisaabta & Aqoonta",
                category = NotebookCategory.SOMALI,
                coverColorHex = "#059669",
                pageCount = 1
            )
            notebookDao.insertNotebook(NotebookEntity.fromDomain(somaliNotebook))

            val somaliPage = WhiteboardPage(
                id = "somali_page_1",
                notebookId = "default_somali",
                pageIndex = 0,
                title = "Barashada Xisaabta",
                backgroundStyle = CanvasBackgroundStyle.LINES,
                textElements = listOf(
                    CanvasElement.Text(
                        x = 80f,
                        y = 120f,
                        text = "Barashada Xisaabta & Qaababka Geometriyada",
                        fontSize = 24f,
                        isHandwrittenConverted = true
                    )
                )
            )
            pageDao.insertPage(PageEntity.fromDomain(somaliPage))
        }
    }

    fun getPagesForNotebook(notebookId: String): Flow<List<WhiteboardPage>> =
        pageDao.getPagesForNotebook(notebookId).map { list -> list.map { it.toDomain() } }

    suspend fun getNotebook(id: String): Notebook? = withContext(Dispatchers.IO) {
        notebookDao.getNotebookById(id)?.toDomain()
    }

    suspend fun createNotebook(title: String, category: NotebookCategory, coverColorHex: String): Notebook =
        withContext(Dispatchers.IO) {
            val notebook = Notebook(
                title = title,
                category = category,
                coverColorHex = coverColorHex,
                pageCount = 1
            )
            notebookDao.insertNotebook(NotebookEntity.fromDomain(notebook))

            val firstPage = WhiteboardPage(
                notebookId = notebook.id,
                pageIndex = 0,
                title = "Page 1"
            )
            pageDao.insertPage(PageEntity.fromDomain(firstPage))
            notebook
        }

    suspend fun savePage(page: WhiteboardPage) = withContext(Dispatchers.IO) {
        val updatedPage = page.copy(updatedAt = System.currentTimeMillis())
        pageDao.insertPage(PageEntity.fromDomain(updatedPage))

        // Update notebook page count and timestamp
        val pages = pageDao.getPagesListForNotebook(page.notebookId)
        val notebook = notebookDao.getNotebookById(page.notebookId)
        if (notebook != null) {
            notebookDao.updateNotebook(
                notebook.copy(
                    pageCount = pages.size,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun createNewPage(notebookId: String, backgroundStyle: CanvasBackgroundStyle): WhiteboardPage =
        withContext(Dispatchers.IO) {
            val existingPages = pageDao.getPagesListForNotebook(notebookId)
            val newIndex = existingPages.size
            val newPage = WhiteboardPage(
                notebookId = notebookId,
                pageIndex = newIndex,
                title = "Page ${newIndex + 1}",
                backgroundStyle = backgroundStyle
            )
            pageDao.insertPage(PageEntity.fromDomain(newPage))

            val notebook = notebookDao.getNotebookById(notebookId)
            if (notebook != null) {
                notebookDao.updateNotebook(notebook.copy(pageCount = newIndex + 1, updatedAt = System.currentTimeMillis()))
            }
            newPage
        }

    suspend fun deletePage(page: WhiteboardPage) = withContext(Dispatchers.IO) {
        pageDao.deletePage(PageEntity.fromDomain(page))
        val remaining = pageDao.getPagesListForNotebook(page.notebookId)
        val notebook = notebookDao.getNotebookById(page.notebookId)
        if (notebook != null) {
            notebookDao.updateNotebook(notebook.copy(pageCount = remaining.size, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteNotebook(notebookId: String) = withContext(Dispatchers.IO) {
        pageDao.deletePagesForNotebook(notebookId)
        notebookDao.deleteNotebookById(notebookId)
    }
}
