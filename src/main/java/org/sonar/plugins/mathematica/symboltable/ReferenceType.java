package org.sonar.plugins.mathematica.symboltable;

/**
 * Type of reference to a symbol (variable).
 */
public enum ReferenceType {
    /**
     * Variable is read (e.g., Print[x])
     */
    READ,

    /**
     * Variable is written/assigned (e.g., x = 5)
     */
    WRITE,

    /**
     * Variable is both read and written (e.g., x += 5, x++)
     */
    READ_WRITE
}
