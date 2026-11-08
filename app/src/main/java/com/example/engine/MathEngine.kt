package com.example.engine

import com.example.data.model.MathCategory
import com.example.data.model.MathFormula
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * On-device Math Engine for equation formatting, scientific calculation, graph plotting,
 * and formula reference library.
 */
object MathEngine {

    /**
     * Built-in Educational Math Formula Library.
     */
    val FORMULA_LIBRARY: List<MathFormula> = listOf(
        MathFormula(
            id = "f_pythagoras",
            title = "Pythagorean Theorem",
            category = MathCategory.GEOMETRY,
            expression = "a² + b² = c²",
            latex = "a^2 + b^2 = c^2",
            description = "In a right-angled triangle, the square of the hypotenuse is equal to the sum of the squares of the other two sides.",
            example = "If a = 3, b = 4 -> c = √(3² + 4²) = 5"
        ),
        MathFormula(
            id = "f_quadratic",
            title = "Quadratic Formula",
            category = MathCategory.ALGEBRA,
            expression = "x = (-b ± √(b² - 4ac)) / (2a)",
            latex = "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
            description = "Solutions to quadratic equation ax² + bx + c = 0.",
            example = "x² - 5x + 6 = 0 -> x = 2 or x = 3"
        ),
        MathFormula(
            id = "f_circle_area",
            title = "Area of a Circle",
            category = MathCategory.GEOMETRY,
            expression = "Area = π * r²",
            latex = "A = \\pi r^2",
            description = "Area of a circle given radius r.",
            example = "Radius r = 7 -> Area ≈ 153.94"
        ),
        MathFormula(
            id = "f_triangle_area",
            title = "Area of a Triangle",
            category = MathCategory.GEOMETRY,
            expression = "Area = ½ * base * height",
            latex = "A = \\frac{1}{2} b h",
            description = "Standard formula for triangle area given base and perpendicular height.",
            example = "Base = 10, Height = 6 -> Area = 30"
        ),
        MathFormula(
            id = "f_trig_identity",
            title = "Pythagorean Trig Identity",
            category = MathCategory.GEOMETRY,
            expression = "sin²(θ) + cos²(θ) = 1",
            latex = "\\sin^2(\\theta) + \\cos^2(\\theta) = 1",
            description = "Fundamental identity relating sine and cosine."
        ),
        MathFormula(
            id = "f_derivative_power",
            title = "Power Rule for Derivatives",
            category = MathCategory.CALCULUS,
            expression = "d/dx(xⁿ) = n * xⁿ⁻¹",
            latex = "\\frac{d}{dx}(x^n) = n x^{n-1}",
            description = "Derivative of power function xⁿ with respect to x.",
            example = "d/dx(x³) = 3x²"
        ),
        MathFormula(
            id = "f_integral_power",
            title = "Power Rule for Integration",
            category = MathCategory.CALCULUS,
            expression = "∫ xⁿ dx = (xⁿ⁺¹)/(n+1) + C",
            latex = "\\int x^n dx = \\frac{x^{n+1}}{n+1} + C \\quad (n \\neq -1)",
            description = "Indefinite integral of power function."
        ),
        MathFormula(
            id = "f_mean",
            title = "Arithmetic Mean",
            category = MathCategory.STATISTICS,
            expression = "Mean = (∑ x) / n",
            latex = "\\bar{x} = \\frac{\\sum_{i=1}^n x_i}{n}",
            description = "Average value of a set of n observations."
        ),
        MathFormula(
            id = "f_newton2",
            title = "Newton's Second Law of Motion",
            category = MathCategory.PHYSICS_FORMULAS,
            expression = "F = m * a",
            latex = "F = m a",
            description = "Force equals mass times acceleration.",
            example = "m = 10kg, a = 9.8 m/s² -> F = 98 N"
        ),
        MathFormula(
            id = "f_einstein",
            title = "Mass-Energy Equivalence",
            category = MathCategory.PHYSICS_FORMULAS,
            expression = "E = m * c²",
            latex = "E = m c^2",
            description = "Einstein's relativistic energy formula."
        )
    )

