package io.github.pak3nuh.util.processor

import javax.lang.model.element.*
import javax.lang.model.type.*

interface EmptyElementVisitor<R, P>: ElementVisitor<R, P> {
    override fun visit(e: Element, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visit(e: Element): R {
        throw UnsupportedOperationException()
    }

    override fun visitPackage(e: PackageElement, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitType(e: TypeElement, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitVariable(e: VariableElement, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitExecutable(e: ExecutableElement, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitTypeParameter(e: TypeParameterElement, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitUnknown(e: Element, p: P): R {
        throw UnsupportedOperationException()
    }
}

interface EmptyTypeVisitor<R, P>: TypeVisitor<R, P> {
    override fun visit(t: TypeMirror, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visit(t: TypeMirror): R {
        throw UnsupportedOperationException()
    }

    override fun visitPrimitive(t: PrimitiveType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitNull(t: NullType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitArray(t: ArrayType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitDeclared(t: DeclaredType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitError(t: ErrorType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitTypeVariable(t: TypeVariable, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitWildcard(t: WildcardType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitExecutable(t: ExecutableType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitNoType(t: NoType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitUnknown(t: TypeMirror, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitUnion(t: UnionType, p: P): R {
        throw UnsupportedOperationException()
    }

    override fun visitIntersection(t: IntersectionType, p: P): R {
        throw UnsupportedOperationException()
    }
}

interface UnitElementVisitor<R>: EmptyElementVisitor<R, Unit>

interface UnitTypeVisitor<R>: EmptyTypeVisitor<R, Unit>