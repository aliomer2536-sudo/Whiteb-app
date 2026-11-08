package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CanvasBackgroundStyle
import com.example.data.model.CanvasElement
import com.example.data.model.DrawnStroke
import com.example.data.model.ExportFormat
import com.example.data.model.MathFormula
import com.example.data.model.Notebook
import com.example.data.model.NotebookCategory
import com.example.data.model.ShapeType
import com.example.data.model.ToolType
import com.example.data.model.WhiteboardPage
import com.example.data.repository.WhiteboardRepository
import com.example.engine.ExportEngine
import com.example.engine.HandwritingRecognitionEngine
import com.example.engine.RecognitionLanguage
import com.example.engine.ShapeRecognitionEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Stack

class WhiteboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WhiteboardRepository(application)

    val notebooks: StateFlow<List<Notebook>> = repository.allNotebooks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _currentNotebookId = MutableStateFlow<String?>("default_math")
    val currentNotebookId: StateFlow<String?> = _currentNotebookId.asStateFlow()

    private val _pages = MutableStateFlow<List<WhiteboardPage>>(emptyList())
    val pages: StateFlow<List<WhiteboardPage>> = _pages.asStateFlow()

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    private val _selectedTool = MutableStateFlow(ToolType.PEN)
    val selectedTool: StateFlow<ToolType> = _selectedTool.asStateFlow()

    private val _currentColor = MutableStateFlow(Color.Black)
    val currentColor: StateFlow<Color> = _currentColor.asStateFlow()

    private val _strokeWidth = MutableStateFlow(6.0f)
    val strokeWidth: StateFlow<Float> = _strokeWidth.asStateFlow()

    private val _isSmartShapeEnabled = MutableStateFlow(true)
    val isSmartShapeEnabled: StateFlow<Boolean> = _isSmartShapeEnabled.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(RecognitionLanguage.SOMALI)
    val selectedLanguage: StateFlow<RecognitionLanguage> = _selectedLanguage.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    // Undo / Redo history stacks for the active whiteboard page
    private val undoStack = Stack<WhiteboardPage>()
    private val redoStack = Stack<WhiteboardPage>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _arrowHeight = MutableStateFlow(28f)
    val arrowHeight: StateFlow<Float> = _arrowHeight.asStateFlow()

    // Reactive current page state flow so Compose Canvas immediately re-renders when a stroke is drawn
    val currentPage: StateFlow<WhiteboardPage?> = combine(_pages, _currentPageIndex) { pageList, idx ->
        if (idx in pageList.indices) pageList[idx] else null
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    init {
        viewModelScope.launch {
            try {
                repository.initializeDefaultNotebooksIfEmpty()
                selectNotebook("default_math")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectNotebook(notebookId: String) {
        _currentNotebookId.value = notebookId
        viewModelScope.launch {
            repository.getPagesForNotebook(notebookId).collect { pageList ->
                _pages.value = pageList
                if (pageList.isNotEmpty() && _currentPageIndex.value >= pageList.size) {
                    _currentPageIndex.value = 0
                }
            }
        }
    }

    fun createNotebook(title: String, category: NotebookCategory, colorHex: String) {
        viewModelScope.launch {
            val created = repository.createNotebook(title, category, colorHex)
            selectNotebook(created.id)
            showNotification("Created new notebook '${created.title}'")
        }
    }

    fun selectPage(index: Int) {
        val pageList = _pages.value
        if (index in pageList.indices) {
            _currentPageIndex.value = index
            undoStack.clear()
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    fun getCurrentPage(): WhiteboardPage? {
        val pageList = _pages.value
        val idx = _currentPageIndex.value
        return if (idx in pageList.indices) pageList[idx] else null
    }

    fun setSelectedTool(tool: ToolType) {
        _selectedTool.value = tool
    }

    fun setCurrentColor(color: Color) {
        _currentColor.value = color
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth.value = width
    }

    fun setArrowHeight(height: Float) {
        _arrowHeight.value = height
        showNotification("Arrow Height set to ${height.toInt()}px")
    }

    fun toggleSmartShape(enabled: Boolean) {
        _isSmartShapeEnabled.value = enabled
        showNotification(if (enabled) "Smart AI Shape Assistant ON" else "Smart AI Shape Assistant OFF")
    }

    fun addStroke(stroke: DrawnStroke) {
        val currentPage = getCurrentPage() ?: return
        saveStateForUndo(currentPage)

        var newStrokes = currentPage.strokes.toMutableList()
        var newShapes = currentPage.shapeElements.toMutableList()

        if (stroke.isEraser) {
            // Eraser logic: remove strokes near eraser path
            val eraserPoints = stroke.points
            newStrokes = newStrokes.filterNot { s ->
                s.points.any { p1 -> eraserPoints.any { p2 -> Math.hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble()) < 30.0 } }
            }.toMutableList()
        } else if (_selectedTool.value == ToolType.SHAPE_AUTO && !stroke.isHighlighter) {
            val recognized = ShapeRecognitionEngine.analyzeStroke(stroke.points)
            if (recognized != null) {
                // Converted into clean geometric shape
                val shapeElem = CanvasElement.Shape(
                    x = recognized.boundsX,
                    y = recognized.boundsY,
                    shapeType = recognized.shapeType,
                    width = recognized.width,
                    height = recognized.height,
                    strokeColorArgb = stroke.colorArgb,
                    strokeWidth = stroke.strokeWidth
                )
                newShapes.add(shapeElem)
                showNotification("AI Corrected Shape: ${recognized.shapeType.name}")
            } else {
                newStrokes.add(stroke)
            }
        } else {
            // Standard Pen, Calligraphy, Arrow Pen, Highlighter: permanently keep stroke on canvas
            newStrokes.add(stroke)
        }

        val updatedPage = currentPage.copy(
            strokes = newStrokes,
            shapeElements = newShapes
        )
        updateCurrentPage(updatedPage)
    }

    fun setSelectedLanguage(language: RecognitionLanguage) {
        _selectedLanguage.value = language
        showNotification("Language set to ${language.displayName}")
    }

    fun convertHandwritingToText(language: RecognitionLanguage = _selectedLanguage.value) {
        val page = getCurrentPage() ?: return
        if (page.strokes.isEmpty()) {
            showNotification("No handwriting detected on canvas!")
            return
        }

        saveStateForUndo(page)
        val result = HandwritingRecognitionEngine.recognizeStrokes(page.strokes, language)

        val textElem = CanvasElement.Text(
            x = result.boundsX,
            y = result.boundsY,
            text = result.recognizedText,
            colorArgb = _currentColor.value.toArgb(),
            isHandwrittenConverted = true
        )

        val updatedPage = page.copy(
            textElements = page.textElements + textElem
        )
        updateCurrentPage(updatedPage)
        showNotification("Handwriting converted (${language.displayName}): '${result.recognizedText}'")
    }

    fun addCustomText(text: String, x: Float = 120f, y: Float = 200f) {
        val page = getCurrentPage() ?: return
        if (text.isBlank()) return
        saveStateForUndo(page)

        val textElem = CanvasElement.Text(
            x = x,
            y = y + (page.textElements.size * 50f),
            text = text,
            colorArgb = _currentColor.value.toArgb(),
            isHandwrittenConverted = false
        )

        val updatedPage = page.copy(
            textElements = page.textElements + textElem
        )
        updateCurrentPage(updatedPage)
        showNotification("Added text: '$text'")
    }

    fun insertMathEquation(expression: String, result: String) {
        val page = getCurrentPage() ?: return
        saveStateForUndo(page)

        val mathElem = CanvasElement.MathEquation(
            x = 100f,
            y = 200f + (page.mathElements.size * 80f),
            rawExpression = expression,
            formattedLaTeX = expression,
            solutionResult = result,
            colorArgb = _currentColor.value.toArgb()
        )

        val updatedPage = page.copy(
            mathElements = page.mathElements + mathElem
        )
        updateCurrentPage(updatedPage)
        showNotification("Inserted equation: $expression = $result")
    }

    fun insertFormula(formula: MathFormula) {
        val page = getCurrentPage() ?: return
        saveStateForUndo(page)

        val mathElem = CanvasElement.MathEquation(
            x = 80f,
            y = 150f + (page.mathElements.size * 90f),
            rawExpression = formula.title + ": " + formula.expression,
            formattedLaTeX = formula.latex,
            colorArgb = _currentColor.value.toArgb()
        )

        val updatedPage = page.copy(
            mathElements = page.mathElements + mathElem
        )
        updateCurrentPage(updatedPage)
        showNotification("Inserted formula: ${formula.title}")
    }

    fun insertGraph(functionExpr: String, points: List<Pair<Float, Float>>) {
        val page = getCurrentPage() ?: return
        saveStateForUndo(page)

        val graphElem = CanvasElement.PlotGraph(
            x = 100f,
            y = 250f,
            functionExpression = functionExpr,
            points = points
        )

        val updatedPage = page.copy(
            graphElements = page.graphElements + graphElem
        )
        updateCurrentPage(updatedPage)
        showNotification("Inserted graph for $functionExpr")
    }

    fun setBackgroundStyle(style: CanvasBackgroundStyle) {
        val page = getCurrentPage() ?: return
        saveStateForUndo(page)
        updateCurrentPage(page.copy(backgroundStyle = style))
    }

    fun addPage() {
        val notebookId = _currentNotebookId.value ?: return
        viewModelScope.launch {
            val newPage = repository.createNewPage(notebookId, CanvasBackgroundStyle.BLANK)
            val updatedList = repository.getPagesForNotebook(notebookId)
            _currentPageIndex.value = _pages.value.size
            showNotification("Added Page ${newPage.pageIndex + 1}")
        }
    }

    fun deleteCurrentPage() {
        val page = getCurrentPage() ?: return
        viewModelScope.launch {
            repository.deletePage(page)
            if (_currentPageIndex.value > 0) {
                _currentPageIndex.value--
            }
            showNotification("Deleted page")
        }
    }

    fun clearCanvas() {
        val page = getCurrentPage() ?: return
        saveStateForUndo(page)
        val clearedPage = page.copy(
            strokes = emptyList(),
            textElements = emptyList(),
            shapeElements = emptyList(),
            mathElements = emptyList(),
            graphElements = emptyList()
        )
        updateCurrentPage(clearedPage)
        showNotification("Whiteboard cleared")
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentPage = getCurrentPage() ?: return
            redoStack.push(currentPage)
            val previousState = undoStack.pop()
            updateCurrentPageDirect(previousState)
            updateUndoRedoStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentPage = getCurrentPage() ?: return
            undoStack.push(currentPage)
            val nextState = redoStack.pop()
            updateCurrentPageDirect(nextState)
            updateUndoRedoStates()
        }
    }

    fun exportCurrentPage(context: Context, format: ExportFormat): File? {
        val page = getCurrentPage() ?: return null
        val exportedFile = ExportEngine.exportPage(context, page, format)
        if (exportedFile != null) {
            showNotification("Exported ${format.extension.uppercase()} successfully")
        }
        return exportedFile
    }

    private fun saveStateForUndo(page: WhiteboardPage) {
        undoStack.push(page)
        redoStack.clear()
        updateUndoRedoStates()
    }

    private fun updateCurrentPage(page: WhiteboardPage) {
        updateCurrentPageDirect(page)
        viewModelScope.launch {
            repository.savePage(page)
        }
    }

    private fun updateCurrentPageDirect(page: WhiteboardPage) {
        val list = _pages.value.toMutableList()
        val idx = _currentPageIndex.value
        if (idx in list.indices) {
            list[idx] = page
            _pages.value = list.toList()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun showNotification(msg: String) {
        _notificationMessage.value = msg
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
