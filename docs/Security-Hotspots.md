# Security Hotspots

**Security Hotspots** are security-sensitive pieces of code that require **manual review**. Unlike Vulnerabilities (which are definite bugs), Hotspots highlight areas where security could be weak if not implemented correctly.

**See also:** [[Security Vulnerabilities]] | [[Best Practices]] | [[Rule Catalog]]

---

## What are Security Hotspots?

Security Hotspots are **not bugs** - they're areas of code that:
- Use security-sensitive APIs
- Could be vulnerable if misconfigured
- Require security expertise to review
- Should be verified by security team

**Your job:** Review each hotspot and confirm it's secure, then mark as "Safe" or "Fix".

---

## 29 Security Hotspot Rules

### Authentication & Authorization (7 rules)

#### 1. Weak Authentication Mechanisms
**Rule:** `WeakAuthentication`
**Review:** Does this authentication method provide adequate security?

**Sensitive code:**
```mathematica
(* Simple password check - is this enough? *)
AuthenticateUser[user_, pass_] := user == "admin" && pass == "password123"
```

**Security checklist:**
- [ ] Uses strong hashing (bcrypt, Argon2)?
- [ ] Implements rate limiting?
- [ ] Protects against timing attacks?
- [ ] Enforces password complexity?
- [ ] Uses multi-factor authentication?

**Safe implementation:**
```mathematica
(* Secure: Use proper hashing *)
AuthenticateUser[user_, pass_] := Module[{stored, hash},
  stored = LoadHashedPassword[user];
  hash = Hash[pass, "bcrypt"];  (* Use proper library *)
  SlowEquals[hash, stored]  (* Timing-safe comparison *)
]
```

#### 2. Missing Authorization Checks
Check every protected resource has authorization.

#### 3-7. Additional Auth Rules
- Insecure session management
- Missing role validation
- Privilege escalation risks
- Account enumeration
- Credential management

---

###Cryptography (7 rules)

#### Weak Hash Functions (MD5, SHA-1)
**Rule:** `WeakHashing`

```mathematica
(* REVIEW NEEDED *)
hash = Hash[password, "MD5"]  (* MD5 is broken! *)

(* SAFE *)
hash = Hash[password, "SHA256"]  (* Minimum SHA-256 *)
```

#### Weak Encryption Algorithms
Avoid: DES, RC4, ECB mode

Use: AES-256-GCM, ChaCha20-Poly1305

---

### Network Security (6 rules)

#### HTTP Without TLS
```mathematica
(* REVIEW: Should this use HTTPS? *)
URLRead["http://api.internal.com/data"]

(* SAFE: Use HTTPS *)
URLRead["https://api.internal.com/data"]
```

#### Disabled Certificate Validation
Never disable cert validation in production!

---

### Data Protection (3 rules)

#### File Upload Without Validation
Validate: file type, size, content

#### Public Network Binding
Review if service should be public

#### Sensitive Data in Logs
Never log passwords, tokens, PII

---

### Input Validation (6 rules)

All user inputs must be validated before use.

---

## How to Review Hotspots

### In SonarQube UI

1. Go to **Security Hotspots** tab
2. Click a hotspot to review
3. Read the security concern
4. Check the code implementation
5. Choose:
   - **Safe** - Confirmed secure
   - **Fixed** - Made changes
   - **Won't Fix** - False positive

### Review Checklist

For each hotspot:
- [ ] Understand the security risk
- [ ] Check implementation details
- [ ] Verify secure coding practices
- [ ] Consult security guidelines
- [ ] Test security controls
- [ ] Document review decision

---

## Complete Hotspot List

| Category | Rule | What to Review |
|----------|------|----------------|
| **Auth** | Weak Authentication | Hash algorithm, rate limiting |
| **Auth** | Missing Authorization | Permission checks |
| **Auth** | Session Management | Timeout, secure flags |
| **Auth** | Role Validation | RBAC implementation |
| **Auth** | Privilege Escalation | Vertical/horizontal checks |
| **Auth** | Account Enumeration | Timing attacks, error messages |
| **Auth** | Credential Storage | Encryption at rest |
| **Crypto** | Weak Hashing | MD5, SHA-1 usage |
| **Crypto** | Weak Encryption | DES, RC4, ECB mode |
| **Crypto** | Short Keys | Key length >= 2048 bits |
| **Crypto** | Hardcoded IV | Random IV generation |
| **Crypto** | Weak Random | Cryptographic PRNG |
| **Crypto** | Custom Crypto | Use standard libraries |
| **Crypto** | Insecure Protocols | SSLv3, TLS 1.0/1.1 |
| **Network** | HTTP Without TLS | HTTPS enforcement |
| **Network** | Cert Validation Disabled | Production certs |
| **Network** | Public Binding | Network exposure |
| **Network** | Unrestricted CORS | Origin validation |
| **Network** | Open Redirects | URL validation |
| **Network** | DNS Rebinding | Host validation |
| **Data** | File Uploads | Type, size, content validation |
| **Data** | Sensitive in Logs | PII, credentials |
| **Data** | Unencrypted Storage | Encryption at rest |
| **Input** | Missing Validation | Whitelist validation |
| **Input** | Regex Complexity | DoS potential |
| **Input** | Mass Assignment | Field whitelisting |
| **Input** | XML Parsing | DTD disabled |
| **Input** | Deserialization | Safe formats |
| **Input** | File Path Construction | Path traversal |

---

See [[Rule Catalog]] for detailed descriptions of all 29 rules.
