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
                if (it.kind != ElementKind.INTERFACE) {
                    logError("Only interfaces are allowed for delegation: ${it.qualifiedName}")
                }
                val sourceType = elementAnalyzer.type(it)
                if (isBridge(sourceType)) {
                    BridgeWriter(sourceType, elementAnalyzer, this::logError)
                } else {
                    DelegateWriter(sourceType, elementAnalyzer, this::logError)
                }
            }
        }
    }

    private fun isBridge(sourceType: ElementAnalyzer.Type): Boolean {
        return sourceType.annotations()
                .first { it.isA(Delegate::class.java) }.values()
                .first { it.name == Delegate::bridge.name }
                .value as Boolean
    }


}

private class BridgeWriter(private val sourceType: ElementAnalyzer.Type,
                           private val elementAnalyzer: ElementAnalyzer,
                           private val logError: (String) -> Unit) : FileWriteable {
    override fun writeTo(filer: Filer) {
        val newClassName = ClassName.bestGuess("${sourceType.typeName()}Bridge")
        val sourceName = ClassName.bestGuess(sourceType.name())
        val sourceTypeVariables = sourceType.typeVariables().map { TypeVariableName.get(it.type) as TypeVariableName }
        val parameterizedDelegateType = buildDelegateType(sourceName, sourceTypeVariables)
        val delegateInterfaceBuilder = TypeSpec.interfaceBuilder(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Generated code. DO NOT MODIFY MANUALLY!")
                .addSuperinterface(parameterizedDelegateType)
                .addTypeVariables(sourceTypeVariables)

        sourceType.interfaces().forEach {
            val methods = getAbstractMethods(it, elementAnalyzer)
            if (methods.isNotEmpty()) {
                val bridgeTypeName = TypeName.get(it.type)
                delegateInterfaceBuilder.addMethod(buildDelegateToMethod(bridgeTypeName))
                        .addMethods(buildMethodImplementations(methods, bridgeTypeName))
            }
        }

        JavaFile.builder(sourceName.packageName(), delegateInterfaceBuilder.build())
                .build().writeTo(filer)
    }

}

private class DelegateWriter(
        val sourceType: ElementAnalyzer.Type,
        val elementAnalyzer: ElementAnalyzer,
        val logError: (String) -> Unit
) : FileWriteable {
    override fun writeTo(filer: Filer) {
        if (sourceType.interfaces().size > 1) {
            logError("Delegate $sourceType extends more than one interface")
        }
        val newClassName = ClassName.bestGuess("${sourceType.typeName()}Delegate")
        val sourceName = ClassName.bestGuess(sourceType.name())
        val sourceTypeVariables = sourceType.typeVariables().map { TypeVariableName.get(it.type) as TypeVariableName }
        val parameterizedDelegateType = buildDelegateType(sourceName, sourceTypeVariables)
        val delegateInterface = TypeSpec.interfaceBuilder(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Generated code. DO NOT MODIFY MANUALLY!")
                .addAnnotation(FunctionalInterface::class.java)
                .addSuperinterface(parameterizedDelegateType)
                .addTypeVariables(sourceTypeVariables)
                .addMethod(buildDelegateToMethod(parameterizedDelegateType))
                .addMethods(buildMethodImplementations(getAbstractMethods(sourceType, elementAnalyzer), parameterizedDelegateType))
                .build()

        JavaFile.builder(sourceName.packageName(), delegateInterface)
                .build().writeTo(filer)
    }

}

private const val DELEGATE_METHOD = "delegateTo"

private fun buildMethodImplementations(methods: List<ElementAnalyzer.Method>,
                                       delegateType: TypeName): Iterable<MethodSpec> {
    return methods.map { method ->
        val paramNames = method.parameters().joinToString { it.name() }
        val builder = MethodSpec.methodBuilder(method.name())
                .addAnnotation(Override::class.java)
                .returns(method.returnTypeReified())
                .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                .addExceptions(method.exceptions().map { TypeName.get(it) })
                .addTypeVariables(method.typeVariables().map { TypeVariableName.get(it.type) as TypeVariableName })
                .addParameters(method.parameters().map { param ->
                    ParameterSpec.builder(param.reify(), param.name()).build()
                })

        builder.addCode(CodeBlock.builder()
                .apply {
                    if (method.returnType().kind != TypeKind.VOID) {
                        addStatement("return \$L((\$T)this).\$L(\$L)", DELEGATE_METHOD, delegateType, method.name(), paramNames)
                    } else {
                        addStatement("\$L((\$T)this).\$L(\$L)", DELEGATE_METHOD, delegateType, method.name(), paramNames)
                    }
                }
                .build())

        builder.build()
    }
}

private fun buildDelegateType(delegateType: TypeName, sourceTypeVariables: List<TypeName>): TypeName {
    return if (sourceTypeVariables.isEmpty())
        delegateType
    else
        ParameterizedTypeName.get(
                ClassName.bestGuess(delegateType.toString()),
                *sourceTypeVariables.toTypedArray()
        )
}

private fun buildDelegateToMethod(delegateType: TypeName): MethodSpec {
    return MethodSpec.methodBuilder(DELEGATE_METHOD)
            .returns(delegateType)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(ParameterSpec.builder(delegateType, "caller").build())
            .build()
}


private fun getAbstractMethods(type: ElementAnalyzer.Type, elementAnalyzer: ElementAnalyzer)
        : List<ElementAnalyzer.Method> {
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

    return getRecursive(type)
            .filter { it.isAbstract() }
            .filterOverrides()
            .filterObjectMethods()
}
