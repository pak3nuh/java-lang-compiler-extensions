package io.github.pak3nuh.util.lang.enum_expression.processor

import com.squareup.javapoet.ClassName
import io.github.pak3nuh.util.lang.enum_expression.Expression
import io.github.pak3nuh.util.processor.ElementAnalyzer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class EnumDataExtractor(private val elementAnalyzer: ElementAnalyzer) {

    fun extractData(element: TypeElement): EnumData {
        val type = elementAnalyzer.type(element)
        val nameOverride = type.annotations()
                .filter { it.isA(Expression::class.java) }
                .flatMap { it.values() }
                .filter { it.name == Expression::value.name }
                .map { it.value as String }
                .filter { it.isNotEmpty() }
                .firstOrNull()

        val enumName = element.simpleName.toString()
        val pkg = type.packageName

        val constantNames = element.enclosedElements
                .filter { it.kind == ElementKind.ENUM_CONSTANT }
                .map { it.simpleName.toString() }
        val expressionName = nameOverride ?: "${enumName}Expression"

        return EnumData(pkg, enumName,  element.qualifiedName.toString(), expressionName, constantNames)
    }
}

data class EnumData(
        val pkg: String,
        val simpleName: String,
        val qualifiedName: String,
        val expressionName: String,
        val symbols: List<String>
) {
    val enumType: ClassName = ClassName.bestGuess(qualifiedName)
}
