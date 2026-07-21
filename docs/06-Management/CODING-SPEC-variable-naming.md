# SPEC-CODING-001: Variable Naming Convention — Project-Wide Standard

| Field | Value |
|---|---|
| **Spec ID** | SPEC-CODING-001 |
| **Version** | 2.0 |
| **Status** | ACTIVE |
| **Date** | 2026-07-17 |
| **Replaces** | SPEC-CODING-001 v1.0 |
| **Author** | Engineering Team |
| **Scope** | All source code: `apps/frontend` (JS/JSX) · `apps/backend` (Java/Spring Boot) |
| **Enforcement** | MUST — PR merge blocker |

---

## 1. Context & Goal

### 1.1 Background

The Japanese Skill Practice Platform is a full-stack monorepo consisting of:

- **Frontend:** React 18 + Vite, located at `apps/frontend/src/`
  - Layers: `pages/`, `components/`, `api/`, `store/slices/`, `hooks/`, `utils/`
- **Backend:** Java 17 + Spring Boot 3, located at `apps/backend/src/main/java/com/jlpt/`
  - Layers: `feature/*/controller`, `feature/*/service`, `feature/*/repository`, shared `entity`

A codebase-wide static scan conducted on **2026-07-17** identified pervasive single-character and non-descriptive abbreviated identifier names across **both layers** of the stack. These names force every reader to perform constant mental context-switching to decode intent, significantly raising cognitive load during code review, onboarding, and debugging.

### 1.2 Evidence — Violations Found in Production Code

#### Frontend (JavaScript / JSX)

| File | Line | Violation | Interpretation Required |
|---|---|---|---|
| `Reading.jsx` | 97 | `const q = byQuestion.get(r.questionId)` | `q` = question? query? |
| `Reading.jsx` | 289 | `results.items.map((r, idx) =>` | `r` = result? reading? record? |
| `QuizPage.jsx` | 131 | `const q = questions[currentIdx]` | `q` = question? |
| `Grammar.jsx` | 62 | `const q = search.trim().toLowerCase()` | `q` = query? question? |
| `StaffContent.jsx` | 181 | `const v = e.target.value` | `v` = value? `e` = event? |
| `ContentFormModal.jsx` | 605 | `const v = e.target.value` | `v` = value? |
| `GradingPanel.jsx` | 67 | `const v = parseFloat(manualScore)` | `v` = value? |
| `Dictionary.jsx` | 33 | `const v = JSON.parse(localStorage...)` | `v` = value? |
| `UserModals.jsx` | 82, 89 | `const e = {}` / `const e = validate()` | `e` = error? event? |
| `validation.js` | 45, 55, 57 | `requiredError(v,...)` / `const s = String(v)` / `const n = Number(s)` | `v`, `s`, `n` all opaque |
| `studentService.js` | 241, 248 | `searchDictionary(q, jlptLevel, type)` | `q` = query? |
| `KanjiPractice.jsx` | 161, 171 | `onyomiList.map((r, i) =>` | `r` = reading? |
| `VocabularyList.jsx` | 25, 89, 152, 172 | `useAppSelector((s) => s.auth)` / `const s = {...prev}` / `topics.map((t) =>` | `s`, `t` opaque |
| `VocabFlashcardSession.jsx` | 27, 86, 106 | `(s) => s.auth` / `.filter((c) =>` / `setCorrectCnt((c) =>` | `s`, `c` opaque |
| `StaffQuestions.jsx` | 111, 117, 166, 180 | `onEdit((q) =>` / `onNewVersion((q) =>` / `onSubmit((q) =>` | `q` = question? |
| `SupportTickets.jsx` | 123 | `tickets.map((t) =>` | `t` = ticket? |
| `StaffStudents.jsx` | 86, 214 | `.then((d) => setDetail(d))` / `students.map((s) =>` | `d`, `s` opaque |
| `date.js` | 6, 19 | `const d = new Date(isoStr)` | `d` = date? |
| `kanjiLookup.js` | 35, 43, 67 | `isVowel = (c) =>` / `const c = s[i]` / `kataToHira = (s) =>` | `c`, `s` opaque |

