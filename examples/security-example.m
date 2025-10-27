(* Example file demonstrating all 5 security vulnerability rules *)

(* ====================
   RULE 1: Hardcoded Credentials (BLOCKER)
   ==================== *)

(* VIOLATION: Hardcoded API key *)
apiKey = "sk_live_XXXXXXXXXXXXXXXXXXXXXXXXXXXX";  (* Example only - not a real key *)

(* VIOLATION: Hardcoded password *)
dbPassword = "SuperSecret123!";

(* VIOLATION: AWS credentials *)
awsAccessKey = "AKIAIOSFODNN7EXAMPLE";  (* AWS example from documentation *)
awsSecretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";  (* AWS example *)

(* VIOLATION: Auth token *)
authToken = "ghp_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";  (* Example only *)

(* COMPLIANT: Load from environment or secure configuration *)
apiKeyGood = Environment["API_KEY"];
passwordGood = Import["!get-secret database-password", "String"];


(* ====================
   RULE 2: Command Injection (CRITICAL)
   ==================== *)

(* VIOLATION: Constructing shell command with user input *)
userFile = "somefile.txt"; (* Imagine this comes from user *)
Run["cat " <> userFile]; (* User could inject: "file.txt; rm -rf /" *)

(* VIOLATION: Using sh with concatenated commands *)
searchTerm = GetUserInput[]; (* User input *)
RunProcess[{"sh", "-c", "grep " <> searchTerm <> " data.txt"}];

(* VIOLATION: Import with shell execution *)
command = "ls -la"; (* Could be user-controlled *)
Import["!" <> command, "Text"];

(* COMPLIANT: Use array form of RunProcess (safer) *)
RunProcess[{"grep", searchTerm, "data.txt"}]; (* Arguments are separate *)

(* COMPLIANT: Use Mathematica APIs instead of shell *)
files = FileNames["*.txt"];


(* ====================
   RULE 3: SQL Injection (CRITICAL)
   ==================== *)

(* VIOLATION: Building SQL with string concatenation *)
userId = "123"; (* User input *)
SQLExecute[conn, "SELECT * FROM users WHERE id=" <> userId];

(* VIOLATION: DELETE with concatenated user input *)
userName = GetUserInput[];
SQLSelect[conn, "DELETE FROM data WHERE name='" <> userName <> "'"];

(* User could supply: "'; DROP TABLE users; --" *)

(* COMPLIANT: Use parameterized queries *)
SQLExecute[conn, "SELECT * FROM users WHERE id=?", {userId}];

(* COMPLIANT: Use SQLWhere for safe filtering *)
SQLSelect[conn, SQLColumn["id"], SQLTable["users"],
  SQLWhere["id" == userId]];


(* ====================
   RULE 4: Code Injection (CRITICAL)
   ==================== *)

(* VIOLATION: Evaluating user input as code *)
userFormula = "x^2 + 2*x + 1"; (* From web form *)
result = ToExpression[userFormula];
(* User could inject: "DeleteFile[\"important.txt\"]" *)

(* VIOLATION: Using Evaluate on external data *)
formulaFromAPI = FetchFromAPI["formula"];
Evaluate[StringToExpression[formulaFromAPI]];

(* COMPLIANT: Parse and validate first *)
safePattern = RegularExpression["^[x0-9+\\-*/^()\\s]+$"];
If[StringMatchQ[userFormula, safePattern],
  ToExpression[userFormula],
  $Failed
];

(* COMPLIANT: Use a whitelist of allowed functions *)
allowedFunctions = {"Sin", "Cos", "Tan", "Log", "Exp"};
If[MemberQ[allowedFunctions, Head[ToExpression[input]]],
  ToExpression[input],
  $Failed
];


(* ====================
   RULE 5: Path Traversal (HIGH)
   ==================== *)

(* VIOLATION: Constructing file paths from user input *)
baseDirectory = "/app/data/";
requestedFile = "../../etc/passwd"; (* User input *)
data = Import[baseDirectory <> requestedFile]; (* Accesses /etc/passwd! *)

(* VIOLATION: Export with user-controlled filename *)
outputDir = "/output/";
userFilename = GetUserInput[];
Export[outputDir <> userFilename, data]; (* User could write anywhere *)

(* VIOLATION: Opening files with concatenated paths *)
OpenRead["/safe/path/" <> userProvidedName];

(* COMPLIANT: Use FileNameTake to get only the basename *)
safeFileName = FileNameTake[requestedFile]; (* Returns "passwd", not "../../etc/passwd" *)
safePath = FileNameJoin[{baseDirectory, safeFileName}];
data = Import[safePath];

(* COMPLIANT: Validate path stays within base directory *)
fullPath = FileNameJoin[{baseDirectory, requestedFile}];
If[StringStartsQ[fullPath, baseDirectory],
  Import[fullPath],
  $Failed (* Reject if path escapes base directory *)
];

(* COMPLIANT: Use a whitelist of allowed filenames *)
allowedFiles = {"data1.txt", "data2.txt", "config.json"};
If[MemberQ[allowedFiles, requestedFile],
  Import[FileNameJoin[{baseDirectory, requestedFile}]],
  $Failed
];


(* ====================
   Additional Security Best Practices
   ==================== *)

(* Validate and sanitize all external input *)
ValidateInput[input_String] := Module[{},
  (* Check length *)
  If[StringLength[input] > 1000, Return[$Failed]];

  (* Check for suspicious characters *)
  If[StringContainsQ[input, ".." | "/" | "\\"], Return[$Failed]];

  (* Check against whitelist pattern *)
  If[!StringMatchQ[input, RegularExpression["^[a-zA-Z0-9_-]+$"]],
    Return[$Failed]
  ];

  input
];

(* Use secure random for security-sensitive operations *)
(* DON'T use Random[] for security! *)
secureToken = IntegerString[RandomInteger[{10^20, 10^21 - 1}], 16];

(* Log security events *)
LogSecurityEvent[event_] := Module[{timestamp},
  timestamp = DateString[];
  PutAppend[timestamp <> ": " <> event, "/var/log/security.log"];
];
