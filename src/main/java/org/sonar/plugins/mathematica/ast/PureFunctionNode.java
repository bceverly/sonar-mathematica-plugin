package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * AST node representing pure/anonymous functions.
 *
 * Mathematica has two forms of anonymous functions:
 * 1. Slot-based: #1 + #2 & (uses # for arguments)
 * 2. Function form: Function[{x, y}, x + y]
 *
 * This is Item 10 from ROADMAP_325.md (Core AST Infrastructure).
 */
public class PureFunctionNode extends AstNode {

    public enum PureFunctionForm {
        SLOT_BASED,    // #1 + #2 &
        FUNCTION_FORM  // Function[{x, y}, x + y]
    }

    private final PureFunctionForm form;
    private final List<String> parameters;  // For Function form
    private final AstNode body;
    private final int maxSlotNumber;        // For slot-based form (highest #n)

    public PureFunctionNode(
        PureFunctionForm form,
        List<String> parameters,
        AstNode body,
        int maxSlotNumber,
        SourceLocation location
    ) {
        super(NodeType.PURE_FUNCTION, location);
        this.form = form;
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        this.body = body;
        this.maxSlotNumber = maxSlotNumber;
    }

    @SuppressWarnings("java:S107") // Backward compatibility constructor, prefer SourceLocation-based one
    public PureFunctionNode(
        PureFunctionForm form,
        List<String> parameters,
        AstNode body,
        int maxSlotNumber,
        int startLine,
        int startColumn,
        int endLine,
        int endColumn
    ) {
        this(form, parameters, body, maxSlotNumber,
             new SourceLocation(startLine, startColumn, endLine, endColumn));
    }

    public PureFunctionForm getForm() {
        return form;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public AstNode getBody() {
        return body;
    }

    public int getMaxSlotNumber() {
        return maxSlotNumber;
    }

    public boolean isSlotBased() {
        return form == PureFunctionForm.SLOT_BASED;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (body != null) {
            body.accept(visitor);
        }
    }

    @Override
    public String toString() {
        if (form == PureFunctionForm.SLOT_BASED) {
            return String.format("PureFunction[slots=%d, body=%s]", maxSlotNumber, body);
        } else {
            return String.format("Function[params=%s, body=%s]", parameters, body);
        }
    }
}
