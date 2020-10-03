package io.github.pak3nuh.util.processor

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class ElementAnalyzer(private val elements: Elements, private val types: Types) {

    fun type(typeElement: TypeElement) = Type(typeElement)

    inner class Type internal constructor(val element: TypeElement) {
        fun annotations(): Sequence<Annotation> =
                element.annotationMirrors.asSequence().map { Annotation(it) }

        val packageName: String get() = elements.getPackageOf(element).toString()
    }

    inner class Annotation internal constructor(val annotation: AnnotationMirror) {
        fun isA(clazz: Class<out kotlin.Annotation>): Boolean =
                types.isSameType(annotation.annotationType, clazz.asElement().asType())

        fun values(defaults: Boolean = true): Sequence<AnnotationValue> {
            return if (defaults) {
                elements.getElementValuesWithDefaults(annotation).asSequence().map { AnnotationValue(it) }
            } else {
                annotation.elementValues.asSequence().map { AnnotationValue(it) }
            }
        }
    }

    class AnnotationValue internal constructor(val entry: Map.Entry<ExecutableElement, javax.lang.model.element.AnnotationValue>) {
        val name: String get() = entry.key.simpleName.toString()
        val value: Any get() = entry.value.value
    }
    
    private fun Class<*>.asElement() = elements.getTypeElement(this.name)

}
