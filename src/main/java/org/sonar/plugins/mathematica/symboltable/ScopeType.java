package org.sonar.plugins.mathematica.symboltable;

/**
 * Type of lexical scope in Mathematica code.
 */
public enum ScopeType {
    /**
     * Global scope (file-level)
     */
    GLOBAL,

    /**
     * Module[{vars}, body] - lexical scoping with unique variable names
     */
    MODULE,

    /**
     * Block[{vars}, body] - dynamic scoping, shadows global variables
     */
    BLOCK,

    /**
     * With[{vars}, body] - constant substitution scope
     */
    WITH,

    /**
     * Function parameter scope (e.g., f[x_, y_] := ...)
     */
    FUNCTION,

    /**
     * BeginPackage/EndPackage scope
     */
    PACKAGE,

    /**
     * Begin["`Private`"]/End[] scope
     */
    PRIVATE_CONTEXT
}