#### Backend (Java / Spring Boot)

| File | Line | Violation | Interpretation Required |
|---|---|---|---|
| `AdminUserRepository.java` | 37 | `@Param("q") String q` | `q` = search keyword? |
| `AdminUserService.java` | 64 | `String q, String status, ...` | `q` = query? |
| `AdminController.java` | 50 | `@RequestParam String q` | `q` = query? |
| `AdminAuditLogService.java` | 22 | `String t = targetTable ...` | `t` = table? |
| `StaffUser.java` | 76, 92 | `private final String v` / `StaffRole(String v)` | `v` = value string? |
| `Ticket.java` | 66, 83 | `Priority(String v)` / `TicketStatus(String v)` | `v` = enum value label? |
| `StudentUser.java` | 113, 129 | `StudentStatus(String v)` / `OauthProvider(String v)` | `v` = value? |
| `StudentContentProgress.java` | 60, 75 | `ContentType(String v)` / `ProgressStatus(String v)` | `v` = value? |
| `SupportTicketService.java` | 270 | `String q, int page, int size` | `q` = keyword? |
| `StaffSupportController.java` | 44 | `@RequestParam String q` | `q` = query? |
| `StaffStudentService.java` | 51 | `String q = "%" + search.trim() + "%"` | `q` = JPQL query? SQL like? |
| `StaffQuizService.java` | 307 | `String s = requestedStatus.trim().toLowerCase()` | `s` = status? string? |
| `StaffExamService.java` | 321 | `String s = requestedStatus.trim()...` | `s` = status? |
| `StaffQuestionService.java` | 21 | `String q, String skill, ...` | `q` = query? |
| `LearningContentService.java` | 41–55 | `String q, String jlptLevel, ...` | `q` = query? |
| `KanaServiceImpl.java` | 78 | `String r = romaji.toLowerCase()` | `r` = romaji? result? |
| `KanjiWritingServiceImpl.java` | 128 | `int n = s1.size(), m = s2.size()` | `n`, `m` = sizes? |
| `EmailService.java` | 284 | `private String truncate(String s)` | `s` = source? string? |
| `DevDataSeeder.java` | 102 | `String a, String b, String c, String d` | all four are answer options |
| `GrammarPointRepository.java` | 25 | `@Param("q") String q` | `q` = query? |

### 1.3 Goal

Establish a **project-wide, technology-agnostic** naming standard that makes every identifier self-documenting. A developer reading any file for the first time must be able to understand the purpose and content of any variable **without relying on comments, IDE hover-tooltips, or surrounding context**.

> **Core Principle:** Code is read far more often than it is written. The incremental cost of typing a descriptive name is always lower than the cumulative cost of re-inferring ambiguous names across every future read, review, and debug session.

---

## 2. Actors

| Actor | Role | Responsibility under this spec |
|---|---|---|
| **Developer (Frontend)** | JS/JSX code author | Applies naming rules when writing or modifying any `.js`, `.jsx`, `.ts`, `.tsx` file |
| **Developer (Backend)** | Java code author | Applies naming rules when writing or modifying any `.java` file |
| **Code Reviewer** | PR gatekeeper | Rejects any PR containing P0 violations before approval; flags P1 violations with required fix or written justification |
| **Tech Lead** | Standard owner | Maintains and evolves this spec; issues binding rulings on edge cases within one business day |
| **CI Pipeline** | Automated enforcer | Runs linter/checkstyle rules derived from this spec on every push; fails the build on P0 violations |
| **New Team Member** | Onboardee | Studies this spec during onboarding; raises questions to Tech Lead rather than introducing exceptions |

---

## 3. Functional Requirements

