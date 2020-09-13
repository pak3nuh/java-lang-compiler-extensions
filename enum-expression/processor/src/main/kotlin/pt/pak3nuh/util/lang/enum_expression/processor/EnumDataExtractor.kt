package pt.pak3nuh.util.lang.enum_expression.processor

import com.squareup.javapoet.ClassName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

fun extractData(element: TypeElement): EnumData {
    val constantNames = element.enclosedElements
            .filter { it.kind == ElementKind.ENUM_CONSTANT }
            .map { it.simpleName.toString() }

    val lastDot = element.qualifiedName.lastIndexOf('.')
    val pkg = element.qualifiedName.substring(0, lastDot)
    val name = element.qualifiedName.substring(lastDot + 1)
    return EnumData(pkg, name, constantNames)
}

data class EnumData(val pkg: String, val name: String, val symbols: List<String>) {
    val enumType: ClassName = ClassName.bestGuess("$pkg.$name")
}
