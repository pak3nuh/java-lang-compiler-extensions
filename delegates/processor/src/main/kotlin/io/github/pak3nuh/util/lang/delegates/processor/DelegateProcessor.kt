package io.github.pak3nuh.util.lang.delegates.processor

import com.squareup.javapoet.*
import io.github.pak3nuh.util.lang.delegates.Delegate
import io.github.pak3nuh.util.processor.ElementAnalyzer
import io.github.pak3nuh.util.processor.FileWriteable
import io.github.pak3nuh.util.processor.KotlinProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import kotlin.reflect.KClass

class DelegateProcessor : KotlinProcessor(setOf(Delegate::class)) {

    override fun kProcessRound(byAnnotationList: List<Pair<KClass<out Annotation>, Set<Element>>>, roundEnv: RoundEnvironment): List<FileWriteable> {
        return byAnnotationList.flatMap { pair ->
            // Only types are annotated
            pair.second.filterIsInstance(TypeElement::class.java).map {
                require(it.kind == ElementKind.INTERFACE) { "Only interfaces are allowed for delegate generation" }
                val sourceType = elementAnalyzer.type(it)
                DelegateWriter(sourceType, elementAnalyzer)
            }
        }
    }

}

private class DelegateWriter(val sourceType: ElementAnalyzer.Type, val elementAnalyzer: ElementAnalyzer) : FileWriteable {
    override fun writeTo(filer: Filer) {
        val newClassName = "${sourceType.typeName()}Delegate"
        val sourceName = ClassName.bestGuess(sourceType.name())
        // todo bounds on delegate interface not supported
        val sourceTypeVariables = sourceType.typeVariables().map { TypeVariableName.get(it.name()) }
        val parameterizedDelegateType = buildDelegateType(sourceName, sourceTypeVariables)
        val delegateInterface = TypeSpec.interfaceBuilder(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Generated delegate. DO NOT MODIFY MANUALLY!")
                .addAnnotation(FunctionalInterface::class.java)
                .addSuperinterface(parameterizedDelegateType)
                .addTypeVariables(sourceTypeVariables)
                .addMethod(buildDelegateMethod(parameterizedDelegateType))
                .addMethods(buildMethodImplementations(getAbstractMethods(sourceType)))
                .build()
        JavaFile.builder(sourceName.packageName(), delegateInterface).build().writeTo(filer)
    }

    private fun getAbstractMethods(type: ElementAnalyzer.Type): List<ElementAnalyzer.Method> {
        fun getRecursive(type: ElementAnalyzer.Type): List<ElementAnalyzer.Method> {
            return type.methods() + type.interfaces().flatMap { getRecursive(it) }
        }

        fun List<ElementAnalyzer.Method>.filterOverrides(): List<ElementAnalyzer.Method> {
            data class Unique(val method: ElementAnalyzer.Method) {
                override fun equals(other: Any?): Boolean {
                    return other is Unique &&
                            (other.method.isOverride(method) || method.isOverride(other.method))
                }

                override fun hashCode(): Int {
                    return method.name().hashCode()
                }
            }
            return this.mapTo(HashSet()) { Unique(it) }.map { it.method }
        }

        // Object methods have implementations which we can't default on interfaces
        fun List<ElementAnalyzer.Method>.filterObjectMethods(): List<ElementAnalyzer.Method> {
            val objectType = elementAnalyzer.type(java.lang.Object::class)
            val methods = objectType.methods()
            return this.filter { iMethod ->
                methods.none { oMethod ->
                    iMethod.isOverride(oMethod)
                }
            }
        }

        val allMethods = getRecursive(type)
                .filter { it.isAbstract() }
                .filterOverrides()
                .filterObjectMethods()
        return allMethods
    }

    private fun buildMethodImplementations(methods: List<ElementAnalyzer.Method>): Iterable<MethodSpec> {
        return methods.map { method ->
            val paramNames = method.parameters().joinToString { it.name() }
            val builder = MethodSpec.methodBuilder(method.name())
                    .addAnnotation(Override::class.java)
                    .returns(method.returnTypeReified())
                    .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                    .addExceptions(method.exceptions().map { TypeName.get(it) })
                    .addTypeVariables(method.typeVariables().map { TypeVariableName.get(it.name()) })
                    .addParameters(method.parameters().map { param ->
                        ParameterSpec.builder(param.reify(), param.name()).build()
                    })

            val returnStatement = if (method.returnType().kind != TypeKind.VOID) {
                "return $DELEGATE_METHOD(this).${method.name()}($paramNames)"
            } else {
                "$DELEGATE_METHOD(this).${method.name()}($paramNames)"
            }
            builder.addCode(CodeBlock.builder()
                    .addStatement(returnStatement)
                    .build())

            builder.build()
        }
    }

    private fun buildDelegateType(delegateType: TypeName, sourceTypeVariables: List<TypeVariableName>): TypeName {
        return if (sourceTypeVariables.isEmpty())
            delegateType
        else
            ParameterizedTypeName.get(
                    ClassName.bestGuess(delegateType.toString()),
                    *sourceTypeVariables.toTypedArray()
            )
    }

    private fun buildDelegateMethod(delegateType: TypeName): MethodSpec {

        return MethodSpec.methodBuilder(DELEGATE_METHOD)
                .returns(delegateType)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(ParameterSpec.builder(delegateType, "caller").build())
                .build()
    }

    private companion object {
        const val DELEGATE_METHOD = "delegateTo"
    }
}