> **EARS Patterns used in this section:**
> - **Ubiquitous:** `The [actor] SHALL [action]`
> - **Event-driven:** `WHEN [trigger], the [actor] SHALL [action]`
> - **State-driven:** `WHILE [state], the [actor] SHALL [action]`
> - **Unwanted behavior:** `IF [unwanted condition], THEN the [actor] SHALL [action]`
> - **Complex:** `WHILE [state], WHEN [trigger], the [actor] SHALL [action]`

---

### FR-VAR-01 · No Single-Character Identifiers

**[P0 — MUST]**

> The developer SHALL NOT declare any variable, parameter, field, or callback argument using a single-character name, unless that character appears in the approved exception list defined in FR-VAR-05.

**WHEN** a callback function is passed to `.map()`, `.filter()`, `.reduce()`, or `.forEach()`, **the developer SHALL** name the element argument to reflect the semantic type of the collection element, not use a single letter.

```js
// ❌ VIOLATES FR-VAR-01
questions.map((q, i) => ...)
topics.map((t) => ...)
students.map((s) => ...)
tickets.map((t) => ...)
results.items.map((r, idx) => ...)

// ✅ SATISFIES FR-VAR-01
questions.map((question, questionIndex) => ...)
topics.map((topic) => ...)
students.map((student) => ...)
tickets.map((ticket) => ...)
results.items.map((resultItem, itemIndex) => ...)
```

**WHEN** a Redux selector is written with `useAppSelector`, **the developer SHALL** name the state parameter to reflect the slice being selected.

```js
// ❌ VIOLATES FR-VAR-01
const { user } = useAppSelector((s) => s.auth);
const { vocabHome } = useAppSelector((s) => s.student);

// ✅ SATISFIES FR-VAR-01
const { user } = useAppSelector((rootState) => rootState.auth);
const { vocabHome } = useAppSelector((rootState) => rootState.student);
```

**WHEN** a Java method or constructor is declared with parameters, **the developer SHALL** give every parameter a descriptive name that reflects its role.

```java
// ❌ VIOLATES FR-VAR-01 — DevDataSeeder.java:102
record Q(String text, String a, String b, String c, String d, String correct, String explanation) {}

// ✅ SATISFIES FR-VAR-01
record QuizQuestionSeed(
    String questionText,
    String optionA,
    String optionB,
    String optionC,
    String optionD,
    String correctOption,
    String explanation
) {}
```

---

### FR-VAR-02 · No Opaque Abbreviations

**[P1 — SHOULD]**

> The developer SHOULD NOT use abbreviated identifiers that are not universally established industry symbols, as listed in FR-VAR-05.

**IF** a developer uses an abbreviated name not present in the approved list, **THEN the developer SHALL** provide a written justification in the PR description and open a ticket to add the symbol to FR-VAR-05 if it is genuinely reusable.

#### Common violations and their compliant replacements

**Frontend (JS/JSX):**

```js
// ❌                              ✅
const usr = await getUser()        const currentUser = await getUser()
const cfg = loadConfig()           const appConfig = loadConfig()
const res = await fetch(url)       const apiResponse = await fetch(url)
const cb = () => {}                const onSuccessCallback = () => {}
const fn = () => {}                const handleFormSubmit = () => {}
const arr = []                     const lessonList = []
const obj = {}                     const userProfile = {}
const d = new Date(isoStr)         const parsedDate = new Date(isoStr)
const s = requestedStatus.trim()   const normalizedStatus = requestedStatus.trim()
const n = Number(s)                const parsedNumber = Number(trimmedValue)
```

**Backend (Java):**

```java
// ❌                                        ✅
String q = "%" + search.trim() + "%"         String searchPattern = "%" + search.trim() + "%"
String r = romaji.toLowerCase()              String normalizedRomaji = romaji.toLowerCase()
String t = targetTable ...                   String resolvedTargetTable = targetTable ...
String s = requestedStatus.trim()            String normalizedStatus = requestedStatus.trim()
int n = s1.size(), m = s2.size()             int strokeCount1 = s1.size(), strokeCount2 = s2.size()
private String truncate(String s)            private String truncate(String inputText)
```

---

### FR-VAR-03 · Search / Query Parameter Naming

