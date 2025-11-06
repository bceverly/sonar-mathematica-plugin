package org.sonar.plugins.mathematica.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple recursive descent parser for Mathematica code.
 *
 * This parser builds an Abstract Syntax Tree (AST) from Mathematica source code.
 * It handles basic structures:
 * - Function definitions (f[x_] := body)
 * - Function calls (f[x])
 * - Identifiers and literals
 * - Simple expressions
 *
 * Limitations (for now):
 * - No operator precedence parsing
 * - Limited pattern matching support
 * - No complex list/association parsing
 * - No full expression parsing
 *
 * This provides a foundation for semantic analysis that can be enhanced over time.
 */
public class MathematicaParser {

    private static final Logger LOG = LoggerFactory.getLogger(MathematicaParser.class);

    // Patterns for parsing
    // PERFORMANCE FIX: Possessive quantifier (*+) prevents catastrophic backtracking on long strings
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*+\"");
    private static final Pattern FUNCTION_DEF_PATTERN = Pattern.compile(
        "([a-zA-Z$][a-zA-Z0-9$]*+)\\s*+\\[([^\\]]*+)\\]\\s*+(:?=)"
    );
    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile(
        "([a-zA-Z$][a-zA-Z0-9$]*+)\\s*+\\[([^\\]]*+)\\]"
    );
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d++\\.?\\d*+(?:[eE][+-]?\\d++)?+");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z$][a-zA-Z0-9$]*+");

    // PERFORMANCE: Cache line offsets for O(log n) lookups instead of O(n) scans
    private int[] lineOffsets;

    // PERFORMANCE: Pre-compute delimiter positions to avoid O(n²) scanning
    private int[] semicolonPositions;
    private int[] assignmentPositions;

    /**
     * Parse Mathematica source code into an AST.
     *
     * Returns a list of top-level AST nodes (usually function definitions).
     */
    public List<AstNode> parse(String content) {
        List<AstNode> nodes = new ArrayList<>();

        try {
            long startTotal = System.currentTimeMillis();

            // PERFORMANCE: Build line offset array once for O(log n) lookups
            long startLineOffsets = System.currentTimeMillis();
            this.lineOffsets = buildLineOffsetArray(content);
            long lineOffsetsTime = System.currentTimeMillis() - startLineOffsets;

            // PERFORMANCE: Remove comments efficiently (manual scan instead of regex)
            long startComments = System.currentTimeMillis();
            String cleanContent = removeComments(content);
            long commentsTime = System.currentTimeMillis() - startComments;

            // PERFORMANCE: Pre-scan for delimiters once (O(n)) instead of per-function (O(n²))
            long startDelimiters = System.currentTimeMillis();
            precomputeDelimiterPositions(cleanContent);
            long delimitersTime = System.currentTimeMillis() - startDelimiters;

            // Parse function definitions
            long startParsing = System.currentTimeMillis();
            nodes.addAll(parseFunctionDefinitions(cleanContent));
            long parsingTime = System.currentTimeMillis() - startParsing;

            long totalTime = System.currentTimeMillis() - startTotal;

            if (totalTime > 1000) {
                LOG.debug("PROFILE Parser: total={}ms (lineOffsets={}ms, comments={}ms, delimiters={}ms, parsing={}ms) for {} chars",
                    totalTime, lineOffsetsTime, commentsTime, delimitersTime, parsingTime, content.length());
            }

        } catch (Exception e) {
            LOG.warn("Error parsing Mathematica code: {}", e.getMessage());
        }

        return nodes;
    }

    /**
     * Parse all function definitions in the content.
     */
    private List<AstNode> parseFunctionDefinitions(String content) {
        List<AstNode> functions = new ArrayList<>();

        long startMatcher = System.currentTimeMillis();
        Matcher matcher = FUNCTION_DEF_PATTERN.matcher(content);
        long matcherTime = System.currentTimeMillis() - startMatcher;

        long totalMatchFind = 0;
        long totalParseExpr = 0;
        int funcCount = 0;

        while (matcher.find()) {
            funcCount++;
            long findTime = System.currentTimeMillis();
            try {
                String functionName = matcher.group(1);
                String parametersStr = matcher.group(2);
                String assignOp = matcher.group(3);
                boolean isDelayed = ":=".equals(assignOp);

                int startPos = matcher.start();
                int startLine = calculateLine(startPos);
                int startColumn = calculateColumn(content, startPos);

                // Parse parameters
                List<String> parameters = parseParameters(parametersStr);

                // Find function body (simplified - find up to next function or semicolon)
                int bodyStart = matcher.end();
                int bodyEnd = findBodyEnd(content, bodyStart);
                String bodyStr = content.substring(bodyStart, bodyEnd).trim();

                // Parse body (simplified - just create a placeholder for now)
                long startExpr = System.currentTimeMillis();
                AstNode body = parseExpression(bodyStr, startLine, startColumn);
                totalParseExpr += (System.currentTimeMillis() - startExpr);

                int endLine = calculateLine(bodyEnd);
                int endColumn = calculateColumn(content, bodyEnd);

                FunctionDefNode funcNode = new FunctionDefNode(
                    functionName, parameters, body, isDelayed,
                    startLine, startColumn, endLine, endColumn
                );

                functions.add(funcNode);

            } catch (Exception e) {
                LOG.debug("Error parsing function definition: {}", e.getMessage());
            }
            totalMatchFind += (System.currentTimeMillis() - findTime);
        }

        if (funcCount > 100) {
            LOG.debug("PROFILE parseFunctionDefs: funcCount={}, matcherCreate={}ms, matchFind={}ms, parseExpr={}ms",
                funcCount, matcherTime, totalMatchFind, totalParseExpr);
        }

        return functions;
    }

    /**
     * Parse function parameters from parameter string.
     */
    private List<String> parseParameters(String parametersStr) {
        List<String> parameters = new ArrayList<>();

        if (parametersStr == null || parametersStr.trim().isEmpty()) {
            return parameters;
        }

        // Split by comma (simplified - doesn't handle nested brackets)
        String[] parts = parametersStr.split(",");
        for (String part : parts) {
            String param = part.trim();
            if (!param.isEmpty()) {
                // Extract parameter name (before _ or ?)
                int underscorePos = param.indexOf('_');
                int questionPos = param.indexOf('?');
                int endPos = param.length();

                if (underscorePos > 0) {
                    endPos = Math.min(endPos, underscorePos);
                }
                if (questionPos > 0) {
                    endPos = Math.min(endPos, questionPos);
                }

                parameters.add(param.substring(0, endPos).trim());
            }
        }

        return parameters;
    }

    /**
     * Find the end of a function body using pre-computed delimiter positions.
     * PERFORMANCE: O(log n) binary search instead of O(n) indexOf scan.
     */
    private int findBodyEnd(String content, int start) {
        // Binary search in pre-computed arrays
        int semicolon = binarySearchNext(semicolonPositions, start);
        int nextFunc = binarySearchNext(assignmentPositions, start + 1);

        if (semicolon == -1 && nextFunc == -1) {
            return content.length();
        } else if (semicolon == -1) {
            return nextFunc;
        } else if (nextFunc == -1) {
            return semicolon;
        } else {
            return Math.min(semicolon, nextFunc);
        }
    }

    /**
     * Binary search to find next position > start in sorted array.
     * PERFORMANCE: O(log n) instead of O(n).
     */
    private int binarySearchNext(int[] positions, int start) {
        if (positions == null || positions.length == 0) {
            return -1;
        }

        int left = 0;
        int right = positions.length - 1;

        // Find first position > start
        while (left <= right) {
            int mid = (left + right) / 2;
            if (positions[mid] <= start) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return (left < positions.length) ? positions[left] : -1;
    }

    /**
     * Parse a simple expression (simplified - creates identifier or literal node).
     */
    private AstNode parseExpression(String expr, int line, int column) {
        expr = expr.trim();

        if (expr.isEmpty()) {
            return null;
        }

        // Try to match number
        Matcher numberMatcher = NUMBER_PATTERN.matcher(expr);
        if (numberMatcher.matches()) {
            double value = Double.parseDouble(expr);
            return new LiteralNode(value, LiteralNode.LiteralType.REAL, line, column, line, column + expr.length());
        }

        // Try to match string
        Matcher stringMatcher = STRING_PATTERN.matcher(expr);
        if (stringMatcher.matches()) {
            String value = expr.substring(1, expr.length() - 1);  // Remove quotes
            return new LiteralNode(value, LiteralNode.LiteralType.STRING, line, column, line, column + expr.length());
        }

        // Try to match function call
        Matcher funcCallMatcher = FUNCTION_CALL_PATTERN.matcher(expr);
        if (funcCallMatcher.find()) {
            String functionName = funcCallMatcher.group(1);
            String argsStr = funcCallMatcher.group(2);

            List<AstNode> arguments = new ArrayList<>();
            // Simplified argument parsing
            if (!argsStr.trim().isEmpty()) {
                String[] args = argsStr.split(",");
                for (String arg : args) {
                    AstNode argNode = parseExpression(arg.trim(), line, column);
                    if (argNode != null) {
                        arguments.add(argNode);
                    }
                }
            }

            return new FunctionCallNode(functionName, arguments, line, column, line, column + expr.length());
        }

        // Default to identifier
        Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(expr);
        if (identifierMatcher.find()) {
            String name = identifierMatcher.group();
            return new IdentifierNode(name, line, column, line, column + name.length());
        }

        return null;
    }

    /**
     * Build an array of line start offsets for fast O(log n) line number lookup.
     * PERFORMANCE: This is built once per file, then used for all lookups.
     */
    private int[] buildLineOffsetArray(String content) {
        // Count lines first
        int lineCount = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineCount++;
            }
        }

        // Build offset array
        int[] offsets = new int[lineCount];
        offsets[0] = 0;
        int lineIndex = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n' && lineIndex < offsets.length) {
                offsets[lineIndex++] = i + 1;
            }
        }
        return offsets;
    }

    /**
     * Calculate line number from position using binary search on cached offsets.
     * PERFORMANCE: O(log n) instead of O(n) linear scan.
     */
    private int calculateLine(int position) {
        if (lineOffsets == null || lineOffsets.length == 0) {
            return 1;
        }

        // Binary search to find the line
        int left = 0;
        int right = lineOffsets.length - 1;
        while (left < right) {
            int mid = (left + right + 1) / 2;
            if (lineOffsets[mid] <= position) {
                left = mid;
            } else {
                right = mid - 1;
            }
        }
        return left + 1;
    }

    /**
     * Calculate column number from position using cached line offsets.
     * PERFORMANCE: O(1) instead of O(n) backward scan.
     */
    private int calculateColumn(String content, int position) {
        if (lineOffsets == null || lineOffsets.length == 0) {
            return 0;
        }

        int line = calculateLine(position);
        int lineStartOffset = lineOffsets[line - 1];
        return position - lineStartOffset;
    }

    /**
     * Pre-compute positions of all semicolons and := operators in O(n) time.
     * PERFORMANCE: This avoids O(n²) behavior when calling findBodyEnd() for each function.
     */
    private void precomputeDelimiterPositions(String content) {
        List<Integer> semicolons = new ArrayList<>();
        List<Integer> assignments = new ArrayList<>();

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == ';') {
                semicolons.add(i);
            } else if (c == ':' && i + 1 < content.length() && content.charAt(i + 1) == '=') {
                assignments.add(i);
                i++; // Skip the '=' character
            }
        }

        this.semicolonPositions = semicolons.stream().mapToInt(Integer::intValue).toArray();
        this.assignmentPositions = assignments.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Remove Mathematica comments (* ... *) efficiently.
     * PERFORMANCE: Manual state machine instead of regex replaceAll.
     */
    private String removeComments(String content) {
        StringBuilder result = new StringBuilder(content.length());
        int depth = 0;
        int i = 0;

        while (i < content.length()) {
            // Check for comment start (*
            if (i + 1 < content.length() && content.charAt(i) == '(' && content.charAt(i + 1) == '*') {
                depth++;
                i += 2;
                continue;
            }

            // Check for comment end *)
            if (i + 1 < content.length() && content.charAt(i) == '*' && content.charAt(i + 1) == ')') {
                if (depth > 0) {
                    depth--;
                }
                i += 2;
                continue;
            }

            // If not in a comment, append character
            if (depth == 0) {
                result.append(content.charAt(i));
            }
            i++;
        }

        return result.toString();
    }
}
