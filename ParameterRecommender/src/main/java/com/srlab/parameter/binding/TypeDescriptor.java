package com.srlab.parameter.binding;

import java.io.Serializable;

import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedWildcard.BoundType;

public class TypeDescriptor implements Serializable{

	public boolean primitiveType;
	public boolean array;
	private boolean nullType;
	private boolean reference;
	private boolean referenceType;
	private boolean typeVariable;
	private boolean voidType;
	private boolean unionType;
	private boolean wildcard;
	private BoundType boundType;
	
	private String name;
	public TypeDescriptor(ResolvedType resolvedType) {
		this.primitiveType = resolvedType.isPrimitive();
		this.array = resolvedType.isArray();
		this.nullType = resolvedType.isNull();
		this.reference = resolvedType.isReference();
		this.referenceType = resolvedType.isReferenceType();
		this.typeVariable = resolvedType.isTypeVariable();
		this.voidType = resolvedType.isVoid(); 
		this.unionType = resolvedType.isUnionType();
		this.wildcard = resolvedType.isWildcard();
		this.boundType = null;
		this.name = this.getName(resolvedType);
	}
	
	public String getName(ResolvedType resolvedType) {
		
		if(resolvedType.isPrimitive()) {	
			return resolvedType.asPrimitive().name();
		}
		else if(resolvedType.isReferenceType()) {
			return resolvedType.asReferenceType().getQualifiedName();		
		}
		else if(resolvedType.isVoid()) {
			return "void";
		}
		else if(resolvedType.isNull()) {
			return "null";
		}
		else if(resolvedType.isArray()) {
			return this.getName(resolvedType.asArrayType().getComponentType());
		}
		else if(resolvedType.isUnionType()) {
			return this.getName(resolvedType.asUnionType());
		}
		else if(resolvedType.isTypeVariable()) {
			return resolvedType.asTypeVariable().qualifiedName();
		}
		else if(resolvedType.isWildcard()) {
			if(resolvedType.asWildcard().isSuper())
				this.boundType = BoundType.SUPER;
			else if(resolvedType.asWildcard().isExtends()) {
				this.boundType = BoundType.EXTENDS;
			}
			else {
				this.boundType = null;
			}
			return this.getName(resolvedType.asWildcard().getBoundedType());
		}
		else throw new RuntimeException("Failed to understand resolved type: "+resolvedType.toString());
	}

	public boolean isPrimitiveType() {
		return primitiveType;
	}

	public boolean isArray() {
		return array;
	}

	public boolean isNullType() {
		return nullType;
	}

	public boolean isReference() {
		return reference;
	}

	public boolean isReferenceType() {
		return referenceType;
	}

	public boolean isTypeVariable() {
		return typeVariable;
	}

	public boolean isVoidType() {
		return voidType;
	}

	public boolean isUnionType() {
		return unionType;
	}

	public boolean isWildcard() {
		return wildcard;
	}

	public BoundType getBoundType() {
		return boundType;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "[typename=" + name + "]";
	}
}