**[P0 — MUST]**

> WHEN a method, function, REST controller endpoint, or JPQL repository method declares a parameter representing a search keyword or query string, the developer SHALL name it `searchQuery`, `searchKeyword`, or an equivalently descriptive domain-specific name. The name `q` is forbidden except in `@Param` JPQL binding where the JPQL variable name is already `q` (see FR-VAR-05).

```java
// ❌ VIOLATES FR-VAR-03 — present in AdminController, StaffSupportController,
//    StaffQuestionController, StaffLearningContentController, etc.
@RequestParam(required = false) String q

// ✅ SATISFIES FR-VAR-03
@RequestParam(required = false) String searchKeyword
// Then pass to service: service.list(searchKeyword, ...)
```

```java
// ❌ VIOLATES FR-VAR-03 — present in LearningContentService.java:41
List<...> listLessons(String q, String jlptLevel, ...);

// ✅ SATISFIES FR-VAR-03
List<...> listLessons(String searchKeyword, String jlptLevel, ...);
```

```js
// ❌ VIOLATES FR-VAR-03 — studentService.js:241
export async function searchDictionary(q, jlptLevel, type) {}

// ✅ SATISFIES FR-VAR-03
export async function searchDictionary(searchQuery, jlptLevel, entryType) {}
```

---

### FR-VAR-04 · Naming Conventions by Identifier Category

**[P0 — MUST for boolean prefix; P1 — SHOULD for others]**

The developer SHALL follow the category-specific patterns below for all identifiers across both stacks.

#### 3.4.1 Regular Variables — full descriptive noun phrase

```js
// ✅ Frontend
const currentLessonIndex = 0;
const selectedJlptLevel = 'N3';
const vocabularySearchQuery = '';
const cachedHistory = JSON.parse(localStorage.getItem(HISTORY_KEY));
const parsedScore = parseFloat(manualScore);
const inputValue = event.target.value;
```

```java
// ✅ Backend
String searchPattern = "%" + search.trim() + "%";
String normalizedStatus = requestedStatus.trim().toLowerCase();
int strokeCount1 = firstStrokeList.size();
int strokeCount2 = secondStrokeList.size();
```

#### 3.4.2 Boolean Identifiers — MUST start with `is`, `has`, `can`, `should`

**[P0 — MUST]**

> WHEN a variable or field holds a boolean value, the developer SHALL prefix its name with `is`, `has`, `can`, or `should`.

```js
// ✅ Frontend
const isAuthenticated = false;
const hasCompletedLesson = true;
const canSubmitAnswer = false;
const shouldShowHint = true;

// ❌ VIOLATES FR-VAR-04
const authenticated = false;
const completedLesson = true;
```

```java
// ✅ Backend
boolean isActiveUser = user.getStatus() == ACTIVE;
boolean hasValidToken = tokenService.validate(token);

// ❌ VIOLATES FR-VAR-04
boolean active = user.getStatus() == ACTIVE;
```

#### 3.4.3 Functions and Methods — start with action verb

```js
// ✅ Frontend
function fetchUserProgress() {}
function handleQuestionSubmit(event) {}
function validateLoginForm(formData) {}
const onAnswerSelect = (selectedOption) => {};
const onLessonComplete = (lessonResult) => {};
```

#### 3.4.4 Java Enum Constructor Parameters — use `displayValue` or domain name, not `v`

**[P0 — MUST]**

> WHEN a Java enum declares a constructor that accepts a String label or display value, the developer SHALL name the parameter `displayValue`, `label`, `code`, or another descriptive domain term. The name `v` is forbidden.

```java
// ❌ VIOLATES FR-VAR-04 — present in StaffUser.java, Ticket.java,
//    StudentUser.java, StudentContentProgress.java
enum StaffRole {
    TEACHER("teacher"), ADMIN("admin");
    private final String v;
    StaffRole(String v) { this.v = v; }
}

// ✅ SATISFIES FR-VAR-04
enum StaffRole {
    TEACHER("teacher"), ADMIN("admin");
    private final String displayValue;
    StaffRole(String displayValue) { this.displayValue = displayValue; }
    public String getDisplayValue() { return displayValue; }
}
```

