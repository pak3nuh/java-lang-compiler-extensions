package io.github.pak3nuh.util.processor

import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass

class ElementAnalyzer(private val elements: Elements, private val types: Types) {

    fun type(typeElement: TypeElement) = Type(typeElement)

    fun type(kClass: KClass<*>) = Type(kClass.java.asElement())

    inner class Type internal constructor(private val element: TypeElement) {

        fun name(): String = element.qualifiedName.toString()

        fun typeName(): String = element.simpleName.toString()

        fun typeVariables(): List<TypeVariable> {
            return element.typeParameters.map { TypeVariable(it) }
        }

        fun annotations(): Sequence<Annotation> =
                element.annotationMirrors.asSequence().map { Annotation(it) }

        fun methods(): List<Method> {
            return element.enclosedElements.filterIsInstance<ExecutableElement>().map {
                Method(it)
            }
        }

        fun interfaces(): List<Type> {
            return element.interfaces.map { Type(types.asElement(it) as TypeElement) }
        }

        val packageName: String get() = elements.getPackageOf(element).toString()
    }

    inner class Method internal constructor(private val element: ExecutableElement) {
        fun name(): String = element.simpleName.toString()

        fun parameters(): List<Parameter> {
            return element.parameters.map { Parameter(it) }
        }

        fun isAbstract(): Boolean {
            return element.modifiers.contains(Modifier.ABSTRACT)
        }

        fun returnType(): TypeMirror {
            return element.returnType
        }

        fun typeVariables(): List<TypeVariable> {
            return element.typeParameters.map { TypeVariable(it) }
        }
    }

    inner class TypeVariable internal constructor(private val typeParameterElement: TypeParameterElement) {
        fun name() : String = typeParameterElement.simpleName.toString()
        fun type() : TypeMirror = typeParameterElement.asType()
    }

    inner class Parameter internal constructor(private val variableElement: VariableElement) {
        fun name(): String = variableElement.simpleName.toString()

        fun type(): TypeMirror = variableElement.asType()
    }

    inner class Annotation internal constructor(private val annotation: AnnotationMirror) {
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
