# SonarMathematica Rule Catalog

Complete reference for all static analysis rules in the SonarMathematica plugin.

## Table of Contents

- [Overview](#overview)
- [Rule Statistics](#rule-statistics)
- [Severity Legend](#severity-legend)
- [How to Find Rules in SonarQube](#how-to-find-rules-in-sonarqube)
- [Security Vulnerabilities](#security-vulnerabilities)
- [Security Hotspots](#security-hotspots)
- [Bugs](#bugs)
- [Code Smells](#code-smells)
- [Tag Index](#tag-index)

## Overview

The SonarMathematica plugin (v1.0.0+) provides comprehensive static analysis
for Wolfram Language / Mathematica code. This catalog documents all **559** rules
implemented by the plugin, organized by issue type and severity.

## Rule Statistics

| Category | Count |
|----------|-------|
| **Total Rules** | **559** |
| Security Vulnerabilities | 30 |
| Security Hotspots | 25 |
| Bugs | 200 |
| Code Smells | 304 |

### Severity Distribution

| Severity | Count |
|----------|-------|
| HIGH | 50 |
| MEDIUM | 225 |
| LOW | 190 |

## Severity Legend

SonarQube uses severity levels to indicate the impact and priority of issues:

| Severity | Icon | Description | Action Required |
|----------|------|-------------|-----------------|
| **BLOCKER** | 🔴 | Will likely cause application failure or security breach | Fix immediately |
| **CRITICAL** | 🟠 | High probability of impact on application behavior or security | Fix as soon as possible |
| **HIGH** | 🟡 | Significant impact on maintainability, reliability, or security | Fix in current sprint |
| **MEDIUM** | 🔵 | Medium impact on code quality or potential bugs | Fix in upcoming releases |
| **LOW** | ⚪ | Minor issues, code style violations, or nice-to-have improvements | Fix when convenient |
| **INFO** | ℹ️ | Informational findings with no direct impact | Optional to address |

## How to Find Rules in SonarQube

### Using the SonarQube Web Interface

1. **Navigate to Rules**: Click on "Rules" in the top navigation bar
2. **Filter by Language**: Select "Mathematica" from the language filter
3. **Filter by Type**: Choose from:
   - Security Vulnerability
   - Security Hotspot
   - Bug
   - Code Smell
4. **Filter by Severity**: Select HIGH, MEDIUM, LOW, or INFO
5. **Search by Tag**: Use tags like `security`, `performance`, `bug`, etc.
6. **Search by Keyword**: Enter rule names or keywords in the search box

### Using Rule Keys

Each rule has a unique key (e.g., `TOEXPRESSION_ON_INPUT_KEY`). You can:
- Search by key in the SonarQube Rules page
- Reference rules in Quality Profiles using their keys
- Use keys in `// NOSONAR` comments to suppress specific rules

### Quality Profiles

Rules are organized into Quality Profiles:
- **Sonar way** (default): Recommended rules for general use
- **Custom profiles**: Create your own with specific rule sets

---

## Security Vulnerabilities

**27 rules** that detect actual security vulnerabilities requiring immediate attention.

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `UNSAFE_DESERIALIZATION_TAINT_KEY` |  | HIGH | cwe-502, deserialization, owasp-a08 |
| `HARDCODED_API_KEYS_KEY` | API keys and tokens should not be hardcoded | HIGH | credentials, cwe, owasp-a07 |
| `MISSING_CLOUD_AUTH_KEY` | Cloud functions should have authentication and authorization | HIGH | authentication, cwe, owasp-a01 |
| `UNSAFE_CLOUD_DEPLOY_KEY` | CloudDeploy should specify Permissions | HIGH |  |
| `CODE_INJECTION_TAINT_KEY` | Code injection: untrusted data flows to ToExpression | HIGH | code-injection, cwe-94, owasp-a03 |
| `COMMAND_INJECTION_TAINT_KEY` | Command injection: untrusted data flows to RunProcess | HIGH | command-injection, cwe-78, owasp-a03 |
| `DYNAMIC_INJECTION_KEY` | Dynamic content should not use ToExpression on user input | HIGH | injection, security |
| `HARD_CODED_CREDENTIALS_TAINT_KEY` | Hard-coded credentials: string literals in authentication | HIGH | credentials, cwe-798, owasp-a07 |
| `LDAP_INJECTION_KEY` | LDAP injection: untrusted data in LDAP queries | HIGH | cwe-90, ldap-injection, owasp-a03 |
| `MASS_ASSIGNMENT_KEY` | Mass assignment: untrusted association directly used in updates | HIGH | cwe-915, mass-assignment, owasp-a04 |
| `NEEDS_GET_UNTRUSTED_KEY` | Needs and Get should not load code from untrusted paths | HIGH | code-injection, cwe |
| `PATH_TRAVERSAL_TAINT_KEY` | Path traversal: untrusted data flows to file operations | HIGH | cwe-22, owasp-a01, path-traversal |
| `UNSANITIZED_RUNPROCESS_KEY` | RunProcess with user input enables command injection | HIGH | cwe, injection, owasp-a03 |
| `SQL_INJECTION_TAINT_KEY` | SQL injection: untrusted data flows to SQLExecute | HIGH | cwe-89, owasp-a03, sql-injection |
| `SSRF_TAINT_KEY` | SSRF: untrusted URLs in URLFetch/URLExecute | HIGH | cwe-918, owasp-a10, ssrf |
| `TOEXPRESSION_ON_INPUT_KEY` | ToExpression on external input enables code injection | HIGH | cwe, injection, owasp-a03 |
| `XSS_TAINT_KEY` | XSS: untrusted data in HTML/XML output without sanitization | HIGH | cwe-79, owasp-a03, xss |
| `XXE_TAINT_KEY` | XXE: XML External Entity attack via untrusted XML | HIGH | cwe-611, owasp-a05, xxe |
| `CLOUD_API_MISSING_AUTH_KEY` | Cloud API endpoints should require authentication | MEDIUM | api, cloud, security |
| `CLOUD_DEPLOY_NO_VALIDATION_KEY` | Cloud deployments should validate inputs | MEDIUM | cloud, security, validation |
| `EXPOSING_SENSITIVE_DATA_KEY` | Cloud functions should not expose sensitive system information | MEDIUM | cwe, information-disclosure |
| `CLOUD_PERMISSIONS_TOO_OPEN_KEY` | Cloud object permissions should follow least privilege | MEDIUM | cloud, permissions, security |
| `MISSING_FORMFUNCTION_VALIDATION_KEY` | FormFunction inputs should be validated and sanitized | MEDIUM | cwe, owasp-a03, validation |
| `INSECURE_RANDOMNESS_ENHANCED_KEY` | Insecure randomness: RandomInteger for security-sensitive values | MEDIUM | crypto, cwe-330, randomness |
| `REGEX_DOS_KEY` | ReDoS: untrusted data in regex can cause catastrophic backtracking | MEDIUM | cwe-1333, dos, redos |
| `SENSITIVE_DATA_IN_LOGS_KEY` | Sensitive data: credentials or tokens in Print/logs | MEDIUM | cwe-532, logging, sensitive-data |
| `WEAK_CRYPTOGRAPHY_ENHANCED_KEY` | Weak cryptography: MD5 or SHA1 used for security | MEDIUM | crypto, cwe-327, owasp-a02 |

---

## Security Hotspots

**29 rules** that identify security-sensitive code requiring manual review.

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `CERTIFICATE_VALIDATION_DISABLED_KEY` | Certificate validation should not be disabled | HIGH | certificates, security, tls |
| `HARDCODED_CRYPTO_KEY_KEY` | Cryptographic keys should not be hardcoded | HIGH | cryptography, keys, security |
| `DEFAULT_CREDENTIALS_KEY` | Default credentials should not be used | HIGH | credentials, cwe, security |
| `PASSWORD_PLAIN_TEXT_KEY` | Passwords should not be stored in plain text | HIGH | cwe, passwords, security |
| `PII_EXPOSURE_KEY` | Personally Identifiable Information exposure should be reviewed | HIGH | gdpr, privacy, security |
| `MISSING_ACCESS_CONTROL_KEY` | Access control checks should be implemented | MEDIUM | access-control, owasp, security |
| `MISSING_AUTHORIZATION_KEY` | Authorization checks should be present | MEDIUM | authorization, owasp, security |
| `CORS_PERMISSIVE_KEY` | CORS policy should not be overly permissive | MEDIUM | cors, security, web |
| `CLEAR_TEXT_PROTOCOL_KEY` | Clear-text protocols should not be used | MEDIUM | network, protocol, security |
| `INSUFFICIENT_KEY_SIZE_KEY` | Cryptographic key size should be sufficient | MEDIUM | cryptography, keys, security |
| `CRYPTO_KEY_GENERATION_KEY` | Cryptographic keys should be generated securely | MEDIUM |  |
| `DNS_REBINDING_KEY` | DNS rebinding attacks should be prevented | MEDIUM | dns, network, security |
| `FILE_SYSTEM_MODIFICATIONS_KEY` | Destructive file operations should be reviewed | MEDIUM |  |
| `ENVIRONMENT_VARIABLE_KEY` | Environment variable access should be reviewed | MEDIUM |  |
| `EXTERNAL_API_SAFEGUARDS_KEY` | External API calls should have proper safeguards | MEDIUM |  |
| `FILE_UPLOAD_VALIDATION_KEY` | File uploads should be validated | MEDIUM |  |
| `HTTP_WITHOUT_TLS_KEY` | HTTP connections should use TLS | MEDIUM | http, security, tls |
| `NETWORK_OPERATIONS_KEY` | Network operations should be reviewed for security | MEDIUM |  |
| `OPEN_REDIRECT_KEY` | Open redirects should be reviewed | MEDIUM | owasp, redirect, security |
| `INSECURE_RANDOM_HOTSPOT_KEY` | Random number generation should be cryptographically secure | MEDIUM | cryptography, random, security |
| `MISSING_SECURITY_HEADERS_KEY` | Security HTTP headers should be set | MEDIUM | headers, http, security |
| `SENSITIVE_DATA_LOG_KEY` | Sensitive data should not be logged | MEDIUM | logging, privacy, security |
| `INSECURE_SESSION_KEY` | Session management should be secure | MEDIUM | owasp, security, session |
| `WEAK_SESSION_TOKEN_KEY` | Session tokens should be generated securely | MEDIUM | random, security, session |
| `WEAK_SSL_PROTOCOL_KEY` | Weak SSL/TLS protocol versions should not be used | MEDIUM | security, ssl, tls |
| `WEAK_AUTHENTICATION_KEY` | Weak authentication mechanisms should be reviewed | MEDIUM |  |
| `WEAK_CIPHER_MODE_KEY` | Weak cipher modes should not be used | MEDIUM | cipher, cryptography, security |
| `WEAK_HASHING_KEY` | Weak hashing algorithms should not be used | MEDIUM | cryptography, cwe, security |
| `INSECURE_WEBSOCKET_KEY` | WebSocket connections should be secure | MEDIUM | network, security, websocket |

---

## Bugs

**162 rules** that detect probable runtime errors and incorrect behavior.

### HIGH Severity (26 rules)

| Rule Key | Name | Tags |
|----------|------|------|
| `ASSIGNMENT_IN_CONDITION_ENHANCED_KEY` | Assignment in condition instead of comparison | assignment, condition |
| `UNPACKING_PACKED_ARRAYS_KEY` | Avoid operations that unpack packed arrays | packed-arrays, performance |
| `BREAK_OUTSIDE_LOOP_KEY` | Break[] outside loop context causes runtime error | control-flow, runtime-error |
| `UNDEFINED_FUNCTION_CALL_KEY` | Call to undefined function | runtime-error, undefined |
| `CIRCULAR_PACKAGE_DEPENDENCY_KEY` | Circular package dependency causes load order issues | architecture, circular-dependency |
| `CIRCULAR_NEEDS_KEY` | Circular package dependency detected | circular-dependency, packages |
| `STREAM_REOPEN_ATTEMPT_KEY` | Closed streams should not be reused | bug, reliability, resources |
| `MISSING_EMPTY_LIST_CHECK_KEY` | First, Last, and Part should check for empty lists | crash, reliability |
| `FLAT_ATTRIBUTE_MISUSE_KEY` | Flat attribute on non-associative operation | attributes, semantics |
| `PACKAGE_VERSION_MISMATCH_KEY` | Importing incompatible package versions causes runtime errors | compatibility, versioning |
| `INFINITE_LOOP_PROVEN_KEY` | Loop has no exit condition (proven infinite) | hang, infinite-loop |
| `MISMATCHED_BEGIN_END_KEY` | Mismatched BeginPackage/EndPackage or Begin/End | contexts, packages |
| `VARIABLE_ESCAPES_SCOPE_KEY` | Module variable captured in closure may fail | closure, logic-error, scope |
| `CONTEXT_NOT_FOUND_KEY` | Needs references non-existent context | imports, runtime-error |
| `NULL_DEREFERENCE_KEY` | Null dereference causes runtime error | null-safety, runtime-error |
| `ORDERLESS_ATTRIBUTE_ON_NON_COMMUTATIVE_KEY` | Orderless on non-commutative operation | attributes, semantics |
| `PARALLEL_RACE_CONDITION_KEY` | Parallel code should avoid race conditions | bug, concurrency, parallel |
| `PART_SPECIFICATION_OUT_OF_BOUNDS_KEY` | Part specification out of bounds | bounds-check, runtime-error |
| `TYPO_IN_BUILTIN_NAME_KEY` | Possible typo in built-in function name | built-ins, typo |
| `UNDEFINED_VARIABLE_REFERENCE_KEY` | Reference to undefined variable | runtime-error, undefined |
| `SET_DELAYED_CONFUSION_KEY` | Use SetDelayed (:=) for function definitions | common-mistake, reliability |
| `SCOPE_LEAK_THROUGH_DYNAMIC_EVALUATION_KEY` | Variable scope may leak through dynamic evaluation | dynamic, scope, security |
| `USED_BEFORE_ASSIGNMENT_KEY` | Variable used before being assigned | logic-error, uninitialized |
| `UNINITIALIZED_VARIABLE_USE_ENHANCED_KEY` | Variable used before initialization | data-flow, uninitialized |
| `VARIABLE_BEFORE_ASSIGNMENT_KEY` | Variables should not be used before assignment | reliability |
| `INFINITE_LOOP_KEY` | While loops should have an exit condition | reliability |

### MEDIUM Severity (116 rules)

| Rule Key | Name | Tags |
|----------|------|------|
| `ABORT_IN_LIBRARY_CODE_KEY` | Abort[] in library code is too aggressive | error-handling, library-design |
| `ASSOCIATETO_ON_NON_SYMBOL_KEY` | AssociateTo requires a symbol | associations, mutation |
| `INCORRECT_ASSOCIATION_OPERATIONS_KEY` | Association operations differ from List operations | associations, correctness |
| `SOUND_OPERATION_ON_NON_SOUND_KEY` | Audio operation on non-Audio object | audio-processing, type-mismatch |
| `MACHINE_PRECISION_IN_SYMBOLIC_KEY` | Avoid machine precision floats in symbolic calculations | correctness, precision |
| `BOOLEAN_EXPRESSION_ALWAYS_FALSE_KEY` | Boolean expression is contradiction | logic-error |
| `BOOLEAN_EXPRESSION_ALWAYS_TRUE_KEY` | Boolean expression is tautology | logic-error |
| `EMPTY_CATCH_BLOCK_ENHANCED_KEY` | Catch block with no handlers is pointless | dead_code, error-handling |
| `MISSING_FAILED_CHECK_KEY` | Check for $Failed after Import, Get, URLFetch operations | error-handling, reliability |
| `MISSING_KEY_CHECK_KEY` | Check if association key exists before accessing | associations, validation |
| `EMPTY_LIST_INDEXING_KEY` | Check list length before indexing | bounds-check, lists |
| `CIRCULAR_VARIABLE_DEPENDENCIES_KEY` | Circular variable dependencies detected | circular-dependency, logic-error |
| `UNREACHABLE_AFTER_ABORT_THROW_KEY` | Code after Abort or Throw is unreachable | control-flow, dead_code |
| `CODE_AFTER_ABORT_KEY` | Code after Abort[] is unreachable | abort, dead_code |
| `DEAD_AFTER_RETURN_KEY` | Code after Return statement is unreachable | control-flow, dead_code |
| `UNREACHABLE_CODE_AFTER_RETURN_KEY` | Code after Return[] is unreachable |  |
| `COMPARISON_OF_IDENTICAL_EXPRESSIONS_KEY` | Comparing identical expressions | logic-error |
| `CONDITION_ALWAYS_FALSE_CONSTANT_PROPAGATION_KEY` | Condition always evaluates to False | constant-propagation, dead_code |
| `CONDITION_ALWAYS_TRUE_CONSTANT_PROPAGATION_KEY` | Condition always evaluates to True | constant-propagation, dead_code |
| `CONDITION_ALWAYS_EVALUATES_SAME_KEY` | Condition always evaluates to the same value | control-flow, logic-error |
| `CONDITION_ALWAYS_FALSE_KEY` | Condition is always false | dead_code, logic-error |
| `DATASET_OPERATION_ON_LIST_KEY` | Dataset operations require Dataset wrapper | dataset, type-mismatch |
| `ZERO_DENOMINATOR_KEY` | Division operations should guard against zero denominators | reliability |
| `EVALUATION_ORDER_ASSUMPTION_KEY` | Do not rely on implicit evaluation order | undefined-behavior |
| `ASSOCIATION_VS_LIST_CONFUSION_KEY` | Don't use list operations on associations | associations |
| `DYNAMIC_MEMORY_LEAK_KEY` | Dynamic expressions should not cause memory leaks | dynamic, memory, reliability |
| `ELSE_BRANCH_NEVER_TAKEN_KEY` | Else branch is never reachable | conditional, dead_code |
| `EMPTY_EXCEPTION_HANDLER_KEY` | Empty exception handler silently ignores errors | bad-practice, error-handling |
| `FILE_HANDLE_LEAK_KEY` | File handles should be properly released | file-io, reliability, resources |
| `FORWARD_REFERENCE_WITHOUT_DECLARATION_KEY` | Forward reference without explicit declaration | declaration, forward-reference |
| `PATTERN_TYPE_MISMATCH_KEY` | Function call doesn't match pattern types | patterns, type-mismatch |
| `WRONG_ARGUMENT_TYPE_KEY` | Function called with wrong argument type | argument-type, type-mismatch |
| `HOLD_ATTRIBUTE_MISSING_KEY` | Function manipulates unevaluated expressions without Hold attribute | evaluation, hold |
| `FUNCTION_RETURNS_WRONG_TYPE_KEY` | Function returns type different from declaration | return-type, type-mismatch |
| `MISSING_HOLD_ATTRIBUTES_KEY` | Functions delaying evaluation should use Hold attributes | evaluation |
| `FUNCTION_WITHOUT_RETURN_KEY` | Functions should return a value | reliability |
| `MISSING_PATTERN_TEST_VALIDATION_KEY` | Functions should validate input types with pattern tests | reliability, validation |
| `MISSING_PATH_ENTRY_KEY` | Get references file not in $Path | file-system, imports |
| `GRAPH_OPERATION_ON_NON_GRAPH_KEY` | Graph operation on non-Graph object | graph-theory, type-mismatch |
| `GRAPHICS_OBJECT_IN_NUMERIC_CONTEXT_KEY` | Graphics object used in numeric computation | graphics, type-mismatch |
| `MISSING_SPECIAL_CASE_HANDLING_KEY` | Handle special values: 0, Infinity, ComplexInfinity, Indeterminate | edge-cases |
| `HOLD_FIRST_BUT_USES_SECOND_ARGUMENT_FIRST_KEY` | HoldFirst but uses second argument first | evaluation, hold |
| `UNREACHABLE_BRANCH_ALWAYS_FALSE_KEY` | If condition always false makes true branch unreachable | dead_code, logic-error |
| `UNREACHABLE_BRANCH_ALWAYS_TRUE_KEY` | If condition always true makes else branch unreachable | dead_code, logic-error |
| `IMAGE_OPERATION_ON_NON_IMAGE_KEY` | Image operation on non-Image object | image-processing, type-mismatch |
| `INCONSISTENT_NULL_HANDLING_KEY` | Inconsistent null handling across branches | consistency, null-safety |
| `INTEGER_DIVISION_EXPECTING_REAL_KEY` | Integer division stays symbolic, use real division for numeric result | integer-division, numeric-precision |
| `PACKAGE_DEPENDS_ON_APPLICATION_CODE_KEY` | Library package should not depend on application-specific code | architecture, dependency-direction |
| `LIST_FUNCTION_ON_ASSOCIATION_KEY` | List functions should not be used on associations | associations, type-mismatch |
| `SYMBOL_MASKED_BY_IMPORT_KEY` | Local symbol masked by package import | imports, shadowing |
| `LOCAL_SHADOWS_PARAMETER_KEY` | Local variable shadows function parameter | scoping, shadowing |
| `LOOP_NEVER_EXECUTES_KEY` | Loop body never executes | dead_code, loop |
| `MODIFICATION_OF_LOOP_ITERATOR_KEY` | Loop iterator should not be modified inside loop | iterator-modification, loops |
| `OFF_BY_ONE_KEY` | Loop ranges should not cause off-by-one errors | reliability |
| `INCORRECT_CLOSURE_CAPTURE_KEY` | Loop variable incorrectly captured in closure | closure, logic-error, loop |
| `INCORRECT_LEVEL_SPECIFICATION_KEY` | Map, Apply, Cases should use correct level specifications | correctness |
| `MISMATCHED_DIMENSIONS_KEY` | Matrix operations should use rectangular arrays |  |
| `MISSING_MATRIX_DIMENSION_CHECK_KEY` | Matrix operations should validate compatible dimensions | linear-algebra, reliability |
| `MISSING_UNEVALUATED_WRAPPER_KEY` | Missing Unevaluated wrapper causes premature evaluation | evaluation |
| `MISSING_NULL_CHECK_KEY` | Missing null check before usage | null-safety |
| `MISSING_CHECK_LEADS_TO_NULL_PROPAGATION_KEY` | Missing null check causes error cascade | error-handling, null-safety |
| `MISSING_IMPORT_KEY` | Missing package import for external symbol | imports, packages |
| `VARIABLE_ALIASING_ISSUE_KEY` | Multiple variables point to same mutable structure | aliasing, mutable-state |
| `NON_COMPILABLE_CONSTRUCT_IN_COMPILE_KEY` | Non-compilable function in Compile[] falls back to slow evaluation | compilation, performance |
| `NULL_ASSIGNMENT_TO_TYPED_VARIABLE_KEY` | Null assigned to variable expected to be numeric | null-safety, type-mismatch |
| `NULL_PASSED_TO_NON_NULLABLE_KEY` | Null passed to parameter expecting non-null value | null-safety |
| `MISSING_PATTERN_TEST_KEY` | Numeric functions should test argument types |  |
| `NUMERIC_OPERATION_ON_STRING_KEY` | Numeric operations on strings cause runtime errors | runtime-error, type-mismatch |
| `OFF_DISABLING_IMPORTANT_WARNINGS_KEY` | Off[] disables important warnings | bad-practice, error-handling |
| `ONE_IDENTITY_ATTRIBUTE_MISUSE_KEY` | OneIdentity attribute causes subtle issues | attributes, semantics |
| `TYPE_MISMATCH_KEY` | Operations should use compatible types | reliability |
| `OPTIONAL_TYPE_INCONSISTENT_KEY` | Optional parameter default has wrong type | optional-parameters, patterns |
| `NESTED_OPTIONAL_PATTERNS_KEY` | Optional pattern defaults should not depend on other parameters | evaluation-order, patterns |
| `PARALLEL_SHARED_STATE_KEY` | Parallel operations should minimize shared state | concurrency, parallel |
| `PARAMETER_SHADOWS_BUILTIN_KEY` | Parameter shadows built-in function | built-ins, shadowing |
| `PART_ASSIGNMENT_TO_IMMUTABLE_KEY` | Part assignment requires a variable | lists, mutation |
| `PATTERN_BLANKS_MISUSE_KEY` | Pattern blanks should be used correctly | pattern-matching, reliability |
| `IMPOSSIBLE_PATTERN_KEY` | Pattern can never match any input | dead_code, pattern-matching |
| `SUSPICIOUS_PATTERN_KEY` | Pattern matching should not have contradictions |  |
| `PATTERN_NAMING_CONFLICTS_KEY` | Pattern names should not have conflicting type restrictions | patterns |
| `PATTERN_WITH_SIDE_EFFECT_KEY` | Pattern test with side effects evaluated multiple times | patterns, side-effects |
| `PRIVATE_SYMBOL_USED_EXTERNALLY_KEY` | Private` symbol used from another package breaks encapsulation | encapsulation, private-access |
| `PUBLIC_API_NOT_IN_PACKAGE_CONTEXT_KEY` | Public symbol not in package context is wrong context | api, context |
| `CLOSURE_OVER_MUTABLE_VARIABLE_KEY` | Pure function captures mutable variable | closures, variable-capture |
| `QUANTITY_UNIT_MISMATCH_KEY` | Quantity operations should have compatible units | correctness, units |
| `QUERY_ON_NON_DATASET_KEY` | Query requires Dataset wrapper | associations, datasets |
| `QUIET_SUPPRESSING_IMPORTANT_MESSAGES_KEY` | Quiet suppresses critical error messages | bad-practice, error-handling |
| `READING_UNSET_VARIABLE_KEY` | Reading variable after Unset or Clear | data-flow, unset |
| `REPLACE_ALL_VS_REPLACE_CONFUSION_KEY` | ReplaceAll vs Replace confusion | replacement-rules |
| `REPLACEMENT_RULE_ORDER_MATTERS_KEY` | Replacement rule order affects result | patterns, replacement-rules |
| `RESERVED_NAME_USAGE_KEY` | Reserved system variable name used | reserved, system-variables |
| `CLOSE_IN_FINALLY_MISSING_KEY` | Resource cleanup should handle errors | error-handling, reliability, resources |
| `RULE_DOESNT_MATCH_DUE_TO_EVALUATION_KEY` | Rule won't match due to evaluation timing | evaluation, replacement-rules |
| `DUPLICATE_SYMBOL_DEFINITION_ACROSS_PACKAGES_KEY` | Same symbol defined in multiple packages causes conflict | conflict, naming |
| `SEQUENCE_IN_UNEXPECTED_CONTEXT_KEY` | Sequence flattens unexpectedly | sequence, structure |
| `PATTERN_DEFINITION_SHADOWED_KEY` | Specific pattern definition shadowed by more general one | dead_code, pattern-matching |
| `ORDER_DEPENDENT_PATTERNS_KEY` | Specific patterns should be defined before general ones | patterns, unreachable-code |
| `STREAM_NOT_CLOSED_KEY` | Streams should be closed after use | file-io, reliability, resources |
| `STRING_OPERATION_ON_NUMBER_KEY` | String operations on numbers cause runtime errors | runtime-error, type-mismatch |
| `SWITCH_CASE_SHADOWED_KEY` | Switch case is shadowed by earlier more general case | dead_code, switch |
| `SYMBOL_AFTER_ENDPACKAGE_KEY` | Symbol defined after EndPackage | contexts, packages |
| `CONTEXT_CONFLICTS_KEY` | Symbol defined in multiple contexts | ambiguity, contexts |
| `SYMBOL_REDEFINITION_AFTER_IMPORT_KEY` | Symbol defined locally after importing package with same symbol | conflict, shadowing |
| `TEST_NO_ISOLATION_KEY` | Tests should be isolated from each other | isolation, tests |
| `THROW_WITHOUT_CATCH_KEY` | Throw without surrounding Catch aborts evaluation | error-handling |
| `TOTAL_MEAN_ON_NON_NUMERIC_KEY` | Total, Mean should only operate on numeric data | correctness, statistics |
| `TYPE_CAST_WITHOUT_VALIDATION_KEY` | Type conversion without validation | type-casting, validation |
| `PATTERN_REPEATED_DIFFERENT_TYPES_KEY` | Use conditions instead of repeated pattern names for equality checks | patterns |
| `INCORRECT_SET_IN_SCOPING_KEY` | Use proper assignment inside Module and Block | evaluation, scoping |
| `MISSING_PACKAGE_IMPORT_KEY` | Using symbol from package without Needs may fail in fresh kernel | missing-import, runtime-error |
| `NEGATIVE_INDEX_WITHOUT_VALIDATION_KEY` | Validate negative indices against list length | bounds-check, lists |
| `VARIABLE_MAY_BE_UNINITIALIZED_KEY` | Variable may be uninitialized in some code paths | data-flow, uninitialized |
| `REDEFINED_WITHOUT_USE_KEY` | Variable redefined without using previous value | dead_code, logic-error |
| `TYPE_INCONSISTENCY_KEY` | Variable used with inconsistent types | logic-error, type |
| `VERIFICATION_TEST_NO_EXPECTED_KEY` | VerificationTest should specify expected output | tests, verification-test |
| `WRONG_CAPITALIZATION_KEY` | Wrong capitalization of built-in function | built-ins, capitalization |

### LOW Severity (20 rules)

| Rule Key | Name | Tags |
|----------|------|------|
| `CATCH_ALL_EXCEPTION_HANDLER_KEY` | Catch-all exception handler is too broad | error-handling |
| `CHECK_PATTERN_DOESNT_HANDLE_ALL_CASES_KEY` | Check pattern missing error cases | error-handling |
| `TEST_ONLY_CODE_IN_PRODUCTION_KEY` | Code only executed in tests is dead in production | dead_code, testing |
| `EVALUATE_IN_HELD_CONTEXT_KEY` | Evaluate in held context may not be intended | evaluation, hold |
| `INTERNAL_API_USED_LIKE_PUBLIC_KEY` | Internal` symbol called from multiple packages should be public or private | api, encapsulation |
| `LONGEST_SHORTEST_WITHOUT_ORDERING_KEY` | Longest and Shortest require proper context | patterns |
| `USE_OF_ITERATOR_OUTSIDE_LOOP_KEY` | Loop iterator value after loop is undefined | iterator-scope, loops |
| `MESSAGE_WITHOUT_DEFINITION_KEY` | Message issued but not defined | error-handling, messaging |
| `VARIABLE_SCOPE_ESCAPE_KEY` | Module local variable escapes its scope | module, scope |
| `MISSING_PATTERN_DEFAULTS_KEY` | Optional arguments should have sensible defaults | patterns, validation |
| `PATTERN_TEST_VS_CONDITION_KEY` | PatternTest (?) is more efficient than Condition (/;) for simple tests | patterns, performance |
| `SPAN_SPECIFICATION_INVALID_KEY` | Span specification is invalid | spans |
| `MERGE_WITHOUT_CONFLICT_STRATEGY_KEY` | Specify merge function for Merge | associations, clarity |
| `MISSING_DEFAULT_CASE_KEY` | Switch without default case may return unevaluated | completeness, switch |
| `TEST_ASSERT_COUNT_KEY` | Tests should have sufficient assertions | assertions, tests |
| `THREADING_OVER_NON_LISTS_KEY` | Threading over non-list with Listable attribute | attributes, listable |
| `COMPARISON_WITH_NULL_KEY` | Use === for Null comparison, not == | null-safety, semantics |
| `DATEOBJECT_VALIDATION_KEY` | Validate DateObject inputs for invalid dates | validation |
| `VERBATIM_PATTERN_MISUSE_KEY` | Verbatim should only be used when necessary | patterns |
| `VERIFICATION_TEST_TOO_BROAD_KEY` | VerificationTest tolerance should not be too broad | precision, tests, verification-test |

---

## Code Smells

**304 rules** that identify maintainability issues, coding standard violations, and technical debt.

### HIGH Severity (1 rules)

| Rule Key | Name | Tags |
|----------|------|------|
| `PACKAGE_CIRCULAR_DEPENDENCY_KEY` | Packages should not have circular dependencies | dependencies, package |

### MEDIUM Severity (76 rules)

| Rule Key | Name | Tags |
|----------|------|------|
| `APPEND_IN_LOOP_KEY` | AppendTo should not be used in loops |  |
| `ASSIGNMENT_IN_CONDITIONAL_KEY` | Assignments should not be used in conditional expressions |  |
| `INEFFICIENT_LIST_CONCATENATION_KEY` | Avoid repeated Join operations in loops | lists, performance |
| `BLOCK_MODULE_MISUSE_KEY` | Block and Module should be used correctly |  |
| `CODE_INJECTION_KEY` | Code should not be evaluated from user input |  |
| `COMMENTED_OUT_CODE_KEY` | Commented-out code should be removed | comments, dead-code |
| `IDENTICAL_BRANCHES_KEY` | Conditional branches should not be identical | pitfall, suspicious |
| `DEEPLY_NESTED_KEY` | Conditionals should not be nested too deeply |  |
| `HARDCODED_CREDENTIALS_KEY` | Credentials should not be hard-coded |  |
| `DEBUG_CODE_KEY` | Debug code should not be left in production |  |
| `GROWING_DEFINITION_CHAIN_KEY` | Definitions should not grow unbounded |  |
| `VULNERABLE_DEPENDENCY_KEY` | Dependencies with known vulnerabilities should not be used |  |
| `DEPRECATED_FUNCTION_KEY` | Deprecated functions should not be used | obsolete |
| `INSECURE_DESERIALIZATION_KEY` | Deserialization of untrusted data should be avoided |  |
| `DIVISION_BY_ZERO_KEY` | Division operations should check for zero divisors |  |
| `DOCUMENTATION_OUTDATED_KEY` | Documentation should be kept up to date | documentation |
| `DYNAMIC_HEAVY_COMPUTATION_KEY` | Dynamic should not contain expensive computations | dynamic, performance, ui |
| `TEST_MULTIPLE_CONCERNS_KEY` | Each test should verify one concern | single-responsibility, tests |
| `VERIFICATION_TEST_EMPTY_KEY` | Empty VerificationTest provides no value | dead-code, tests, verification-test |
| `EMPTY_BLOCK_KEY` | Empty blocks should be removed | suspicious |
| `EMPTY_CATCH_KEY` | Exceptions should not be silently ignored |  |
| `REPEATED_CALCULATIONS_KEY` | Expensive expressions should not be recalculated in loops | performance |
| `EXPRESSION_TOO_COMPLEX_KEY` | Expressions should not be too complex | brain-overload |
| `FILE_EXCEEDS_ANALYSIS_LIMIT_KEY` | File exceeds analysis size limit |  |
| `UNCLOSED_FILE_HANDLE_KEY` | File handles should be closed |  |
| `PATH_TRAVERSAL_KEY` | File paths should not be constructed from user input |  |
| `HARDCODED_FILE_PATHS_KEY` | File paths should not be hardcoded | portability |
| `FILE_LENGTH_KEY` | Files should not be too long | brain-overload |
| `FLOATING_POINT_EQUALITY_KEY` | Floating point numbers should not be tested for equality |  |
| `TOO_MANY_RETURN_POINTS_KEY` | Function with more than 5 Return statements is hard to reason about |  |
| `GLOBAL_STATE_MODIFICATION_KEY` | Functions modifying global state should be clearly named | conventions, side-effects |
| `DUPLICATE_FUNCTION_KEY` | Functions should not be redefined with same signature |  |
| `FUNCTION_LENGTH_KEY` | Functions should not be too long | brain-overload |
| `TOO_MANY_PARAMETERS_KEY` | Functions should not have too many parameters | brain-overload |
| `INCONSISTENT_RETURN_TYPES_KEY` | Functions should return consistent types | consistency |
| `MISSING_OPTIONS_PATTERN_KEY` | Functions with multiple optional parameters should use OptionsPattern |  |
| `TEST_IGNORED_KEY` | Ignored or skipped tests should be investigated | technical-debt, tests |
| `IMPORT_WITHOUT_FORMAT_KEY` | Import should specify format explicitly |  |
| `NOTEBOOK_INIT_CELL_MISUSE_KEY` | Initialization cells should be used carefully | initialization, notebook |
| `INTERNAL_IMPLEMENTATION_EXPOSED_KEY` | Internal` symbols used from outside are unstable API | api, stability |
| `LARGE_COMMENTED_BLOCK_KEY` | Large blocks of commented code should be removed | comments, dead-code |
| `LIST_INDEX_OUT_OF_BOUNDS_KEY` | List access should be bounds-checked |  |
| `MANIPULATE_PERFORMANCE_KEY` | Manipulate controls should not perform heavy computations | manipulate, performance, ui |
| `INCONSISTENT_NAMING_KEY` | Naming conventions should be consistent |  |
| `NOTEBOOK_CELL_SIZE_KEY` | Notebook cells should not be too large | notebook, readability |
| `LARGE_DATA_IN_NOTEBOOK_KEY` | Notebooks should not store large data structures | memory, notebook, performance |
| `COMMAND_INJECTION_KEY` | OS commands should not be constructed from user input |  |
| `PACKED_ARRAY_UNPACKED_KEY` | Operation unpacks packed array | packed-arrays, performance |
| `PACKAGE_PUBLIC_PRIVATE_MIX_KEY` | Packages should separate public and private symbols | api, package |
| `PACKAGE_NO_BEGIN_KEY` | Packages should use Begin/End for context management | context, package |
| `PARALLEL_NO_GAIN_KEY` | Parallel operations should have sufficient workload | parallel, performance |
| `UNREACHABLE_PATTERN_KEY` | Pattern definitions should not be unreachable |  |
| `PLOT_IN_LOOP_KEY` | Plotting functions should not be called in loops | performance, visualization |
| `API_MISSING_DOCUMENTATION_KEY` | Public functions should be documented | api, documentation |
| `MISSING_DOCUMENTATION_KEY` | Public functions should be documented |  |
| `PACKAGE_NO_USAGE_KEY` | Public package functions should have usage messages | documentation, package |
| `INFINITE_RECURSION_KEY` | Recursive functions must have a base case |  |
| `SQL_INJECTION_KEY` | SQL queries should not be constructed from user input |  |
| `COMMENTED_CODE_KEY` | Sections of code should not be commented out |  |
| `INSECURE_RANDOM_EXPANDED_KEY` | Secure random should be used for security-sensitive operations |  |
| `STRING_CONCAT_IN_LOOP_KEY` | String concatenation should not be used in loops | performance |
| `UNSAFE_SYMBOL_KEY` | Symbol construction from user input should be avoided |  |
| `ANALYSIS_TIMEOUT_KEY` | Symbol table analysis timeout |  |
| `TEST_TOO_LONG_KEY` | Test functions should not be too long | complexity, tests |
| `GLOBAL_VARIABLE_POLLUTION_KEY` | Too many global variables defined | architecture, global, namespace |
| `SSRF_KEY` | URLs should not be constructed from user input |  |
| `LINEAR_SEARCH_INSTEAD_LOOKUP_KEY` | Use Association or Dispatch for lookups instead of Select | algorithmic, performance |
| `MISSING_SANITIZATION_KEY` | User input should be sanitized before use with dangerous functions |  |
| `SYMBOL_NAME_COLLISION_KEY` | User symbols should not shadow built-in functions |  |
| `ASSIGNED_BUT_NEVER_READ_KEY` | Variable assigned but value never read | dead_code, unused |
| `WRITE_ONLY_VARIABLE_KEY` | Variable is only written to, never read | dead_code, unused |
| `MODIFIED_IN_UNEXPECTED_SCOPE_KEY` | Variable modified in unexpected scope | data-flow, maintainability, scope |
| `GENERIC_VARIABLE_NAMES_KEY` | Variables should have meaningful names |  |
| `UNUSED_VARIABLES_KEY` | Variables should not be declared and not used | clutter, unused |
| `WEAK_CRYPTOGRAPHY_KEY` | Weak cryptographic algorithms should not be used |  |
| `XXE_KEY` | XML imports should disable external entity processing |  |

### LOW Severity (170 rules)

| Rule Key | Name | Tags |
|----------|------|------|
| `OVER_ABSTRACTED_API_KEY` | API with single implementation violates YAGNI | abstraction, yagni |
| `OVERWRITTEN_BEFORE_READ_KEY` | Assignment overwritten before being read | data-flow, redundant |
| `ASSIGNMENT_NEVER_READ_KEY` | Assignment value is never read | dead_code, unused |
| `FLATTEN_TABLE_ANTIPATTERN_KEY` | Avoid Flatten[Table[...]] pattern | performance |
| `PATTERN_MATCHING_LARGE_LISTS_KEY` | Avoid pattern matching on large lists | patterns, performance |
| `PATTERN_TEST_PURE_FUNCTION_KEY` | Avoid pure functions in PatternTest for hot code | patterns, performance |
| `UNNECESSARY_TRANSPOSE_KEY` | Avoid repeatedly transposing data | performance |
| `UNRESTRICTED_BLANK_PATTERN_KEY` | Blank patterns should have type restrictions when appropriate | patterns, type-safety |
| `BLANKSEQUENCE_WITHOUT_RESTRICTION_KEY` | BlankSequence should have type restrictions when possible | patterns, performance |
| `COMPLEX_BOOLEAN_EXPRESSION_ENHANCED_KEY` | Boolean expression too complex | complexity, readability |
| `UNTESTED_BRANCH_KEY` | Branch never executed in tests | coverage, testing |
| `PUBLIC_API_CHANGED_WITHOUT_VERSION_BUMP_KEY` | Breaking changes to public API should bump version | api, versioning |
| `BUILTIN_NAME_IN_LOCAL_SCOPE_KEY` | Built-in function name used in local scope | built-ins, shadowing |
| `LENGTH_IN_LOOP_CONDITION_KEY` | Cache list length outside loops | lists, performance |
| `EXCEPTION_NEVER_THROWN_KEY` | Catch handles exception tag that is never thrown | dead_code, exception |
| `CATCH_WITHOUT_THROW_KEY` | Catch statement without corresponding Throw | error-handling, unused |
| `COMMENTED_OUT_PACKAGE_LOAD_KEY` | Commented out Needs[] is dead dependency or TODO | commented-code, dependency |
| `COMPARISON_INCOMPATIBLE_TYPES_KEY` | Comparison of incompatible types | comparison, type-mismatch |
| `COMPILATION_TARGET_MISSING_KEY` | Compile should target C not MVM | compilation, performance |
| `COMPLEX_BOOLEAN_KEY` | Complex boolean expressions should be simplified | complexity, readability |
| `MISSING_RETURN_KEY` | Complex functions should have explicit Return statements | readability |
| `MISSING_OPERATOR_PRECEDENCE_KEY` | Complex operator expressions should use parentheses for clarity | readability |
| `INEFFICIENT_PATTERN_IN_PERFORMANCE_CRITICAL_CODE_KEY` | Complex pattern matching in hot loop | patterns, performance |
| `MISSING_DOWNVALUES_DOC_KEY` | Complex pattern-based functions should have documentation | documentation |
| `EXCESSIVE_PURE_FUNCTIONS_KEY` | Complex pure functions should use named parameters | readability |
| `CONDITIONAL_PACKAGE_LOAD_KEY` | Conditional Needs[] creates fragile dependency | dependency, fragile |
| `CONSTANT_EXPRESSION_KEY` | Constant expression should be simplified | simplification |
| `MISSING_ERROR_MESSAGES_KEY` | Custom functions should define error messages | usability |
| `CYCLIC_CALL_BETWEEN_PACKAGES_KEY` | Cyclic function calls between packages indicate tight coupling | architecture, coupling |
| `DE_MORGANS_LAW_OPPORTUNITY_KEY` | De Morgan's Law could improve clarity | clarity |
| `NESTED_IF_DEPTH_KEY` | Deeply nested If statements (>4 levels) are hard to understand | complexity, nesting |
| `DELETEDUPS_ON_LARGE_DATA_KEY` | DeleteDuplicates on large lists should use alternative methods | performance |
| `DEPRECATED_API_STILL_USED_INTERNALLY_KEY` | Deprecated function still called from same package should migrate | deprecated, migration |
| `DIAMOND_DEPENDENCY_KEY` | Diamond dependency pattern may cause version conflicts | architecture, dependency |
| `DOCUMENTATION_TOO_SHORT_KEY` | Documentation should be adequately detailed | documentation |
| `UNNECESSARY_FLATTEN_KEY` | Don't flatten already-flat lists | lists, performance |
| `LOOKUP_WITH_MISSING_DEFAULT_KEY` | Don't specify Missing as Lookup default | associations, redundant |
| `REVERSE_TWICE_KEY` | Double Reverse is a no-op | lists, redundant |
| `DOUBLE_NEGATION_KEY` | Double negation should be simplified | simplification |
| `KEYDROP_MULTIPLE_TIMES_KEY` | Drop multiple keys in one call | associations, performance |
| `MISSING_LOCALIZATION_KEY` | Dynamic interfaces should use LocalizeVariables | scoping |
| `DYNAMIC_NO_TRACKING_KEY` | Dynamic tracking should be explicit when needed | dynamic, tracking |
| `MULTIPLE_RETURNS_MAKE_CODE_UNREACHABLE_KEY` | Early returns make subsequent code unreachable | dead_code, return |
| `EMPTY_IF_BRANCH_KEY` | Empty If true branch should be inverted | conditional, readability |
| `EMPTY_STATEMENT_KEY` | Empty statements should be removed | suspicious |
| `REPEATED_FUNCTION_CALLS_KEY` | Expensive function calls should not be repeated | performance |
| `MISSING_MEMOIZATION_KEY` | Expensive pure computations should use memoization | performance |
| `PUBLIC_EXPORT_MISSING_USAGE_MESSAGE_KEY` | Exported package function missing usage message | api, documentation, package |
| `FIXME_TRACKING_KEY` | FIXME comments should be tracked | comments, technical-debt |
| `LOW_TEST_COVERAGE_WARNING_KEY` | File has low test coverage | coverage, testing |
| `FUNCTION_ONLY_CALLED_ONCE_KEY` | Function called from exactly one place should be inlined | abstraction, yagni |
| `MISSING_MESSAGE_DEFINITION_KEY` | Function issues messages without defining them | documentation, messaging |
| `UNUSED_PARAMETER_KEY` | Function parameter is never used | parameters, unused |
| `PARAMETER_NOT_DOCUMENTED_KEY` | Function parameters should be documented | documentation, parameters |
| `NULL_RETURN_NOT_DOCUMENTED_KEY` | Function returns Null without documenting it | documentation, null-safety |
| `RETURN_TYPE_INCONSISTENT_KEY` | Function returns inconsistent types | api-design, return-type |
| `MISSING_ATTRIBUTES_DECLARATION_KEY` | Function should have Listable attribute | attributes, performance |
| `COMPILABLE_FUNCTION_NOT_COMPILED_KEY` | Function suitable for Compile[] is not compiled | compilation, performance |
| `TEST_FUNCTION_IN_PRODUCTION_CODE_KEY` | Function with 'Test' in name should be in test package | organization, testing |
| `SIDE_EFFECTS_NAMING_KEY` | Functions with side effects should have descriptive names | naming, side-effects |
| `GLOBAL_IN_PACKAGE_KEY` | Global context used in package code | contexts, packages |
| `FUNCTION_DEFINED_NEVER_CALLED_KEY` | Global function defined but never called | dead_code, unused |
| `SHARED_MUTABLE_STATE_KEY` | Global mutable state accessed from multiple functions | global-state, mutable-state |
| `EXPLICIT_GLOBAL_CONTEXT_KEY` | Global` context should not be used explicitly | conventions |
| `HACK_COMMENT_KEY` | HACK comments indicate technical debt | comments, technical-debt |
| `HOLDPATTERN_UNNECESSARY_KEY` | HoldPattern should be removed when not needed | clutter, patterns |
| `IDENTITY_OPERATION_KEY` | Identity operation has no effect | simplification |
| `MISSING_ELSE_CONSIDERED_HARMFUL_KEY` | If without else can have unclear intent | clarity, conditional |
| `IMPLEMENTATION_WITHOUT_TESTS_KEY` | Implementation file without corresponding test file | coverage, testing |
| `INCONSISTENT_NAMING_CONVENTION_KEY` | Inconsistent naming convention (mix of camelCase, snake_case, PascalCase) | consistency, naming |
| `INCONSISTENT_PARAMETER_NAMES_ACROSS_OVERLOADS_KEY` | Inconsistent parameter names across overloads is confusing | consistency, naming |
| `MISSING_PARALLELIZATION_KEY` | Large independent iterations should use parallelization | parallelization, performance |
| `LARGE_TEMP_EXPRESSIONS_KEY` | Large temporary expressions should be assigned to variables | memory, performance |
| `NO_CLEAR_AFTER_USE_KEY` | Large variables should be cleared after use | memory, performance |
| `LIST_CONCATENATION_IN_LOOP_KEY` | List concatenation in loop has quadratic complexity | lists, performance |
| `LOCAL_SHADOWS_GLOBAL_KEY` | Local variable shadows global variable | naming, shadowing |
| `LOOP_BOUND_CONSTANT_KEY` | Loop bound is constant - use literal | clarity |
| `LOOP_VARIABLE_UNUSED_KEY` | Loop iterator variable is never used in body | loops, unused |
| `LAYER_VIOLATION_KEY` | Lower layer depending on higher layer violates architecture | architecture, layering |
| `MAGIC_NUMBER_KEY` | Magic numbers should not be used | readability |
| `MANIPULATE_TOO_COMPLEX_KEY` | Manipulate should not have too many controls | complexity, manipulate |
| `MIXED_NUMERIC_TYPES_KEY` | Mixing exact and approximate numbers loses precision | numeric-precision, type-mismatch |
| `VARIABLE_NEVER_MODIFIED_KEY` | Module variable never modified, use With instead | best-practice, immutability |
| `N_APPLIED_TOO_LATE_KEY` | N[] applied after symbolic computation | numeric, performance |
| `PACKAGE_LOADED_BUT_NOT_LISTED_IN_METADATA_KEY` | Needs[] not reflected in PacletInfo.m is incomplete metadata | dependency, metadata |
| `NESTED_MAP_TABLE_KEY` | Nested Map/Table should be refactored | performance, readability |
| `NOTEBOOK_UNORGANIZED_KEY` | Notebooks should have clear organization | notebook, organization |
| `NOTEBOOK_NO_SECTIONS_KEY` | Notebooks should use section headers | documentation, notebook |
| `MISSING_COMPILATION_TARGET_KEY` | Numerical code should use CompilationTarget->C | compilation, performance |
| `UNCOMPILED_NUMERICAL_KEY` | Numerical loops should use Compile | optimization, performance |
| `PACKED_ARRAY_BREAKING_KEY` | Operations should preserve packed arrays | arrays, performance |
| `PACKAGE_EXPORTS_TOO_LITTLE_KEY` | Package exporting 0-1 symbols may have questionable design | api, design |
| `PACKAGE_EXPORTS_TOO_MUCH_KEY` | Package exporting more than 50 symbols has poor cohesion | api, cohesion |
| `INCONSISTENT_PACKAGE_NAMING_KEY` | Package names should follow consistent naming convention | consistency, naming |
| `DEAD_PACKAGE_KEY` | Package never imported by anyone is dead code | dead_code, unused |
| `PACKAGE_TOO_SMALL_KEY` | Package with fewer than 50 lines may be over-modularized | over-modularization, size |
| `PACKAGE_TOO_LARGE_KEY` | Package with more than 3000 lines should be split | maintainability, size |
| `GOD_PACKAGE_TOO_MANY_DEPENDENCIES_KEY` | Package with too many dependencies (>10) has high coupling | architecture, coupling |
| `MISSING_PACKAGE_DOCUMENTATION_KEY` | Package without usage message reduces discoverability | discoverability, documentation |
| `REPEATED_STRING_PARSING_KEY` | Parsing the same string multiple times should be avoided | performance |
| `REPEATED_PATTERN_ALTERNATIVES_KEY` | Pattern alternatives should use correct syntax | clarity, patterns |
| `ALTERNATIVES_TOO_COMPLEX_KEY` | Pattern alternatives with many options cause backtracking | patterns, performance |
| `OVERCOMPLEX_PATTERNS_KEY` | Pattern definitions should not be overly complex | complexity, maintainability |
| `PRIVATE_CONTEXT_SYMBOL_PUBLIC_KEY` | Private context symbol used from outside package | encapsulation, packages |
| `UNPROTECTED_SYMBOLS_KEY` | Public API symbols should be protected | api-design, safety |
| `UNTESTED_PUBLIC_FUNCTION_KEY` | Public function has no tests | coverage, testing |
| `UNUSED_PUBLIC_FUNCTION_KEY` | Public function never called from outside may be dead code | dead_code, unused |
| `INCOMPLETE_PUBLIC_API_KEY` | Public function relying on private function breaks encapsulation | api, encapsulation |
| `MISSING_FUNCTION_ATTRIBUTES_KEY` | Public functions should have appropriate attributes | best-practice |
| `MISSING_USAGE_MESSAGE_KEY` | Public functions should have usage messages | documentation |
| `PUBLIC_FUNCTION_WITH_IMPLEMENTATION_DETAILS_IN_NAME_KEY` | Public symbol with 'Internal', 'Helper', 'Private' in name is leaky abstraction | abstraction, naming |
| `PURE_EXPRESSION_IN_LOOP_KEY` | Pure expression computed in every iteration | loop-optimization, performance |
| `MUTATION_IN_PURE_FUNCTION_KEY` | Pure function mutates outer variable | pure-functions, side-effects |
| `MISSING_MEMOIZATION_OPPORTUNITY_ENHANCED_KEY` | Recursive function without memoization | memoization, performance |
| `IMPLICIT_TYPE_CONVERSION_KEY` | Redundant type conversion | redundant, type-conversion |
| `RELEASE_HOLD_AFTER_HOLD_KEY` | ReleaseHold after Hold is redundant | simplification |
| `TRANSITIVE_DEPENDENCY_COULD_BE_DIRECT_KEY` | Relying on transitive dependency is fragile | dependency, fragile |
| `REPEATED_PART_EXTRACTION_KEY` | Repeated Part extractions should be destructured | clarity |
| `INEFFICIENT_STRING_CONCATENATION_ENHANCED_KEY` | Repeated string concatenation in loop | performance, strings |
| `RETURN_NOT_DOCUMENTED_KEY` | Return values should be documented | documentation, return-value |
| `INCONSISTENT_RULE_TYPES_KEY` | Rule and RuleDelayed should be used consistently | consistency |
| `REDUNDANT_COMPUTATION_KEY` | Same expression computed multiple times | caching, performance |
| `MISSING_SEQUENCE_WRAPPER_KEY` | Should use Sequence to avoid extra nesting | idiom, sequence |
| `UNSTABLE_DEPENDENCY_KEY` | Stable package depending on unstable package causes ripple effects | architecture, stability |
| `MULTIPLE_DEFINITIONS_SAME_SYMBOL_KEY` | Symbol defined multiple times | patterns, redefinition |
| `UNUSED_EXPORT_KEY` | Symbol exported but never imported anywhere | api, unused |
| `SYMBOL_NAME_TOO_LONG_KEY` | Symbol name exceeds 50 characters | naming, readability |
| `SYMBOL_NAME_TOO_SHORT_KEY` | Symbol name is too short in large function | naming, readability |
| `SYMBOL_IN_NUMERIC_CONTEXT_KEY` | Symbolic variable in numeric context | numeric-context, symbolic |
| `TODO_TRACKING_KEY` | TODO comments should be tracked | comments, technical-debt |
| `MISSING_TEMPORARY_CLEANUP_KEY` | Temporary files and directories should be cleaned up | resource-management |
| `TEST_DATA_HARDCODED_KEY` | Test data should be clearly defined | test-data, tests |
| `ORPHANED_TEST_FILE_KEY` | Test file for non-existent implementation | orphaned, testing |
| `TEST_NAMING_CONVENTION_KEY` | Test functions should follow naming conventions | naming, tests |
| `TEST_MAGIC_NUMBER_KEY` | Tests should not use unexplained magic numbers | magic-numbers, tests |
| `TODO_FIXME_KEY` | Track TODO and FIXME comments | todo |
| `UNNECESSARY_HOLD_KEY` | Unnecessary Hold on literal | simplification |
| `UNNECESSARY_BOOLEAN_CONVERSION_KEY` | Unnecessary boolean conversion | simplification |
| `ASSIGNMENT_AS_RETURN_VALUE_KEY` | Unnecessary variable assignment before return | redundant, return-value |
| `UNUSED_MODULE_VARIABLE_KEY` | Unused Module variables should be removed | scoping, unused |
| `UNUSED_WITH_VARIABLE_KEY` | Unused With variables should be removed | scoping, unused |
| `UNUSED_FUNCTION_PARAMETER_KEY` | Unused function parameters should be removed or prefixed with underscore | parameters, unused |
| `UNUSED_OPTIONAL_PARAMETER_KEY` | Unused optional parameters should be removed | parameters, unused |
| `UNUSED_PACKAGE_IMPORT_KEY` | Unused package import should be removed | dependency, unused |
| `UNUSED_IMPORT_KEY` | Unused package imports should be removed | imports, unused |
| `UNUSED_PATTERN_NAME_KEY` | Unused pattern names should use blank patterns | patterns, unused |
| `UNUSED_PRIVATE_FUNCTION_KEY` | Unused private functions should be removed | dead_code, unused |
| `ASSOCIATION_UPDATE_PATTERN_KEY` | Use AssociateTo or Append for association updates | associations, clarity |
| `NESTED_LISTS_INSTEAD_ASSOCIATION_KEY` | Use Association instead of nested indexed lists | maintainability, readability |
| `GROUPBY_WITHOUT_AGGREGATION_KEY` | Use GatherBy when not aggregating | associations, clarity |
| `INEFFICIENT_KEY_LOOKUP_KEY` | Use KeySelect instead of Select on Keys | associations, performance |
| `SORT_WITHOUT_COMPARISON_KEY` | Use Reverse[Sort[list]] instead of Sort with Greater | lists, performance |
| `POSITION_VS_SELECT_KEY` | Use Select instead of Extract with Position | clarity, lists |
| `MISSING_SPARSE_ARRAY_KEY` | Use SparseArray for arrays with >80% zeros | memory, performance |
| `STRINGJOIN_FOR_TEMPLATES_KEY` | Use StringTemplate instead of repeated StringJoin | readability |
| `NESTED_PART_EXTRACTION_KEY` | Use multi-dimensional Part syntax | clarity, lists |
| `POSITION_INSTEAD_PATTERN_KEY` | Use pattern matching instead of Position when possible | idiomatic, performance |
| `ALL_SPECIFICATION_INEFFICIENT_KEY` | Using [[All]] is redundant | simplification |
| `DEAD_STORE_KEY` | Value assigned but never read | dead-store, performance |
| `CONSTANT_NOT_MARKED_AS_CONSTANT_KEY` | Variable assigned once should be constant | best-practice, constants |
| `REDUNDANT_ASSIGNMENT_KEY` | Variable assigned same value multiple times | code-smell, redundant |
| `DOUBLE_ASSIGNMENT_SAME_VALUE_KEY` | Variable assigned same value twice | code-smell, redundant |
| `VARIABLE_IN_WRONG_SCOPE_KEY` | Variable could be declared in more specific scope | best-practice, scope |
| `UNUSED_VARIABLE_KEY` | Variable declared but never used | dead_code, unused |
| `LIFETIME_EXTENDS_BEYOND_SCOPE_KEY` | Variable lifetime extends beyond necessary scope | maintainability, memory, scope |
| `NAMING_CONVENTION_VIOLATIONS_KEY` | Variable naming convention violations | naming, readability |
| `VARIABLE_REUSE_WITH_DIFFERENT_SEMANTICS_KEY` | Variable reused for different purposes | clarity, maintainability |
| `VARIABLE_SHADOWING_KEY` | Variable shadows outer scope variable | confusing, naming |
| `TEMP_VARIABLE_NOT_TEMP_KEY` | Variables named 'temp' or 'tmp' used multiple times | naming, readability |
| `VERIFICATION_TEST_NO_DESCRIPTION_KEY` | VerificationTest should have descriptive TestID | documentation, tests, verification-test |

### Coding Standards (21 rules)

Based on best practices from the Emerald Cloud Lab (ECL) Style Guide, adapted for general Mathematica code. *(Note: 21 of 32 proposed rules have been implemented)*

#### Syntax and Whitespace (10 rules)

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `BRACKET_SPACING_BEFORE_KEY` | Opening brackets should not be preceded by whitespace | LOW | style, whitespace |
| `VARIABLE_ASSIGNMENT_IN_MODULE_DEF_KEY` | Variables should not be assigned in Module definition | MEDIUM | best-practice, module |
| `EXPLICIT_AND_OR_FOR_COMPLEX_BOOLEAN_KEY` | Complex boolean expressions should use explicit And[...] or Or[...] | MEDIUM | readability, boolean |
| `MAP_NOT_SHORTHAND_MULTILINE_KEY` | Multi-line statements should use Map[...] instead of /@ | LOW | readability, style |
| `ERROR_MESSAGE_WITH_SET_KEY` | Error messages should be defined with = not := | MEDIUM | best-practice, error-handling |
| `CONDITIONAL_FUNCTION_DEFINITION_KEY` | Functions should not use /; in definition | MEDIUM | best-practice, conditional |
| `DEREFERENCING_SYNTAX_KEY` | Should not use dereferencing syntax | MEDIUM | best-practice, readability |
| `EMPTY_LINE_BETWEEN_CODE_KEY` | Code sections should have empty lines between them | LOW | readability, style |
| `NON_LINEAR_EVALUATION_KEY` | Non-linear evaluation structures should be used sparingly | LOW | readability, complexity |
| `LIST_MODIFICATION_IN_PLACE_KEY` | Lists should not be modified in place | MEDIUM | best-practice, immutability |

#### Local Variables (2 rules)

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `VARIABLE_FULL_WORDS_KEY` | Variable names should use full words, not abbreviations | MEDIUM | naming, readability |
| `VARIABLE_NAME_THREE_WORDS_KEY` | Variable names should not exceed three words | LOW | naming, readability |

#### Function Structure (5 rules)

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `CUSTOM_ASSOCIATIONS_AS_INPUTS_KEY` | Functions should not use custom associations/lists of rules as parameters | MEDIUM | best-practice, interface |
| `GLOBAL_VARIABLE_DOLLAR_PREFIX_KEY` | Global variables should start with $ prefix | MEDIUM | naming, globals |
| `FUNCTION_NAME_THREE_WORDS_KEY` | Function names should not exceed three words | LOW | naming, readability |
| `FUNCTION_NAME_LITTER_WORDS_KEY` | Function names should avoid litter words (Do, Make, Get, And) | LOW | naming, clarity |
| `PURE_FUNCTION_SHORT_OPERATIONS_KEY` | Pure functions should be used for short (<1 line) operations only | MEDIUM | best-practice, readability |

#### Code Organization (1 rule)

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `TIME_CONSTRAINED_USAGE_KEY` | TimeConstrained can kill WSTP programs, avoid unless necessary | CRITICAL | reliability, wstp |

#### Patterns (3 rules)

| Rule Key | Name | Severity | Tags |
|----------|------|----------|------|
| `PATTERN_NAME_ENDS_WITH_P_KEY` | Pattern definitions should end with uppercase P | MEDIUM | naming, patterns |
| `PATTERN_TEST_NAME_ENDS_WITH_Q_KEY` | Pattern test functions should end with uppercase Q | MEDIUM | naming, patterns |
| `ENUMERATED_PATTERN_SYMBOLS_KEY` | Enumerated patterns should use symbols, not strings | MEDIUM | patterns, best-practice |

---

## Tag Index

Rules are tagged with multiple labels for easy filtering. Below are the most common tags:

### Common Tags

| Tag | Count | Category |
|-----|-------|----------|
| `performance` | 48 | Performance |
| `security` | 27 | Security |
| `dead_code` | 26 | Other |
| `patterns` | 22 | Other |
| `unused` | 20 | Code Quality |
| `reliability` | 17 | Other |
| `readability` | 15 | Code Quality |
| `documentation` | 14 | Code Quality |
| `type-mismatch` | 14 | Other |
| `error-handling` | 13 | Other |
| `logic-error` | 13 | Bugs |
| `naming` | 13 | Code Quality |
| `api` | 12 | Other |
| `associations` | 12 | Other |
| `tests` | 12 | Other |
| `clarity` | 11 | Code Quality |
| `lists` | 11 | Other |
| `cwe` | 10 | Security |
| `runtime-error` | 9 | Bugs |
| `architecture` | 8 | Other |
| `null-safety` | 8 | Bugs |
| `owasp-a03` | 8 | Security |
| `evaluation` | 7 | Other |
| `redundant` | 7 | Code Quality |
| `simplification` | 7 | Code Quality |
| `testing` | 7 | Other |
| `validation` | 7 | Other |
| `complexity` | 6 | Code Quality |
| `consistency` | 6 | Other |
| `dependency` | 6 | Other |
| `maintainability` | 6 | Code Quality |
| `memory` | 6 | Performance |
| `packages` | 6 | Other |
| `scope` | 6 | Other |
| `shadowing` | 6 | Other |
| `attributes` | 5 | Other |
| `comments` | 5 | Other |
| `correctness` | 5 | Bugs |
| `cryptography` | 5 | Security |
| `data-flow` | 5 | Other |
| `imports` | 5 | Other |
| `notebook` | 5 | Other |
| `package` | 5 | Other |
| `scoping` | 5 | Other |
| `best-practice` | 4 | Other |
| `brain-overload` | 4 | Other |
| `built-ins` | 4 | Other |
| `compilation` | 4 | Other |
| `contexts` | 4 | Other |
| `control-flow` | 4 | Other |

**Total unique tags**: 273

---

## Additional Resources

- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Wolfram Language Documentation](https://reference.wolfram.com/language/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CWE - Common Weakness Enumeration](https://cwe.mitre.org/)

---

*Generated from rule-catalog.json - SonarMathematica v1.0.0*