#### 3.4.5 Async / Promise Return Variables — prefix with domain context

```js
// ✅
const userProgressData = await fetchUserProgress();
const dictionarySearchResult = await searchDictionary(searchQuery, jlptLevel, entryType);
const kanjiDetailResponse = await getKanjiDetail(kanjiId);
```

#### 3.4.6 Error Variables in `catch` Blocks — use `error` or domain-specific name

**[P1 — SHOULD]**

> WHEN writing a `catch` block, the developer SHOULD name the caught exception `error`, or a domain-specific name such as `fetchError`, `validationError`, `resetProgressError`. The name `e` is forbidden in this context.

```js
// ❌ VIOLATES FR-VAR-04 — KanjiList.jsx:133
} catch (e) {
  toastError(getErrorMessage(e, 'Lỗi khi reset tiến độ.'));
}

// ✅ SATISFIES FR-VAR-04
} catch (resetProgressError) {
  toastError(getErrorMessage(resetProgressError, 'Lỗi khi reset tiến độ.'));
}
```

```java
// ❌ VIOLATES FR-VAR-04
} catch (Exception e) { log.error("Failed", e); }

// ✅ SATISFIES FR-VAR-04
} catch (Exception processingException) { log.error("Failed", processingException); }
```

#### 3.4.7 setState Updater Callbacks — name the previous state argument

**[P1 — SHOULD]**

> WHEN a React `setState` updater callback is written, the developer SHOULD name the previous-state argument descriptively rather than `s`, `d`, or `r`.

```js
// ❌ VIOLATES FR-VAR-04 — VocabFlashcardSession.jsx:262, SupportTicketDetail.jsx:63
onClick={() => setRevealed((r) => !r)}
setDetail((d) => ({ ...d, replies: [...d.replies, reply] }))

// ✅ SATISFIES FR-VAR-04
onClick={() => setRevealed((previousRevealedState) => !previousRevealedState)}
setDetail((previousDetail) => ({ ...previousDetail, replies: [...previousDetail.replies, reply] }))
```

---

### FR-VAR-05 · Approved Exception List (Exhaustive)

**[P0 — MUST comply with scope restrictions]**

> IF a developer uses a short identifier, THEN it MUST be one of the identifiers listed below AND used within its allowed scope. Any other short identifier is a P0 violation.

| Identifier | Language | Allowed Scope | Example |
|---|---|---|---|
| `i`, `j`, `k` | JS + Java | Counter variable in traditional `for` loop header only | `for (int i = 0; i < list.size(); i++)` |
| `_` | JS | Intentionally ignored callback parameter | `array.map((_, index) => index)` |
| `id` | JS + Java | Entity identity field shorthand (universally understood) | `const userId = user.id` |
| `url` | JS + Java | W3C-standard acronym | `String apiUrl = config.getBaseUrl()` |
| `idx` | JS | Second (index) argument of an inline `.map()` / `.filter()` only | `items.map((item, idx) => ...)` |
| `@Param("q")` | Java | JPQL `@Param` binding only — the SQL/JPQL variable name is `q`; the Java method parameter must still be renamed | See FR-VAR-03 |

> **Note:** Even approved exceptions should use a more descriptive name whenever context allows it without verbosity.

---

## 4. Non-Functional Requirements

**NFR-01 — Readability [MUST]**

> The naming standard SHALL ensure that any identifier's purpose can be determined by a developer reading the file for the first time, without relying on IDE hover-tooltips, inline comments, or surrounding code blocks.

**NFR-02 — Cross-Stack Consistency [SHOULD]**

> WHILE a feature spans both the frontend and backend layers, the naming of conceptually equivalent variables (e.g., `searchKeyword` in the Java controller and `searchQuery` in the JS service call) SHOULD use the same root noun to reduce cognitive mapping effort when tracing a feature end-to-end.

