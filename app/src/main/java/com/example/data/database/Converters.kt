package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.CanvasBackgroundStyle
import com.example.data.model.CanvasElement
import com.example.data.model.DrawnStroke
import com.example.data.model.NotebookCategory
import com.example.data.model.SerializedPoint
import com.example.data.model.ShapeType
import org.json.JSONArray
import org.json.JSONObject

class DatabaseConverters {

    @TypeConverter
    fun fromNotebookCategory(category: NotebookCategory): String = category.name

    @TypeConverter
    fun toNotebookCategory(value: String): NotebookCategory =
        try { NotebookCategory.valueOf(value) } catch (e: Exception) { NotebookCategory.GENERAL }

    @TypeConverter
    fun fromCanvasBackgroundStyle(style: CanvasBackgroundStyle): String = style.name

    @TypeConverter
    fun toCanvasBackgroundStyle(value: String): CanvasBackgroundStyle =
        try { CanvasBackgroundStyle.valueOf(value) } catch (e: Exception) { CanvasBackgroundStyle.BLANK }

    // --- Strokes List Conversion ---
    @TypeConverter
    fun fromStrokesList(strokes: List<DrawnStroke>?): String {
        if (strokes.isNullOrEmpty()) return "[]"
        return try {
            val array = JSONArray()
            for (stroke in strokes) {
                val obj = JSONObject()
                obj.put("id", stroke.id)
                obj.put("colorArgb", stroke.colorArgb)
                obj.put("strokeWidth", stroke.strokeWidth.toDouble())
                obj.put("isHighlighter", stroke.isHighlighter)
                obj.put("isEraser", stroke.isEraser)
                obj.put("isCalligraphy", stroke.isCalligraphy)
                obj.put("isArrowPen", stroke.isArrowPen)
                obj.put("isCorrectedShape", stroke.isCorrectedShape)
                stroke.shapeType?.let { obj.put("shapeType", it.name) }

                val ptsArray = JSONArray()
                for (p in stroke.points) {
                    val pObj = JSONObject()
                    pObj.put("x", p.x.toDouble())
                    pObj.put("y", p.y.toDouble())
                    pObj.put("pressure", p.pressure.toDouble())
                    pObj.put("timestamp", p.timestamp)
                    ptsArray.put(pObj)
                }
                obj.put("points", ptsArray)
                array.put(obj)
            }
            array.toString()
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toStrokesList(json: String?): List<DrawnStroke> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<DrawnStroke>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val colorArgb = obj.optInt("colorArgb", -16777216)
                val strokeWidth = obj.optDouble("strokeWidth", 4.0).toFloat()
                val isHighlighter = obj.optBoolean("isHighlighter", false)
                val isEraser = obj.optBoolean("isEraser", false)
                val isCalligraphy = obj.optBoolean("isCalligraphy", false)
                val isArrowPen = obj.optBoolean("isArrowPen", false)
                val isCorrectedShape = obj.optBoolean("isCorrectedShape", false)
                val shapeTypeStr = obj.optString("shapeType", "")
                val shapeType = if (shapeTypeStr.isNotEmpty()) try { ShapeType.valueOf(shapeTypeStr) } catch (e: Exception) { null } else null

                val ptsArray = obj.optJSONArray("points") ?: JSONArray()
                val pts = mutableListOf<SerializedPoint>()
                for (j in 0 until ptsArray.length()) {
                    val pObj = ptsArray.getJSONObject(j)
                    pts.add(
                        SerializedPoint(
                            x = pObj.optDouble("x", 0.0).toFloat(),
                            y = pObj.optDouble("y", 0.0).toFloat(),
                            pressure = pObj.optDouble("pressure", 1.0).toFloat(),
                            timestamp = pObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                list.add(
                    DrawnStroke(
                        id = id,
                        points = pts,
                        colorArgb = colorArgb,
                        strokeWidth = strokeWidth,
                        isHighlighter = isHighlighter,
                        isEraser = isEraser,
                        isCalligraphy = isCalligraphy,
                        isArrowPen = isArrowPen,
                        isCorrectedShape = isCorrectedShape,
                        shapeType = shapeType
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Text Elements ---
    @TypeConverter
    fun fromTextElementsList(list: List<CanvasElement.Text>?): String {
        if (list.isNullOrEmpty()) return "[]"
        return try {
            val array = JSONArray()
            for (t in list) {
                val obj = JSONObject()
                obj.put("id", t.id)
                obj.put("x", t.x.toDouble())
                obj.put("y", t.y.toDouble())
                obj.put("text", t.text)
                obj.put("fontSize", t.fontSize.toDouble())
                obj.put("colorArgb", t.colorArgb)
                obj.put("isHandwrittenConverted", t.isHandwrittenConverted)
                array.put(obj)
            }
            array.toString()
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toTextElementsList(json: String?): List<CanvasElement.Text> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            val res = mutableListOf<CanvasElement.Text>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                res.add(
                    CanvasElement.Text(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        text = obj.optString("text", ""),
                        fontSize = obj.optDouble("fontSize", 20.0).toFloat(),
                        colorArgb = obj.optInt("colorArgb", -16777216),
                        isHandwrittenConverted = obj.optBoolean("isHandwrittenConverted", false)
                    )
                )
            }
            res
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Shape Elements ---
    @TypeConverter
    fun fromShapeElementsList(list: List<CanvasElement.Shape>?): String {
        if (list.isNullOrEmpty()) return "[]"
        return try {
            val array = JSONArray()
            for (s in list) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("x", s.x.toDouble())
                obj.put("y", s.y.toDouble())
                obj.put("shapeType", s.shapeType.name)
                obj.put("width", s.width.toDouble())
                obj.put("height", s.height.toDouble())
                obj.put("strokeColorArgb", s.strokeColorArgb)
                obj.put("strokeWidth", s.strokeWidth.toDouble())
                obj.put("fillColorArgb", s.fillColorArgb)
                s.label?.let { obj.put("label", it) }
                array.put(obj)
            }
            array.toString()
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toShapeElementsList(json: String?): List<CanvasElement.Shape> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            val res = mutableListOf<CanvasElement.Shape>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val shapeTypeStr = obj.optString("shapeType", "RECTANGLE")
                val shapeType = try { ShapeType.valueOf(shapeTypeStr) } catch (e: Exception) { ShapeType.RECTANGLE }

                res.add(
                    CanvasElement.Shape(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        shapeType = shapeType,
                        width = obj.optDouble("width", 100.0).toFloat(),
                        height = obj.optDouble("height", 100.0).toFloat(),
                        strokeColorArgb = obj.optInt("strokeColorArgb", -16777216),
                        strokeWidth = obj.optDouble("strokeWidth", 4.0).toFloat(),
                        fillColorArgb = obj.optInt("fillColorArgb", 0),
                        label = if (obj.has("label")) obj.optString("label") else null
                    )
                )
            }
            res
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Math Equations ---
    @TypeConverter
    fun fromMathElementsList(list: List<CanvasElement.MathEquation>?): String {
        if (list.isNullOrEmpty()) return "[]"
        return try {
            val array = JSONArray()
            for (m in list) {
                val obj = JSONObject()
                obj.put("id", m.id)
                obj.put("x", m.x.toDouble())
                obj.put("y", m.y.toDouble())
                obj.put("rawExpression", m.rawExpression)
                obj.put("formattedLaTeX", m.formattedLaTeX)
                m.solutionResult?.let { obj.put("solutionResult", it) }
                obj.put("fontSize", m.fontSize.toDouble())
                obj.put("colorArgb", m.colorArgb)
                array.put(obj)
            }
            array.toString()
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toMathElementsList(json: String?): List<CanvasElement.MathEquation> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            val res = mutableListOf<CanvasElement.MathEquation>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                res.add(
                    CanvasElement.MathEquation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        rawExpression = obj.optString("rawExpression", ""),
                        formattedLaTeX = obj.optString("formattedLaTeX", ""),
                        solutionResult = if (obj.has("solutionResult")) obj.optString("solutionResult") else null,
                        fontSize = obj.optDouble("fontSize", 22.0).toFloat(),
                        colorArgb = obj.optInt("colorArgb", -16777216)
                    )
                )
            }
            res
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Plot Graphs ---
    @TypeConverter
    fun fromGraphElementsList(list: List<CanvasElement.PlotGraph>?): String {
        if (list.isNullOrEmpty()) return "[]"
        return try {
            val array = JSONArray()
            for (g in list) {
                val obj = JSONObject()
                obj.put("id", g.id)
                obj.put("x", g.x.toDouble())
                obj.put("y", g.y.toDouble())
                obj.put("functionExpression", g.functionExpression)
                obj.put("width", g.width.toDouble())
                obj.put("height", g.height.toDouble())

                val ptsArray = JSONArray()
                for (pt in g.points) {
                    val pObj = JSONObject()
                    pObj.put("first", pt.first.toDouble())
                    pObj.put("second", pt.second.toDouble())
                    ptsArray.put(pObj)
                }
                obj.put("points", ptsArray)
                array.put(obj)
            }
            array.toString()
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toGraphElementsList(json: String?): List<CanvasElement.PlotGraph> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val array = JSONArray(json)
            val res = mutableListOf<CanvasElement.PlotGraph>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val ptsArray = obj.optJSONArray("points") ?: JSONArray()
                val pts = mutableListOf<Pair<Float, Float>>()
                for (j in 0 until ptsArray.length()) {
                    val pObj = ptsArray.getJSONObject(j)
                    pts.add(Pair(pObj.optDouble("first", 0.0).toFloat(), pObj.optDouble("second", 0.0).toFloat()))
                }
                res.add(
                    CanvasElement.PlotGraph(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        functionExpression = obj.optString("functionExpression", ""),
                        points = pts,
                        width = obj.optDouble("width", 300.0).toFloat(),
                        height = obj.optDouble("height", 200.0).toFloat()
                    )
                )
            }
            res
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Tables ---
    @TypeConverter
    fun fromTableElementsList(list: List<CanvasElement.TableGrid>?): String = "[]"

    @TypeConverter
    fun toTableElementsList(json: String?): List<CanvasElement.TableGrid> = emptyList()

    // --- Flowcharts ---
    @TypeConverter
    fun fromFlowchartList(list: List<CanvasElement.FlowchartNode>?): String = "[]"

    @TypeConverter
    fun toFlowchartList(json: String?): List<CanvasElement.FlowchartNode> = emptyList()
}
