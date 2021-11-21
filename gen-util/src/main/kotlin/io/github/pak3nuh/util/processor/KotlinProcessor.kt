package io.github.pak3nuh.util.processor

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

abstract class KotlinProcessor(private val annotations: Set<KClass<out Annotation>>): AbstractProcessor() {

    protected lateinit var elementAnalyzer: ElementAnalyzer

    final override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementAnalyzer = ElementAnalyzer(processingEnv.elementUtils, processingEnv.typeUtils)
    }

    final override fun getSupportedAnnotationTypes(): Set<String> = annotations.mapTo(HashSet()) {it.qualifiedName!!}

    final override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val env = roundEnv!!

        return try {
            val byAnnotationList = this.annotations.map {
                Pair(it, env.getElementsAnnotatedWith(it.java))
            }
            kProcessRound(byAnnotationList, env).forEach {
                it.writeTo(processingEnv.filer)
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            logError("Error processing annotations: ${ex.message}")
            false
        }
    }

    abstract fun kProcessRound(byAnnotationList: List<Pair<KClass<out Annotation>, Set<Element>>>, roundEnv: RoundEnvironment): List<FileWriteable>

    final override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    protected fun logError(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

}