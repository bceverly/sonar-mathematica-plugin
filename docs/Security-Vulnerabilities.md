# Security Vulnerabilities

This page documents **27 Security Vulnerability rules** that detect actual security bugs in your Mathematica code. These are **CRITICAL** and **HIGH** severity issues that could lead to data breaches, unauthorized access, or system compromise.

**See also:** [[Security Hotspots]] | [[Bug Detection]] | [[Best Practices]]

---

## Table of Contents

- [OWASP Top 10 Coverage](#owasp-top-10-coverage)
- [Critical Vulnerabilities (18 rules)](#critical-vulnerabilities)
- [High Severity Vulnerabilities (9 rules)](#high-severity-vulnerabilities)
- [How to Fix](#how-to-fix)
- [Prevention Strategies](#prevention-strategies)

---

## OWASP Top 10 Coverage

This plugin covers **9 of 10** OWASP Top 10 2021 categories:

| OWASP | Category | Rules | Examples |
|-------|----------|-------|----------|
| **A01** | Broken Access Control | 3 | Missing authentication, path traversal |
| **A02** | Cryptographic Failures | 3 | Weak algorithms, hardcoded keys |
| **A03** | Injection | 8 | SQL, Command, Code, LDAP, XSS |
| **A04** | Insecure Design | 2 | Missing validation, mass assignment |
| **A05** | Security Misconfiguration | 3 | XXE, exposed data, insecure defaults |
| **A06** | Vulnerable Components | 1 | Unsafe deserialization |
| **A07** | Authentication Failures | 2 | Weak auth, hardcoded credentials |
| **A08** | Software & Data Integrity | 1 | Code injection via Get/Needs |
| **A10** | Server-Side Request Forgery | 1 | SSRF via URLRead/URLExecute |

---

## Critical Vulnerabilities

### 1. SQL Injection (CRITICAL)

**Rule:** `SQL_INJECTION_TAINT_KEY`
**Severity:** CRITICAL
**OWASP:** A03:2021 - Injection
**CWE:** CWE-89

**What it detects:**
Untrusted data flowing into `SQLExecute`, `SQLSelect`, or `SQLInsert` without proper sanitization or parameterization.

**Why it matters:**
Attackers can execute arbitrary SQL commands, leading to data theft, modification, or deletion. This is consistently in OWASP Top 3.

**BAD Example:**
```mathematica
(* VULNERABLE: Direct string concatenation *)
ProcessUserQuery[username_String] := Module[{query, conn, result},
  conn = OpenSQLConnection[JDBC["MySQL", "localhost:3306/mydb"], "user", "pass"];
  query = "SELECT * FROM users WHERE name = '" <> username <> "'";
  result = SQLExecute[conn, query];
  CloseSQLConnection[conn];
  result
]

(* Attack: ProcessUserQuery["admin' OR '1'='1"]
   Executes: SELECT * FROM users WHERE name = 'admin' OR '1'='1'
   Result: Returns ALL users! *)
```

**GOOD Example:**
```mathematica
(* SECURE: Use parameterized queries *)
ProcessUserQuery[username_String] := Module[{query, conn, result},
  conn = OpenSQLConnection[JDBC["MySQL", "localhost:3306/mydb"], "user", "pass"];
  (* Use ? placeholder and pass parameter separately *)
  query = "SELECT * FROM users WHERE name = ?";
  result = SQLExecute[conn, query, {username}];
  CloseSQLConnection[conn];
  result
]

(* Even better: Use SQLSelect with proper escaping *)
ProcessUserQuerySafe[username_String] := Module[{conn, result},
  conn = OpenSQLConnection[JDBC["MySQL", "localhost:3306/mydb"], "user", "pass"];
  result = SQLSelect[conn, "users", {"*"}, SQLColumn["name"] == username];
  CloseSQLConnection[conn];
  result
]
```

**How to fix:**
1. **Use parameterized queries** with `?` placeholders
2. **Use SQLSelect/SQLInsert** functions that handle escaping
3. **Validate and sanitize** all user inputs
4. **Never concatenate** SQL strings with user data

**References:**
- OWASP: https://owasp.org/Top10/A03_2021-Injection/
- CWE-89: https://cwe.mitre.org/data/definitions/89.html
- Wolfram: SQLExecute documentation

---

### 2. Command Injection (CRITICAL)

**Rule:** `COMMAND_INJECTION_TAINT_KEY`
**Severity:** CRITICAL
**OWASP:** A03:2021 - Injection
**CWE:** CWE-78

**What it detects:**
Untrusted data flowing into `RunProcess`, `Run`, or `StartProcess` without validation.

**Why it matters:**
Attackers can execute arbitrary system commands, potentially taking over the entire system.

**BAD Example:**
```mathematica
(* VULNERABLE: Direct command execution *)
ProcessUserFile[filename_String] := Module[{output},
  output = RunProcess[{"cat", filename}];
  output["StandardOutput"]
]

(* Attack: ProcessUserFile["data.txt; rm -rf /"]
   Executes: cat data.txt; rm -rf /
   Result: Deletes everything! *)

(* Also vulnerable *)
ConvertImage[file_String] := Module[{cmd},
  cmd = "convert " <> file <> " output.jpg";
  RunProcess[{"sh", "-c", cmd}]
]
```

**GOOD Example:**
```mathematica
(* SECURE: Validate and use safe functions *)
ProcessUserFileSafe[filename_String] := Module[{output, safeName},
  (* 1. Validate filename - only alphanumeric, dots, underscores *)
  If[!StringMatchQ[filename, RegularExpression["^[a-zA-Z0-9._-]+$"]],
    Return[$Failed, "Invalid filename"]
  ];

  (* 2. Check file exists and is in allowed directory *)
  safeName = FileNameJoin[{$UserDocumentsDirectory, filename}];
  If[!FileExistsQ[safeName],
    Return[$Failed, "File not found"]
  ];

  (* 3. Use Import instead of shell commands *)
  Import[safeName, "Text"]
]

(* Better: Use built-in functions instead of shell commands *)
ConvertImageSafe[file_String] := Module[{img},
  (* Validate first *)
  If[!FileExistsQ[file], Return[$Failed]];

  (* Use Mathematica's built-in image processing *)
  img = Import[file];
  Export["output.jpg", img, "JPEG"]
]
```

**How to fix:**
1. **Never pass user input** to shell commands
2. **Use Mathematica built-ins** instead (Import, Export, etc.)
3. **Validate all inputs** with strict whitelist patterns
4. **Avoid shell interpreters** (`sh -c`, `bash -c`)
5. If shell is necessary, use **argument arrays** not strings

---

### 3. Code Injection via ToExpression (CRITICAL)

**Rule:** `CODE_INJECTION_TAINT_KEY`, `TOEXPRESSION_ON_INPUT_KEY`
**Severity:** CRITICAL
**OWASP:** A03:2021 - Injection
**CWE:** CWE-94

**What it detects:**
Using `ToExpression`, `Get`, or `Needs` on untrusted input.

**Why it matters:**
Allows attackers to execute arbitrary Mathematica code with full system access.

**BAD Example:**
```mathematica
(* EXTREMELY VULNERABLE *)
EvaluateUserFormula[formula_String] := ToExpression[formula]

(* Attack: EvaluateUserFormula["DeleteFile[\"important.txt\"]"]
   Result: File deleted! *)

(* Also vulnerable: loading code from user paths *)
LoadUserPlugin[path_String] := Get[path]

(* Attack: LoadUserPlugin["/tmp/malicious.m"]
   Result: Malicious code executed! *)
```

**GOOD Example:**
```mathematica
(* SECURE: Use safe evaluation with restrictions *)
EvaluateUserFormulaSafe[formula_String] := Module[{expr, result},
  (* 1. Parse without evaluation *)
  expr = Quiet[ToExpression[formula, InputForm, Hold]];

  If[FailureQ[expr] || expr === Hold[],
    Return[$Failed, "Invalid formula"]
  ];

  (* 2. Check for dangerous functions *)
  If[!FreeQ[expr, _DeleteFile | _Run | _RunProcess | _Get | _Install],
    Return[$Failed, "Forbidden functions in formula"]
  ];

  (* 3. Whitelist allowed functions only *)
  If[!FreeQ[expr, Except[_?(MemberQ[{Plus, Times, Power, Sin, Cos, Log, Exp}, Head[#]]&)]],
    Return[$Failed, "Only basic math allowed"]
  ];

  (* 4. Evaluate with timeout *)
  TimeConstrained[ReleaseHold[expr], 1, $Failed]
]

(* Even better: Use a DSL parser *)
ParseSafeMath[input_String] := Module[{tokens, ast},
  (* Write a custom parser for your safe subset *)
  (* This is the most secure approach *)
  ParseMathExpression[input]
]
```

**How to fix:**
1. **Never use ToExpression** on user input
2. **Create a safe DSL** and parse it yourself
3. **Whitelist allowed operations** only
4. **Use sandboxing** if evaluation is absolutely necessary
5. **Validate extensively** before any evaluation

---

### 4. Path Traversal (CRITICAL)

**Rule:** `PATH_TRAVERSAL_TAINT_KEY`
**Severity:** CRITICAL
**OWASP:** A01:2021 - Broken Access Control
**CWE:** CWE-22

**What it detects:**
Untrusted data used in file paths for `Import`, `Export`, `DeleteFile`, `CopyFile`, etc.

**Why it matters:**
Attackers can read/write/delete files outside intended directories, accessing sensitive data.

**BAD Example:**
```mathematica
(* VULNERABLE *)
LoadUserData[filename_String] := Module[{path, data},
  path = FileNameJoin[{$UserDocumentsDirectory, "data", filename}];
  Import[path, "JSON"]
]

(* Attack: LoadUserData["../../../etc/passwd"]
   Path becomes: /Users/name/Documents/data/../../../etc/passwd
   Simplified: /etc/passwd
   Result: Reads password file! *)

ExportUserReport[name_String, data_] := Module[{path},
  path = "/var/reports/" <> name <> ".txt";
  Export[path, data]
]

(* Attack: ExportUserReport["../../../tmp/malicious", maliciousData]
   Result: Writes outside reports directory! *)
```

**GOOD Example:**
```mathematica
(* SECURE: Validate and canonicalize paths *)
LoadUserDataSafe[filename_String] := Module[{basePath, requestedPath, canonicalPath, data},
  (* 1. Define allowed base directory *)
  basePath = FileNameJoin[{$UserDocumentsDirectory, "data"}];

  (* 2. Validate filename - no path separators *)
  If[StringContainsQ[filename, "/" | "\\"],
    Return[$Failed, "Filename cannot contain path separators"]
  ];

  (* 3. Build full path *)
  requestedPath = FileNameJoin[{basePath, filename}];

  (* 4. Get canonical (absolute, resolved) path *)
  canonicalPath = ExpandFileName[requestedPath];

  (* 5. Verify it's still within base directory *)
  If[!StringStartsQ[canonicalPath, basePath],
    Return[$Failed, "Path traversal detected"]
  ];

  (* 6. Check file exists *)
  If[!FileExistsQ[canonicalPath],
    Return[$Failed, "File not found"]
  ];

  (* 7. Now safe to import *)
  Import[canonicalPath, "JSON"]
]

(* Alternative: Use FileNameTake to strip any path *)
LoadUserDataSimple[filename_String] := Module[{safeFilename, path},
  (* FileNameTake gets just the filename, removing any path *)
  safeFilename = FileNameTake[filename];
  path = FileNameJoin[{$UserDocumentsDirectory, "data", safeFilename}];
  If[FileExistsQ[path],
    Import[path, "JSON"],
    $Failed
  ]
]
```

**How to fix:**
1. **Validate filenames** - no `/`, `\`, `..`
2. **Use FileNameJoin** - handles path separators correctly
3. **Canonicalize paths** with ExpandFileName
4. **Check final path** is within allowed directory
5. **Use FileNameTake** to strip directory components

---

### 5. Cross-Site Scripting (XSS) (CRITICAL)

**Rule:** `XSS_TAINT_KEY`
**Severity:** CRITICAL
**OWASP:** A03:2021 - Injection
**CWE:** CWE-79

**What it detects:**
Untrusted data included in HTML/XML output without proper escaping, especially in `CloudDeploy`, `FormFunction`, `APIFunction`.

**Why it matters:**
Attackers can inject malicious JavaScript into web pages, stealing cookies, session tokens, or performing actions as the victim.

**BAD Example:**
```mathematica
(* VULNERABLE: Direct HTML generation *)
GenerateUserProfile[name_String] := Module[{html},
  html = "<html><body><h1>Welcome " <> name <> "</h1></body></html>";
  CloudDeploy[APIFunction[{}, html &], Permissions -> "Public"]
]

(* Attack: GenerateUserProfile["<script>alert(document.cookie)</script>"]
   Result: JavaScript executes in victim's browser! *)

(* Also vulnerable: FormFunction *)
ShowComment[comment_String] := FormFunction[{},
  "<p>Comment: " <> comment <> "</p>" &
]
```

**GOOD Example:**
```mathematica
(* SECURE: Use XMLElement or proper escaping *)
GenerateUserProfileSafe[name_String] := Module[{escaped, html},
  (* 1. Escape HTML special characters *)
  escaped = StringReplace[name, {
    "<" -> "&lt;",
    ">" -> "&gt;",
    "&" -> "&amp;",
    "\"" -> "&quot;",
    "'" -> "&#x27;"
  }];

  (* 2. Build HTML safely *)
  html = "<html><body><h1>Welcome " <> escaped <> "</h1></body></html>";

  CloudDeploy[APIFunction[{}, html &], Permissions -> "Public"]
]

(* Even better: Use XMLElement *)
GenerateUserProfileBest[name_String] := Module[{doc},
  doc = XMLElement["html", {},
    {XMLElement["body", {},
      {XMLElement["h1", {}, {"Welcome ", name}]}
    ]}
  ];

  (* XMLElement automatically escapes content *)
  CloudDeploy[APIFunction[{}, ExportString[doc, "HTML"] &]]
]

(* Best for FormFunction: Use structured output *)
ShowCommentSafe[comment_String] := FormFunction[{"comment" -> "String"},
  Column[{
    "Comment:",
    Panel[#comment, Background -> LightGray]
  }] &
]
```

**How to fix:**
1. **Escape all user data** before including in HTML
2. **Use XMLElement** for structured HTML generation
3. **Use FormFunction with structured output** (Column, Panel, etc.)
4. **Never concatenate** HTML strings with user data
5. **Set Content-Security-Policy** headers

---

### 6. Hardcoded Credentials (CRITICAL)

**Rule:** `HARD_CODED_CREDENTIALS_TAINT_KEY`, `HARDCODED_API_KEYS_KEY`
**Severity:** CRITICAL
**OWASP:** A07:2021 - Identification and Authentication Failures
**CWE:** CWE-798

**What it detects:**
Passwords, API keys, tokens, or secrets hardcoded in source code.

**Why it matters:**
Anyone with access to the code (including version control history) can extract credentials and access systems.

**BAD Example:**
```mathematica
(* VULNERABLE: Hardcoded credentials *)
ConnectToDatabase[] := Module[{conn},
  conn = OpenSQLConnection[
    JDBC["MySQL", "production-db.company.com:3306/users"],
    "admin",
    "SuperSecret123!"  (* NEVER DO THIS! *)
  ];
  conn
]

(* Also bad: API keys *)
apiKey = "sk-1234567890abcdef";
CallOpenAI[prompt_] := URLExecute[
  "https://api.openai.com/v1/completions",
  "Method" -> "POST",
  "Headers" -> {"Authorization" -> "Bearer " <> apiKey}
]

(* Bad: Cloud credentials *)
CloudConnect[CloudObject["https://wolfram.cloud"],
  Username -> "user@example.com",
  Password -> "MyPassword123"
]
```

**GOOD Example:**
```mathematica
(* SECURE: Load from environment variables *)
ConnectToDatabaseSafe[] := Module[{host, user, pass, conn},
  (* Get credentials from environment *)
  host = Environment["DB_HOST"];
  user = Environment["DB_USER"];
  pass = Environment["DB_PASSWORD"];

  If[AnyTrue[{host, user, pass}, MissingQ],
    Return[$Failed, "Database credentials not configured"]
  ];

  conn = OpenSQLConnection[
    JDBC["MySQL", host <> ":3306/users"],
    user,
    pass
  ];
  conn
]

(* Better: Use configuration file (outside version control) *)
LoadCredentials[] := Module[{configPath, config},
  configPath = FileNameJoin[{$UserBaseDirectory, "Configurations", "credentials.m"}];
  If[!FileExistsQ[configPath],
    Return[$Failed, "Credentials file not found"]
  ];
  Get[configPath]  (* File contains: credentials = <|"db" -> ...|> *)
]

ConnectToDatabaseBest[] := Module[{creds, conn},
  creds = LoadCredentials[];
  If[FailureQ[creds], Return[$Failed]];

  conn = OpenSQLConnection[
    JDBC["MySQL", creds["db"]["host"]],
    creds["db"]["user"],
    creds["db"]["password"]
  ];
  conn
]

(* For API keys: Use SystemCredential *)
CallOpenAISafe[prompt_] := Module[{key},
  key = SystemCredential["OpenAI"];
  If[FailureQ[key], Return[$Failed, "API key not configured"]];

  URLExecute[
    "https://api.openai.com/v1/completions",
    "Method" -> "POST",
    "Headers" -> {"Authorization" -> "Bearer " <> key}
  ]
]
```

**How to fix:**
1. **Use environment variables** - `Environment["VAR_NAME"]`
2. **Use SystemCredential** for API keys
3. **Store in config files** outside version control
4. **Add .gitignore** for config files
5. **Rotate credentials** if accidentally committed
6. **Use secrets management** (AWS Secrets Manager, HashiCorp Vault)

---

## Additional Critical Vulnerabilities (Summary)

### 7. LDAP Injection
**Rule:** `LDAP_INJECTION_KEY` | **CWE-90**

Untrusted data in LDAP queries. Use parameterized queries or escape special characters: `*`, `(`, `)`, `\`, `NUL`.

### 8. XXE (XML External Entity)
**Rule:** `XXE_TAINT_KEY` | **CWE-611**

Processing untrusted XML with external entities enabled. Disable DTDs: `ImportString[xml, "XML", "ProcessDTDs" -> False]`.

### 9. Unsafe Deserialization
**Rule:** `UNSAFE_DESERIALIZATION_TAINT_KEY` | **CWE-502**

Deserializing untrusted data with `Get` or `Import[..., "MX"]`. Use safe formats like JSON.

### 10. Server-Side Request Forgery (SSRF)
**Rule:** `SSRF_TAINT_KEY` | **CWE-918**

`URLRead`/`URLExecute` with user-controlled URLs. Validate against whitelist of allowed hosts.

---

## High Severity Vulnerabilities

### 11. Missing Cloud Authentication
**Rule:** `MISSING_CLOUD_AUTH_KEY` | **Severity: HIGH**

`CloudDeploy` without proper authentication:
```mathematica
(* BAD *)
CloudDeploy[APIFunction[{}, SensitiveData[] &], Permissions -> "Public"]

(* GOOD *)
CloudDeploy[APIFunction[{}, SensitiveData[] &],
  Permissions -> "Private",
  Authentication -> "Required"
]
```

### 12. Loading Untrusted Code
**Rule:** `NEEDS_GET_UNTRUSTED_KEY` | **Severity: HIGH**

Using `Get` or `Needs` with user-controlled paths:
```mathematica
(* BAD *)
LoadPlugin[userPath_] := Get[userPath]

(* GOOD *)
LoadPlugin[name_String] := Module[{safePath, whitelist},
  whitelist = {"PluginA", "PluginB", "PluginC"};
  If[!MemberQ[whitelist, name], Return[$Failed]];
  safePath = FileNameJoin[{$UserBaseDirectory, "Plugins", name <> ".m"}];
  If[FileExistsQ[safePath], Get[safePath], $Failed]
]
```

### 13-27. Additional High Severity Rules

- **Weak Cryptography**: Using MD5, SHA-1, DES (`WEAK_CRYPTOGRAPHY_ENHANCED_KEY`)
- **Insecure Randomness**: Using `Random[]` for security (`INSECURE_RANDOMNESS_ENHANCED_KEY`)
- **Missing Input Validation**: FormFunction without validation (`MISSING_FORMFUNCTION_VALIDATION_KEY`)
- **Sensitive Data Exposure**: Logging passwords/tokens (`SENSITIVE_DATA_IN_LOGS_KEY`)
- **Mass Assignment**: Allowing arbitrary field updates (`MASS_ASSIGNMENT_KEY`)
- **Regex DoS**: Complex regex on untrusted input (`REGEX_DOS_KEY`)

See the [[Rule Catalog]] for complete details on all 27 rules.

---

## How to Fix Vulnerabilities

### General Strategy

1. **Input Validation**
   - Whitelist allowed characters/patterns
   - Reject anything suspicious
   - Validate length, format, type

2. **Output Encoding**
   - HTML-escape for web output
   - SQL parameterization for databases
   - Shell escaping for commands

3. **Least Privilege**
   - Run with minimal permissions
   - Sandbox untrusted code
   - Restrict file/network access

4. **Defense in Depth**
   - Multiple layers of security
   - Validate at every boundary
   - Log security events

### Fixing in SonarQube

1. Click the vulnerability in Issues tab
2. Read the description and examples
3. Click "Quick Fix" if available (53 rules have automated fixes)
4. Test the fix thoroughly
5. Mark as "Won't Fix" only if false positive

---

## Prevention Strategies

### During Development

1. **Enable all security rules** in Quality Profile
2. **Run SonarLint in IDE** for real-time feedback
3. **Review security issues** before committing
4. **Never disable security rules** without review

### Code Review Checklist

- [ ] No hardcoded credentials or API keys?
- [ ] All user input validated?
- [ ] SQL uses parameterized queries?
- [ ] File paths canonicalized?
- [ ] HTML output escaped?
- [ ] ToExpression avoided on user input?
- [ ] Cloud functions have authentication?

### Testing

1. **Test with malicious input**
   - SQL injection payloads
   - Command injection: `; rm -rf /`
   - Path traversal: `../../etc/passwd`
   - XSS: `<script>alert(1)</script>`

2. **Use security testing tools**
   - OWASP ZAP for web endpoints
   - sqlmap for SQL injection
   - Burp Suite for comprehensive testing

### Deployment

1. **Use HTTPS** for all cloud functions
2. **Set security headers** (CSP, X-Frame-Options)
3. **Enable authentication** on cloud deployments
4. **Monitor logs** for attack attempts
5. **Keep plugin updated** for new security rules

---

## References

- **OWASP Top 10**: https://owasp.org/Top10/
- **CWE**: https://cwe.mitre.org/
- **Wolfram Security**: https://www.wolfram.com/legal/security/
- **Plugin GitHub**: https://github.com/your-repo

**Next:** [[Security Hotspots]] →