**NFR-03 — CI Enforcement Speed [SHOULD]**

> WHEN a developer pushes a commit to any branch, the CI pipeline SHOULD report P0 naming violations within 3 minutes to enable fast local correction before human review begins.

**NFR-04 — Low Refactor Risk [SHOULD]**

> WHEN fixing legacy naming violations in an existing file, the developer SHOULD scope renames to the specific function or class being modified to avoid unintended breakage in untested areas.

**NFR-05 — Tooling Compatibility [MUST]**

> The naming convention SHALL remain compatible with standard JavaScript `camelCase` (frontend) and Java `camelCase`/`PascalCase` (backend) tooling. No non-standard casing scheme shall be introduced.

**NFR-06 — Zero New Violations in New Code [MUST]**

> WHILE this spec is in ACTIVE status, the number of P0 naming violations in any newly authored file SHALL be zero at the time of PR submission.

---

## 5. Data Model

This spec governs **identifier tokens** in source code. The classification schema below is used by both human reviewers and automated tooling.

```
IdentifierToken {
  name          : string               // raw token text
  language      : "JavaScript" | "Java"
  category      : IdentifierCategory
  severity      : ViolationLevel | "CLEAN"
  allowedScope  : string | null        // null = allowed in all scopes
}

IdentifierCategory =
  | VARIABLE           // let / const / local var declaration
  | PARAMETER          // function / method parameter
  | CALLBACK_ARG       // arrow-function / lambda argument
  | FIELD              // class / record field
  | ENUM_CTOR_PARAM    // enum constructor parameter (Java)
  | REDUX_SELECTOR_ARG // useAppSelector state argument (Frontend)

ViolationLevel =
  | P0_MUST            // single-char or non-exception short form → blocks merge
  | P1_SHOULD          // non-standard abbreviation → flagged, must fix or justify
  | P2_MAY             // exception token where full name is feasible → advisory
```

### Violation Classification Matrix

| Pattern | Example | Category | Level |
|---|---|---|---|
| Single character not in approved list | `q`, `v`, `e`, `r`, `s`, `t`, `d`, `c`, `m`, `n` | Any | **P0** |
| Enum constructor param named `v` | `StaffRole(String v)` | `ENUM_CTOR_PARAM` | **P0** |
| Redux selector state named `s` | `useAppSelector((s) => ...)` | `REDUX_SELECTOR_ARG` | **P0** |
| Non-standard abbreviation | `usr`, `cfg`, `res`, `cb`, `fn`, `arr`, `obj` | Any | **P1** |
| `q` as search parameter (controller / service) | `@RequestParam String q` | `PARAMETER` | **P0** |
| `catch (e)` error variable | `} catch (e) {` | `PARAMETER` | **P1** |
| Approved exception used beyond allowed scope | `i` as a standalone variable name | Any | **P1** |
| Exception token where full name is feasible | `idx` where `questionIndex` is clear | Any | **P2** |

---

## 6. API / Tooling Spec

### 6.1 Frontend — ESLint Configuration

Add or merge into `apps/frontend/eslint.config.js`:

```js
export default [
  {
    rules: {
      // FR-VAR-01: Block identifiers shorter than 2 chars
      // Exceptions: i, j, k (for-loop counters), _ (ignored params)
      'id-length': [
        'error',
        {
          min: 2,
          exceptions: ['_', 'i', 'j', 'k'],
          properties: 'always',
        },
      ],

      // FR-VAR-02 / FR-VAR-03: Warn on known opaque short abbreviations
      // Extend this list as new patterns are discovered
      'id-match': [
        'warn',
        '^(?!(usr|cfg|res|cb|fn|arr|obj|msg|btn)([A-Z]|$))',
        {
          properties: true,
          onlyDeclarations: false,
          ignoreDestructuring: false,
        },
      ],
    },
  },
];
```

### 6.2 Backend — Checkstyle Configuration

Add to `apps/backend/checkstyle.xml` (or `spotless.xml` if using Spotless):

