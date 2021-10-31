package io.github.pak3nuh.util.lang.sealed.processor

import com.squareup.javapoet.*
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

class TypeWriter(private val filer: Filer) {
    fun write(roundData: RoundData) {
        roundData.hierarchies
                .map {
                    val newName = ClassName.bestGuess(it.rootType.qualifiedName.toString() + "Expression")
                    val tVar: TypeVariableName = TypeVariableName.get("T")
                    val typeSpec = TypeSpec.interfaceBuilder(newName)
                            .addTypeVariable(tVar)
                            .addMethods(interfaceMethods(it.children, tVar))
                            .addMethod(evalMethod(it.children, TypeName.get(it.rootType.asType()), newName))
                            .addMethod(evalLambdaMethod(it.children, TypeName.get(it.rootType.asType())))
                            .build()

                    JavaFile.builder(newName.packageName(), typeSpec).build()
                }.forEach {
                    it.writeTo(filer)
                }

    }

    private fun evalMethod(children: List<TypeElement>, rootType: TypeName, exprTypeName: ClassName): MethodSpec {
        val evalType = TypeVariableName.get("W")
        return MethodSpec.methodBuilder("eval")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(evalType)
                .returns(evalType)
                .addParameter(rootType, "value")
                .addParameter(ParameterizedTypeName.get(exprTypeName, evalType), "delegate")
                .addCode(CodeBlock.builder()
                        .apply {
                            addStatement("\$T.requireNonNull(value)", Objects::class.java)
                            children.forEach { child ->
                                val asTypeName = TypeName.get(child.asType())
                                beginControlFlow("if (value instanceof \$T)", asTypeName)
                                addStatement("return delegate.\$L((\$T)value)", child.simpleName.toString(), TypeName.get(child.asType()))
                                endControlFlow()
                            }
                            addStatement("throw new IllegalStateException(String.valueOf(value))")
                        }
                        .build())
                .build()
    }

    private fun evalLambdaMethod(children: List<TypeElement>, rootType: TypeName): MethodSpec {
        val evalType = TypeVariableName.get("W")
        val functionType = ClassName.get(java.util.function.Function::class.java)
        return MethodSpec.methodBuilder("evalLambda")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(evalType)
                .returns(evalType)
                .addParameter(rootType, "value")
                .addParameters(
                        children.mapIndexed { idx, elem ->
                            val specializedFunction = ParameterizedTypeName.get(functionType, elem.asTypeName(), evalType)
                            ParameterSpec.builder(specializedFunction, "param$idx").build()
                        }
                )
                .addCode(CodeBlock.builder()
                        .apply {
                            addStatement("\$T.requireNonNull(value)", Objects::class.java)
                            children.forEachIndexed { idx, child ->
                                val asTypeName = child.asTypeName()
                                beginControlFlow("if (value instanceof \$T)", asTypeName)
                                addStatement("return param\$L.apply((\$T)value)", idx, TypeName.get(child.asType()))
                                endControlFlow()
                            }
                            addStatement("throw new IllegalStateException(String.valueOf(value))")
                        }
                        .build())
                .build()
    }

    private fun interfaceMethods(children: List<TypeElement>, tVar: TypeVariableName): Iterable<MethodSpec> {
        return children.map {
            val implName = it.simpleName.toString()
            MethodSpec.methodBuilder(implName)
                    .returns(tVar)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(ParameterSpec.builder(TypeName.get(it.asType()), "value").build())
                    .build()
        }
    }

    private fun Element.asTypeName(): TypeName = TypeName.get(this.asType())
}
