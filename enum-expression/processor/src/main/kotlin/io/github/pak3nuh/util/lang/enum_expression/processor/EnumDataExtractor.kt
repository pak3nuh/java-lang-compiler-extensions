package io.github.pak3nuh.util.lang.enum_expression.processor

import com.squareup.javapoet.ClassName
import io.github.pak3nuh.util.lang.enum_expression.Expression
import io.github.pak3nuh.util.processor.ElementAnalyzer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class EnumDataExtractor(private val elementAnalyzer: ElementAnalyzer) {

    fun extractData(element: TypeElement): EnumData {
        val type = elementAnalyzer.type(element)

        val enumName = element.simpleName.toString()
        val pkg = type.packageName

        val constantNames = element.enclosedElements
                .filter { it.kind == ElementKind.ENUM_CONSTANT }
                .map { it.simpleName.toString() }
                .toSortedSet()
        val expressionName = getNameOverride(type) ?: "${enumName}Expression"

        return EnumData(pkg, enumName,  element.qualifiedName.toString(), expressionName, constantNames,
                getExpressionBuilder(type), getDefaultInterface(type))
    }

    private fun getDefaultInterface(type: ElementAnalyzer.Type): Boolean {
        return type.annotations()
                .filter { it.isA(Expression::class.java) }
                .flatMap { it.values() }
                .filter { it.name == Expression::defaultInterface.name }
                .map { it.value as Boolean }
                .first()
    }

    private fun getExpressionBuilder(type: ElementAnalyzer.Type): Boolean {
        return type.annotations()
                .filter { it.isA(Expression::class.java) }
                .flatMap { it.values() }
                .filter { it.name == Expression::expressionBuilder.name }
                .map { it.value as Boolean }
                .first()
    }

    private fun getNameOverride(type: ElementAnalyzer.Type): String? {
        return type.annotations()
                .filter { it.isA(Expression::class.java) }
                .flatMap { it.values() }
                .filter { it.name == Expression::value.name }
                .map { it.value as String }
                .filter { it.isNotEmpty() }
                .firstOrNull()
    }
}

data class EnumData(
        val pkg: String,
        val simpleName: String,
        val qualifiedName: String,
        val expressionName: String,
        val symbols: Set<String>,
        val expressionBuilder: Boolean,
        val defaultInterface: Boolean
) {
    val enumType: ClassName = ClassName.bestGuess(qualifiedName)
}