    /**
     * Evaluates a mathematical expression string for scientific calculator.
     */
    fun evaluateExpression(expr: String): String {
        val clean = expr.trim()
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())

        if (clean.isEmpty()) return "0"

        return try {
            val result = parseSimpleExpression(clean)
            if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format("%.4f", result)
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    /**
     * Generates 2D plot points for a function string f(x).
     */
    fun generateGraphPoints(
        functionExpr: String,
        xMin: Float = -10f,
        xMax: Float = 10f,
        step: Float = 0.2f
    ): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        var x = xMin
        val cleanExpr = functionExpr.lowercase()
            .replace("f(x)=", "")
            .replace("y=", "")
            .trim()

        while (x <= xMax) {
            val y = evaluateFunctionAt(cleanExpr, x.toDouble()).toFloat()
            if (!y.isNaN() && !y.isInfinite() && abs(y) < 1000f) {
                points.add(Pair(x, y))
            }
            x += step
        }
        return points
    }

    private fun evaluateFunctionAt(expr: String, x: Double): Double {
        return try {
            when {
                expr.contains("sin") -> sin(x)
                expr.contains("cos") -> cos(x)
                expr.contains("tan") -> tan(x)
                expr.contains("sqrt") -> if (x >= 0) sqrt(x) else Double.NaN
                expr.contains("log") -> if (x > 0) log10(x) else Double.NaN
                expr.contains("ln") -> if (x > 0) ln(x) else Double.NaN
                expr.contains("x^2") -> x * x
                expr.contains("x^3") -> x * x * x
                expr.contains("2x") || expr.contains("2*x") -> 2 * x
                expr.contains("3x") || expr.contains("3*x") -> 3 * x
                expr == "x" -> x
                else -> parseSimpleExpression(expr.replace("x", x.toString()))
            }
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun parseSimpleExpression(expr: String): Double {
        var text = expr.replace(" ", "")
        
        // Handle trig/func prefixes
        if (text.startsWith("sin(") && text.endsWith(")")) {
            val valInside = parseSimpleExpression(text.substring(4, text.length - 1))
            return sin(Math.toRadians(valInside))
        }
        if (text.startsWith("cos(") && text.endsWith(")")) {
            val valInside = parseSimpleExpression(text.substring(4, text.length - 1))
            return cos(Math.toRadians(valInside))
        }
        if (text.startsWith("tan(") && text.endsWith(")")) {
            val valInside = parseSimpleExpression(text.substring(4, text.length - 1))
            return tan(Math.toRadians(valInside))
        }
        if (text.startsWith("sqrt(") && text.endsWith(")")) {
            val valInside = parseSimpleExpression(text.substring(5, text.length - 1))
            return sqrt(valInside)
        }

        // Basic arithmetic parser
        if (text.contains("+")) {
            val parts = text.split("+", limit = 2)
            return parseSimpleExpression(parts[0]) + parseSimpleExpression(parts[1])
        }
        if (text.contains("-") && !text.startsWith("-")) {
            val parts = text.split("-", limit = 2)
            return parseSimpleExpression(parts[0]) - parseSimpleExpression(parts[1])
        }
        if (text.contains("*")) {
            val parts = text.split("*", limit = 2)
            return parseSimpleExpression(parts[0]) * parseSimpleExpression(parts[1])
        }
        if (text.contains("/")) {
            val parts = text.split("/", limit = 2)
            val denom = parseSimpleExpression(parts[1])
            return if (denom != 0.0) parseSimpleExpression(parts[0]) / denom else Double.NaN
        }
        if (text.contains("^")) {
            val parts = text.split("^", limit = 2)
            return parseSimpleExpression(parts[0]).pow(parseSimpleExpression(parts[1]))
        }

        return text.toDoubleOrNull() ?: 0.0
    }
}
