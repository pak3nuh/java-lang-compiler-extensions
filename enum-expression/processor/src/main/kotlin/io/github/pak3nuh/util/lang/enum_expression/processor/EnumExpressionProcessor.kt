package io.github.pak3nuh.util.lang.enum_expression.processor

import io.github.pak3nuh.util.lang.enum_expression.Expression
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class EnumExpressionProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty())
            return true

        val writer = ExpressionWriter(processingEnv.filer)
        val expressionAnnotation = annotations.first()
        return try {
            roundEnv.getElementsAnnotatedWith(expressionAnnotation)
                    .asSequence()
                    .filterIsInstance(TypeElement::class.java)
                    .onEach {
                        require(it.kind == ElementKind.ENUM) {
                            "Can't create expressions on element ${it.qualifiedName}"
                        }
                    }
                    .map { extractData(it) }
                    .forEach { writer.write(it) }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Error processing annotations: ${ex.message}")
            false
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8 // default interface methods
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Expression::class.qualifiedName!!)
    }
}