```xml
<!-- FR-VAR-01: Minimum identifier length -->
<module name="LocalVariableName">
  <property name="format" value="^[a-z][a-zA-Z0-9]{1,}$"/>
  <property name="allowOneCharVarInForLoop" value="true"/>
</module>

<module name="ParameterName">
  <property name="format" value="^[a-z][a-zA-Z0-9]{1,}$"/>
</module>

<module name="MemberName">
  <property name="format" value="^[a-z][a-zA-Z0-9]{1,}$"/>
</module>
```

### 6.3 Pre-commit Hook (Husky — Frontend)

```bash
# .husky/pre-commit — append to existing hook
echo "Running variable naming lint..."
npx eslint apps/frontend/src --ext .js,.jsx --max-warnings=0 \
  --rule '{"id-length": ["error", {"min": 2, "exceptions": ["_","i","j","k"]}]}'
```

### 6.4 CI — GitHub Actions

```yaml
# .github/workflows/naming-lint.yml
name: Variable Naming Check

on: [push, pull_request]

jobs:
  frontend-naming:
    name: Frontend Naming Lint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci --prefix apps/frontend
      - name: Check variable naming
        run: npx eslint apps/frontend/src --ext .js,.jsx --max-warnings=0
        working-directory: .

  backend-naming:
    name: Backend Checkstyle
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - run: mvn checkstyle:check -f apps/backend/pom.xml
```

---

## 7. Error Handling

### 7.1 Violation Response by Severity

| Level | Trigger | CI Outcome | PR Outcome | Resolution |
|---|---|---|---|---|
| **P0** | Single-char identifier / `q` as search param / enum `v` field | ❌ Build FAILED | 🔴 Merge blocked | Author must rename before re-review |
| **P1** | Non-standard abbreviation / opaque `catch (e)` | ⚠️ Warning logged | 🟡 Reviewer flags; author must fix or provide written justification | Author fixes OR adds comment `// eslint-disable-next-line -- [reason]` + opens tech-debt ticket |
| **P2** | Approved exception used where full name is feasible | ✅ Build passes | 🔵 IDE advisory only | Author self-guided; no mandatory action |

### 7.2 Automated PR Comment Template (P0)

When CI detects a P0 violation, post the following comment automatically:

```
🚫 [SPEC-CODING-001 P0] Variable naming violation detected.

File: {filename}
Line(s): {line_numbers}
Violation: Identifier `{name}` does not meet the project naming standard.

Required action: Rename to a descriptive, full-word identifier before this PR can be merged.
Reference: docs/06-Management/CODING-SPEC-variable-naming.md
```

### 7.3 Escalation Path

```
IF a developer disputes whether a name is a P0 violation:
  → Developer opens a comment on the PR citing the specific rule
  → Tech Lead issues a binding ruling within 1 business day
  → IF the name is ruled compliant, add it to FR-VAR-05 immediately
  → IF the name is ruled non-compliant, developer renames before merge

IF the linter produces a false positive for a legitimate domain symbol:
  → Developer adds inline suppression comment with justification:
       // eslint-disable-next-line id-length -- 'pi' is math constant PI (Math.PI)
  → Developer opens a ticket to add the symbol to FR-VAR-05
  → Tech Lead reviews and updates spec within the next sprint
```

### 7.4 Legacy Code Tech-Debt Markers

For each known violation listed in Section 1.2, add a tech-debt comment to the relevant file on the next touch:

```js
// TODO [SPEC-CODING-001 P0]: rename `q` → `currentQuestion` — Grammar.jsx:62
// TODO [SPEC-CODING-001 P0]: rename `(s) =>` → `(rootState) =>` — VocabHome.jsx:21
```

```java
// TODO [SPEC-CODING-001 P0]: rename param `v` → `displayValue` — StaffUser.java:78
// TODO [SPEC-CODING-001 P0]: rename param `q` → `searchKeyword` — AdminController.java:50
```

---

## 8. Acceptance Criteria

