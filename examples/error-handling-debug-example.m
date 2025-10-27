(* Example file demonstrating error handling and debug code rules *)
(* These rules improve OWASP A05 and A09 coverage *)

(* ====================
   RULE 1: Empty Catch Blocks (MAJOR) - OWASP A09
   Security Logging and Monitoring Failures
   ==================== *)

(* VIOLATION: Check with only $Failed, no logging *)
result = Check[riskyDatabaseOperation[], $Failed];

(* VIOLATION: Check with Null, silently ignoring errors *)
data = Check[ParseUserInput[untrustedData], Null];

(* VIOLATION: Check with None *)
output = Check[WriteToSecureLocation[file], None];

(* VIOLATION: Quiet suppresses all error messages *)
calculation = Quiet[DivideByPotentialZero[x, y]];

(* VIOLATION: Quiet on security-sensitive operation *)
Quiet[AuthenticateUser[username, password]];

(* COMPLIANT: Check with proper error logging *)
result = Check[
  riskyDatabaseOperation[],
  (
    Print["ERROR: Database operation failed at ", Now];
    Print["Messages: ", $MessageList];
    LogError["DatabaseFailure", $MessageList];
    $Failed
  )
];

(* COMPLIANT: Check with conditional error handling *)
data = Check[
  ParseUserInput[untrustedData],
  If[$MessageList =!= {},
    LogSecurityEvent["InvalidInput", untrustedData, $MessageList];
    NotifySecurityTeam[];
    $Failed
  ]
];

(* COMPLIANT: Proper message handling *)
output = Check[
  WriteToSecureLocation[file],
  (
    Message[WriteError, file, $MessageList];
    AlertAdmin["Write operation failed"];
    $Failed
  )
];

(* COMPLIANT: Use Check with explicit message checking instead of Quiet *)
calculation = Check[
  result = DivideByPotentialZero[x, y];
  If[$MessageList =!= {},
    LogWarning["Division warning: ", $MessageList]
  ];
  result,
  $Failed
];


(* ====================
   RULE 2: Debug Code in Production (MAJOR) - OWASP A05
   Security Misconfiguration
   ==================== *)

(* VIOLATION: Print statement exposing sensitive data *)
Print["User password: ", userPassword];

(* VIOLATION: Echo revealing internal data *)
Echo[databaseCredentials, "DB Config:"];

(* VIOLATION: PrintTemporary in production *)
PrintTemporary["Processing user: ", userId, " with role: ", userRole];

(* VIOLATION: TracePrint exposing execution flow *)
TracePrint[AuthenticationLogic[username, password]];

(* VIOLATION: Trace revealing internal logic *)
result = Trace[SecurityCheck[userData]];

(* VIOLATION: Monitor showing progress (could leak info) *)
Monitor[
  ProcessSensitiveData[records],
  ProgressIndicator[progress]
];

(* VIOLATION: Debug messages enabled globally *)
$DebugMessages = True;

(* VIOLATION: Multiple debug statements *)
Print["DEBUG: Entering security function"];
result = PerformSecurityCheck[];
Echo[result, "DEBUG: Security check result"];

(* COMPLIANT: Use proper logging with level control *)
If[$DevelopmentMode,
  WriteLog["Authentication attempt for user: " <> username, "DEBUG"]
];

(* COMPLIANT: Conditional logging that won't run in production *)
If[$LogLevel === "VERBOSE",
  LogMessage["Processing record " <> ToString[recordId]]
];

(* COMPLIANT: Use monitoring with proper production flag *)
If[$EnableMonitoring && $DevelopmentEnvironment,
  Monitor[calculation, progressBar]
];

(* COMPLIANT: Production-safe logging *)
LogSecurityEvent["AuthAttempt", {
  "user" -> username,
  "timestamp" -> Now,
  "success" -> success
}];


(* ====================
   Real-World Scenarios
   ==================== *)

(* BAD: Database connection with silent failure *)
conn = Check[
  OpenDatabase[connectionString],
  $Failed  (* No logging! Admin won't know DB is down *)
];

(* GOOD: Database connection with proper error handling *)
conn = Check[
  OpenDatabase[connectionString],
  (
    LogError["DatabaseConnectionFailed", {
      "connectionString" -> connectionString,
      "timestamp" -> Now,
      "messages" -> $MessageList
    }];
    AlertOpsTeam["Critical: Database connection failed"];
    SendEmail["admin@company.com", "DB Connection Alert", $MessageList];
    $Failed
  )
];

(* BAD: File operation with debug output *)
Print["DEBUG: About to write sensitive data"];
Export[filename, sensitiveData];
Echo[filename, "Wrote file:"];

(* GOOD: File operation with production logging *)
If[$LogLevel >= "INFO",
  WriteLog["File write operation initiated", "INFO"]
];
result = Check[
  Export[filename, sanitizedData],
  (
    LogError["FileWriteFailed", filename, $MessageList];
    $Failed
  )
];
If[result =!= $Failed && $LogLevel >= "INFO",
  WriteLog["File write successful", "INFO"]
];

(* BAD: Authentication with debugging *)
Print["Authenticating user: ", username];
Print["Password hash: ", Hash[password, "SHA256"]];
result = AuthenticateUser[username, password];
Echo[result, "Auth result:"];

(* GOOD: Authentication with secure logging *)
auditID = GenerateAuditID[];
LogSecurityEvent["AuthenticationAttempt", {
  "auditID" -> auditID,
  "username" -> username,
  "timestamp" -> Now,
  "ipAddress" -> $RemoteIPAddress
}];

result = Check[
  AuthenticateUser[username, password],
  (
    LogSecurityEvent["AuthenticationFailure", {
      "auditID" -> auditID,
      "username" -> username,
      "reason" -> $MessageList
    }];
    IncrementFailedAttempts[username];
    $Failed
  )
];

If[result =!= $Failed,
  LogSecurityEvent["AuthenticationSuccess", {
    "auditID" -> auditID,
    "username" -> username,
    "sessionID" -> result
  }]
];


(* ====================
   Summary
   ==================== *)

(* With these 2 new rules, the plugin now has:
   - 8 Code Smell rules (6 original + 2 new)
   - 8 Security Vulnerability rules
   = 16 total rules + CPD duplication detection

   OWASP Top 10 2021 Coverage: 9 of 10 categories
   ✓ A01 - Broken Access Control (Path Traversal)
   ✓ A02 - Cryptographic Failures (Weak Cryptography)
   ✓ A03 - Injection (Command, SQL, Code Injection)
   ✗ A04 - Insecure Design (too abstract)
   ✓ A05 - Security Misconfiguration (Debug Code) [IMPROVED]
   ✗ A06 - Vulnerable Components (no package manager)
   ✓ A07 - Identification/Auth Failures (Hardcoded Credentials)
   ✓ A08 - Software/Data Integrity (Insecure Deserialization)
   ✓ A09 - Logging/Monitoring Failures (Empty Catch Blocks) [IMPROVED]
   ✓ A10 - Server-Side Request Forgery (SSRF)

   Coverage: 9/10 (90%)
*)
