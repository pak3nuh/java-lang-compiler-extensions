package io.github.pak3nuh.util.processor

import com.squareup.javapoet.TypeName
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass

class ElementAnalyzer(private val elements: Elements, private val types: Types) {

    fun type(typeElement: TypeElement) = Type(typeElement.asType() as DeclaredType, typeElement, null)

    fun type(kClass: KClass<*>) = type(kClass.java.asElement() as TypeElement)

    inner class Type internal constructor(
            val type: DeclaredType,
            val element: TypeElement,
            private val parent: Type?
    ) {

        fun name(): String = element.qualifiedName.toString()

        fun typeName(): String = element.simpleName.toString()

        fun typeVariables(): List<TypeVariable> {
            return element.typeParameters.map { TypeVariable(it, this) }
        }

        fun annotations(): Sequence<Annotation> =
                element.annotationMirrors.asSequence().map { Annotation(it) }

        fun methods(): List<Method> {
            val typeMap = element.typeParameters.zip(type.typeArguments)
            return element.enclosedElements.filterIsInstance<ExecutableElement>().map {
                Method(it, typeMap, this)
            }
        }

        fun interfaces(): List<Type> {
            return types.directSupertypes(type)
                    .filterIsInstance<DeclaredType>()
                    .filter { it.asElement().kind == ElementKind.INTERFACE }
                    .map { Type(it, it.asElement() as TypeElement, this) }
        }

        val packageName: String get() = elements.getPackageOf(element).toString()

        override fun toString(): String {
            return "Type($element)"
        }
    }

    inner class Method internal constructor(
            private val element: ExecutableElement,
            private val paramTypes: List<Pair<TypeParameterElement, TypeMirror>>,
            private val parent: Type
    ) {
        fun name(): String = element.simpleName.toString()

        fun parameters(): List<Parameter> {
            return element.parameters.map {
                Parameter(it, paramTypes)
            }
        }

        fun isAbstract(): Boolean {
            return element.modifiers.contains(Modifier.ABSTRACT)
        }

        fun returnType(): TypeMirror {
            return element.returnType
        }

        fun returnTypeReified(): TypeName {
                return TypeNameReifier(element.returnType, paramTypes).reify()
        }

        fun typeVariables(): List<TypeVariable> {
            return element.typeParameters.map { TypeVariable(it, null) }
        }

        fun isOverride(other: Method): Boolean {
            return elements.overrides(element, other.element, parent.element)
        }

        override fun toString(): String {
            return "Method($element)"
        }

        fun exceptions(): List<TypeMirror> {
            return element.thrownTypes
        }
    }

    inner class TypeVariable internal constructor(
            private val typeParameterElement: TypeParameterElement,
            private val parent: Type?
    ) {
        fun name(): String = typeParameterElement.simpleName.toString()

        val type: TypeMirror = typeParameterElement.asType()

        override fun toString(): String {
            return "TypeVariable($typeParameterElement)"
        }

    }

    inner class Parameter internal constructor(
            private val variableElement: VariableElement,
            val paramTypes: List<Pair<TypeParameterElement, TypeMirror>>
    ) {
        fun name(): String = variableElement.simpleName.toString()

        override fun toString(): String {
            return "Parameter($variableElement)"
        }

        fun reify(): TypeName {
            return TypeNameReifier(variableElement.asType(), paramTypes).reify()
        }

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

        override fun toString(): String {
            return "Annotation($annotation)"
        }
    }

    class AnnotationValue internal constructor(val entry: Map.Entry<ExecutableElement, javax.lang.model.element.AnnotationValue>) {
        val name: String get() = entry.key.simpleName.toString()
        val value: Any get() = entry.value.value
    }

    private fun Class<*>.asElement() = elements.getTypeElement(this.name)

}
