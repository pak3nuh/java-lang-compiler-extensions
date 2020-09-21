package io.github.pak3nuh.util.lang.sealed.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import java.util.Objects
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

class TypeWriter(val filer: Filer) {
    fun write(roundData: RoundData) {
        roundData.hierarchies
                .map {
                    val newName = ClassName.bestGuess(it.rootType.qualifiedName.toString() + "Expression")
                    val tVar: TypeVariableName = TypeVariableName.get("T")
                    val typeSpec = TypeSpec.interfaceBuilder(newName)
                            .addTypeVariable(tVar)
                            .addMethods(interfaceMethods(it.children, tVar))
                            .addMethod(evalMethod(it.children, TypeName.get(it.rootType.asType()), newName))
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
                                addStatement("return delegate.\$L()", child.simpleName.toString())
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
                    .build()
        }
    }
}
