package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing Association literals (Mathematica's dictionary/map type).
 *
 * Mathematica associations:
 * - <|"a" -> 1, "b" -> 2|>: Simple association
 * - <|"x" -> <|"y" -> 1|>|>: Nested association
 * - <||>: Empty association
 *
 * This is Item 6 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class AssociationNode extends AstNode {

    /**
     * Represents a single key-value pair in an association.
     */
    public static class KeyValuePair {
        private final AstNode key;
        private final AstNode value;

        public KeyValuePair(AstNode key, AstNode value) {
            this.key = key;
            this.value = value;
        }

        public AstNode getKey() {
            return key;
        }

        public AstNode getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s -> %s", key, value);
        }
    }

    private final List<KeyValuePair> pairs;

    public AssociationNode(
        List<KeyValuePair> pairs,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        super(NodeType.ASSOCIATION, startLine, startColumn, endLine, endColumn);
        this.pairs = pairs != null ? pairs : new ArrayList<>();
    }

    public List<KeyValuePair> getPairs() {
        return pairs;
    }

    public int getSize() {
        return pairs.size();
    }

    public boolean isEmpty() {
        return pairs.isEmpty();
    }

    @Override
    public void accept(AstVisitor visitor) {
        for (KeyValuePair pair : pairs) {
            if (pair.getKey() != null) {
                pair.getKey().accept(visitor);
            }
            if (pair.getValue() != null) {
                pair.getValue().accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Association[size=%d]", pairs.size());
    }
}
