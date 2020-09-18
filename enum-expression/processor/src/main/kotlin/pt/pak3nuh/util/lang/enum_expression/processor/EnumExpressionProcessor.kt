package pt.pak3nuh.util.lang.enum_expression.processor

import pt.pak3nuh.util.lang.enum_expression.Expression
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class EnumExpressionProcessor : AbstractProcessor() {

    lateinit var filer: Filer

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty())
            return true

        val writer = ExpressionWriter(filer)
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
            throw ex
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8 // default interface methods
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Expression::class.qualifiedName!!)
    }
}
