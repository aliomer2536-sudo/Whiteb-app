package com.example.data.model

/**
 * Math subject category.
 */
enum class MathCategory(val label: String) {
    ARITHMETIC("Arithmetic & Pre-Algebra"),
    ALGEBRA("Algebra & Equations"),
    GEOMETRY("Geometry & Trigonometry"),
    CALCULUS("Calculus & Derivatives"),
    STATISTICS("Statistics & Probabilities"),
    PHYSICS_FORMULAS("Physics Laws")
}

/**
 * Educational Math Formula model for reference sheet.
 */
data class MathFormula(
    val id: String,
    val title: String,
    val category: MathCategory,
    val expression: String, // Plain text representation
    val latex: String,      // LaTeX string for math formatting
    val description: String,
    val example: String? = null
)

/**
 * Scientific Calculator Record.
 */
data class CalculatorHistoryItem(
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
