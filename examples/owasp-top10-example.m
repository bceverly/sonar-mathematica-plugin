(* Example file demonstrating OWASP Top 10 security rules *)
(* These 3 additional rules complete the OWASP Top 10 2021 coverage *)

(* ====================
   RULE 1: Weak Cryptography (CRITICAL) - OWASP A02
   ==================== *)

(* VIOLATION: Using MD5 hash algorithm *)
fileHash = Hash[fileData, "MD5"];

(* VIOLATION: Using SHA1 hash algorithm *)
signature = Hash[message, "SHA1"];
altSignature = Hash[document, "SHA-1"];

(* VIOLATION: Using Random[] for security tokens *)
sessionToken = ToString[Random[Integer, {10^20, 10^21}]];

(* VIOLATION: Using Random[] for encryption keys *)
encryptionKey = Table[Random[], {16}];

(* COMPLIANT: Use SHA256 or SHA512 *)
fileHashGood = Hash[fileData, "SHA256"];
signatureGood = Hash[message, "SHA512"];

(* COMPLIANT: Use RandomInteger for security purposes *)
sessionTokenGood = IntegerString[RandomInteger[{10^20, 10^21}], 16];
encryptionKeyGood = RandomInteger[{0, 255}, 32];


(* ====================
   RULE 2: Server-Side Request Forgery (CRITICAL) - OWASP A10
   ==================== *)

(* VIOLATION: URLFetch with concatenated user input *)
userEndpoint = "api/users"; (* Could be user-controlled *)
response = URLFetch["https://api.example.com/" <> userEndpoint];

(* VIOLATION: URLRead with concatenated path *)
baseURL = "https://internal.company.com/";
data = URLRead[baseURL <> userPath]; (* User could supply: "../admin/secrets" *)

(* VIOLATION: Import with concatenated URL *)
domain = GetUserInput[]; (* User input *)
jsonData = Import["https://" <> domain <> "/data.json"];
(* User could supply: "169.254.169.254/latest/meta-data" (AWS metadata) *)

(* VIOLATION: ServiceExecute with user-controlled URL *)
serviceResult = ServiceExecute[webService, "Query", {"url" -> userURL}];

(* COMPLIANT: Use whitelist of allowed endpoints *)
allowedEndpoints = {"users", "posts", "comments", "products"};
If[MemberQ[allowedEndpoints, userEndpoint],
  URLFetch["https://api.example.com/" <> userEndpoint],
  $Failed
];

(* COMPLIANT: Validate URL domain against whitelist *)
allowedDomains = {"trusted-api.com", "public-data.example.com"};
urlDomain = URLParse[userURL]["Domain"];
If[MemberQ[allowedDomains, urlDomain],
  URLRead[userURL],
  $Failed
];

(* COMPLIANT: Use StringMatchQ to validate URL pattern *)
If[StringMatchQ[userURL, "https://trusted-domain.com/*"],
  Import[userURL],
  $Failed
];


(* ====================
   RULE 3: Insecure Deserialization (CRITICAL) - OWASP A08
   ==================== *)

(* VIOLATION: Importing MX file from user input *)
userFile = "uploaded_data.mx"; (* User-uploaded file *)
data = Import[userFile, "MX"]; (* MX files can execute arbitrary code! *)

(* VIOLATION: Importing WDX file *)
dataset = Import[uploadedFile, "WDX"]; (* WDX can also execute code *)

(* VIOLATION: Get[] with user-controlled path *)
packagePath = "/packages/" <> userPackageName <> ".m";
Get[packagePath]; (* Executes code from file *)

(* VIOLATION: Get[] from URL with concatenation *)
Get["http://" <> userDomain <> "/package.m"];
(* User could supply: "attacker.com/malicious" *)

(* VIOLATION: Get[] from URL *)
Get["https://untrusted-source.com/library.m"];

(* VIOLATION: Loading expression from URL *)
expr = ToExpression[Import[untrustedURL, "String"]];
(* Could execute: DeleteFile["*"], or any malicious code *)

(* COMPLIANT: Use safe formats for untrusted data *)
dataGood = Import[userFile, "JSON"]; (* JSON is data-only, no code execution *)
dataGood2 = Import[userFile, "CSV"];
dataGood3 = Import[userFile, "XML"];

(* COMPLIANT: Validate against whitelist before loading *)
trustedPackages = {
  "/usr/local/mathematica/packages/SafePackage.m",
  "/opt/company/lib/TrustedLib.m"
};
If[MemberQ[trustedPackages, packagePath],
  Get[packagePath],
  $Failed
];

(* COMPLIANT: Verify file integrity with checksums *)
expectedHash = "a3c5e..."; (* Known good hash *)
actualHash = Hash[Import[packageFile, "String"], "SHA256"];
If[IntegerString[actualHash, 16] === expectedHash,
  Get[packageFile],
  Print["Package integrity check failed!"]
];

(* COMPLIANT: Use FileNameTake to prevent path traversal before loading *)
safePackageName = FileNameTake[userPackageName];
safePackagePath = FileNameJoin[{"/safe/packages/", safePackageName <> ".m"}];
If[FileExistsQ[safePackagePath] && StringStartsQ[safePackagePath, "/safe/packages/"],
  Get[safePackagePath],
  $Failed
];


(* ====================
   Summary: OWASP Top 10 2021 Coverage
   ==================== *)

(* With these 3 new rules, the plugin now covers 8 of 10 OWASP categories:

   ✓ A01 - Broken Access Control (Path Traversal)
   ✓ A02 - Cryptographic Failures (Weak Cryptography) [NEW]
   ✓ A03 - Injection (Command, SQL, Code Injection)
   ✗ A04 - Insecure Design (too abstract for static analysis)
   ✗ A05 - Security Misconfiguration (partially covered)
   ✗ A06 - Vulnerable Components (no package manager in Mathematica)
   ✓ A07 - Identification and Authentication Failures (Hardcoded Credentials)
   ✓ A08 - Software and Data Integrity Failures (Insecure Deserialization) [NEW]
   ✗ A09 - Security Logging Failures (partially covered)
   ✓ A10 - Server-Side Request Forgery (SSRF) [NEW]
*)
