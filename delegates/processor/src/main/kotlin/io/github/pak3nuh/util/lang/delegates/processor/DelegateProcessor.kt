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
                DelegateWriter(sourceType)
            }
        }
    }

}

private class DelegateWriter(val sourceType: ElementAnalyzer.Type) : FileWriteable {
    override fun writeTo(filer: Filer) {
        val newClassName = "${sourceType.typeName()}Delegate"
        val sourceName = ClassName.bestGuess(sourceType.name())
        val sourceTypeVariables = sourceType.typeVariables().map { TypeVariableName.get(it.name()) }
        val parameterizedDelegateType = buildDelegateType(sourceName, sourceTypeVariables)
        val delegateInterface = TypeSpec.interfaceBuilder(newClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(parameterizedDelegateType)
                .addTypeVariables(sourceTypeVariables)
                .addMethod(buildDelegateMethod(parameterizedDelegateType))
                .addMethods(buildMethodImplementations(getAbstractMethods(sourceType)))
                .build()
        JavaFile.builder(sourceName.packageName(), delegateInterface).build().writeTo(filer)
    }

    private fun getAbstractMethods(type: ElementAnalyzer.Type): List<ElementAnalyzer.Method> {
        return type.methods().filter { it.isAbstract() }
    }

    private fun buildMethodImplementations(methods: List<ElementAnalyzer.Method>): Iterable<MethodSpec> {
        return methods.map { method ->
            val paramNames = method.parameters().joinToString { it.name() }
            val builder = MethodSpec.methodBuilder(method.name())
                    .addAnnotation(Override::class.java)
                    .returns(TypeName.get(method.returnType()))
                    .addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
                    .addTypeVariables(method.typeVariables().map { TypeVariableName.get(it.name()) })
                    .addParameters(method.parameters().map { param ->
                        ParameterSpec.builder(TypeName.get(param.type()), param.name()).build()
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
                    *sourceTypeVariables.map { ClassName.bestGuess(it.name) }.toTypedArray()
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