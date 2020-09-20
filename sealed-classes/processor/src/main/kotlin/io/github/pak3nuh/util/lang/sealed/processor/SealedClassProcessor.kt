package io.github.pak3nuh.util.lang.sealed.processor

import io.github.pak3nuh.util.lang.sealed.SealedPackage
import io.github.pak3nuh.util.lang.sealed.SealedType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class SealedClassProcessor: AbstractProcessor() {

    lateinit var dataCollector: DataCollector
    lateinit var typeWriter: TypeWriter

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        dataCollector = DataCollector(processingEnv.elementUtils, processingEnv.typeUtils)
        typeWriter = TypeWriter(processingEnv.filer)
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if(annotations.isEmpty())
            return true

        return try {
            val roundData = dataCollector.round(
                    roundEnv.getElementsAnnotatedWith(SealedType::class.java),
                    roundEnv.getElementsAnnotatedWith(SealedPackage::class.java)
            )
            typeWriter.write(roundData)
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Error processing annotations: ${ex.message}")
            false
        }
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(SealedType::class.qualifiedName!!, SealedPackage::class.qualifiedName!!)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }
}