> All criteria are written using EARS **WHEN…SHALL** and **WHILE…SHALL** patterns.

### AC-01 — Zero P0 Violations in New Code
> **WHEN** a developer submits a PR containing any newly authored file or function, **the CI pipeline SHALL** report zero P0 naming violations before the PR is eligible for human review.

*Verification method:* CI `naming-lint` job status must be ✅ green on the PR.

### AC-02 — Legacy Files Cleaned on Touch
> **WHEN** a developer modifies an existing file listed in the violation table of Section 1.2, **the developer SHALL** resolve all P0 violations within the modified scope (function, class, or component) before submitting the PR.

*Verification method:* Reviewer diffs the modified file and confirms no P0 violation remains in any changed function or component.

### AC-03 — CI Gates Active on All Branches
> **WHEN** any commit is pushed to `main`, `develop`, or any feature branch, **the GitHub Actions `naming-lint` workflow SHALL** execute and report a result within 3 minutes.

*Verification method:* Confirm workflow file exists at `.github/workflows/naming-lint.yml` and a test PR with an intentional single-char variable triggers a failed status check.

### AC-04 — Backend Enum Fields Renamed
> **WHILE** `StaffUser.java`, `Ticket.java`, `StudentUser.java`, and `StudentContentProgress.java` are in the codebase, **all** enum constructor parameters named `v` SHALL be renamed to `displayValue` or an equivalent domain-specific name.

*Verification method:* `grep -r "String v" apps/backend/src` returns zero matches in enum bodies.

### AC-05 — All `@RequestParam String q` Renamed
> **WHILE** any REST controller uses `@RequestParam String q`, **the developer SHALL** rename it to `searchKeyword` or an equivalent name, and update all downstream service and repository method signatures accordingly.

*Verification method:* `grep -r "@RequestParam.*String q" apps/backend/src` returns zero matches.

### AC-06 — Redux Selector Args Renamed
> **WHEN** `useAppSelector` is used anywhere in the frontend, **the state argument SHALL** be named `rootState` or a descriptive domain-specific slice name.

*Verification method:* `grep -r "useAppSelector.*=> s\." apps/frontend/src` returns zero matches.

### AC-07 — Developer Self-Service
> **WHEN** a developer is unsure whether a name is compliant, **this spec SHALL** provide sufficient examples and the violation matrix in Section 5 to make a correct determination without consulting a teammate.

*Verification method:* New team members can answer a 10-question naming quiz (5 frontend, 5 backend) derived from Sections 3 and 5 with 80%+ accuracy during onboarding.

---

## 9. Out of Scope

The following are **explicitly excluded** from this specification:

| Item | Reason for Exclusion |
|---|---|
| **CSS class names, HTML `id` / `data-*` attributes** | Governed by a separate UI naming and BEM spec |
| **Database column names, table names, SQL aliases** | Governed by the Database Schema Design spec |
| **Environment variable names** (`REACT_APP_*`, `SPRING_*`) | Follow platform convention (`SCREAMING_SNAKE_CASE`); not identifiers in application code |
| **JPQL / HQL query string variable names** (inside `@Query` strings) | SQL-level aliases inside string literals; not Java identifiers |
| **Third-party library internals** | Not authored by this team; not modifiable |
| **Auto-generated code** (GraphQL types, Lombok, MapStruct, OpenAPI codegen) | Machine-generated; exempt from manual naming rules |
| **Test files** (`*.test.js`, `*Test.java`, `*Spec.java`) | P1/P2 rules relaxed in test scope; P0 still fully applies |
| **Bulk renames in unchanged files** | Untargeted bulk renames without full test coverage carry higher regression risk than the naming violation |
| **Non-source files** (YAML, JSON configs, SQL migration scripts) | Different tooling and review processes |
| **Naming conventions for classes, interfaces, enums** (beyond constructor parameters) | Java type naming is covered by standard Java conventions and a separate type-naming spec |

---

*End of SPEC-CODING-001 v2.0 — Project-Wide Variable Naming Convention*
