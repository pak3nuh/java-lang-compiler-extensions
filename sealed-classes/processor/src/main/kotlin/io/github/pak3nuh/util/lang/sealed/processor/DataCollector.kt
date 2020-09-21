package io.github.pak3nuh.util.lang.sealed.processor

import io.github.pak3nuh.util.lang.sealed.SealedPackage
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class DataCollector(val elementUtils: Elements, val typeUtils: Types) {

    val sealedAnnotation = elementUtils.getTypeElement(SealedPackage::class.qualifiedName).asType()

    fun round(sealedTypes: Set<Element>, sealedPackages: Set<Element>): RoundData {

        val list = sealedTypes.asSequence()
                .filterIsInstance(TypeElement::class.java)
                .onEach {
                    check(it.kind == ElementKind.CLASS) { "SealedType can only be applied to classes." }
                }.map {
                    Step1(typeUtils.asElement(it.superclass) as TypeElement, it)
                }.onEach(Step1::validate)
                .toList()

        val groupBySupertype: Map<TypeElement, List<Step1>> = list.groupBy(Step1::superType)
        val hierarchies = groupBySupertype.map {
            SealedHierarchy(it.key, it.value.map(Step1::type))
        }

        val sealedPackages = sealedPackages
                .filterIsInstance<PackageElement>()
                .associateBy { it.qualifiedName.toString() }
                .mapValues { entry ->
                    elementUtils.getAllAnnotationMirrors(entry.value)
                            .filter {
                                typeUtils.isSameType(it.annotationType, sealedAnnotation)
                            }.map { mirror ->
                                elementUtils.getElementValuesWithDefaults(mirror)
                                        .filterKeys { it.simpleName.contentEquals(SealedPackage::value.name) }
                                        .values.first()
                            }.map {
                                it.value as Boolean
                            }.first()
                }.mapTo(HashSet()) { Package(it.key, it.value) }

        return RoundData(hierarchies, sealedPackages)
    }

    private inner class Step1(val superType: TypeElement, val type: TypeElement) {
        fun validate() {
            val isSupertypeClass = superType.kind == ElementKind.CLASS
            val isSupertypeAbstract = superType.modifiers.contains(Modifier.ABSTRACT)
            check(isSupertypeClass && isSupertypeAbstract) {
                "Supertype of element $type must be an abstract class"
            }
            validateSupertypeConstructor() //only package private constructors means same package for all classes
        }

        private fun validateSupertypeConstructor() {
            val constructors = superType.enclosedElements.asSequence()
                    .filterIsInstance<ExecutableElement>()
                    .filter { it.kind == ElementKind.CONSTRUCTOR }
                    .toList()
            check(constructors.isNotEmpty()) { "Type $superType must declare all constructors" }

            val invalidConstructors = constructors
                    .flatMap { const -> const.modifiers }
                    .filter { it == Modifier.PUBLIC || it == Modifier.PROTECTED }
            check(invalidConstructors.isEmpty()) {
                "$superType can't have public or protected constructors"
            }
        }
    }

}

data class SealedHierarchy(val rootType: TypeElement, val children: List<TypeElement>)
data class Package(val packageName: String, val sealed: Boolean)
data class RoundData(val hierarchies: List<SealedHierarchy>, val packages: Set<Package>)
