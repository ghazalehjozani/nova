# Formula-parity fixtures (Phase 3i, offline)

This directory holds the input tuples for **`FormulaParityTest`** (`container/src/test/java/ir/dotin/loan/trade/formula/FormulaParityTest.java`).

The test proves that the `expression-kit` (EvalEx) evaluation engine reproduces the numeric
result a formula would yield **inside FCB**, for the cases we can verify offline. It runs as a
plain surefire `*Test` (JUnit 5 + AssertJ): **no Spring context, no Testcontainers, no Docker, no
live FCB.** The nova E2E suite mocks all FCB ports, so this harness deliberately needs none of that
infrastructure — it evaluates formulas directly against the in-process engine.

## Why parity is not automatic

FCB's formula engine is **not** EvalEx. It is a bespoke `BigDecimal` recursive-descent parser with
its own operator and function set. `expression-kit` uses EvalEx with `MathContext.DECIMAL128` +
`RoundingMode.HALF_EVEN` (banker's rounding) — the same options nova's
`TradeLoanFormulaEvaluationService` evaluates money formulas with (`EvaluationOptions.financial()`).

- For the **safe common subset** — `+ - * /`, the power operator `^`, parentheses, and multi-digit
  decimal literals — the two engines agree **exactly**. Those are the fixtures with
  `divergenceExpected: false`, and their `expectedResult` is hand-computed and equals what FCB
  produces.
- For **FCB-only constructs** the two engines **diverge**. Those fixtures carry
  `divergenceExpected: true`; the test does **not** assert FCB parity for them — it documents what
  EvalEx actually does instead.

## Fixture format

Each `*.json` file is one parity tuple:

```json
{
  "name": "interest-simple",
  "expression": "approvedAmount * interestRate / 100 * durationMonths / 12",
  "inputs": {
    "approvedAmount": "500000000",
    "interestRate": "18",
    "durationMonths": "12"
  },
  "expectedResult": "90000000",
  "tolerance": "0.000001",
  "divergenceExpected": false,
  "note": "safe arithmetic subset; FCB and EvalEx agree"
}
```

| Field                | Meaning                                                                                                                                                                                                              |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`               | Display name for the parameterized test row. Defaults to the file name if omitted.                                                                                                                                   |
| `expression`         | The formula, written with the **friendly** variable names that appear in `inputs`.                                                                                                                                   |
| `inputs`             | Friendly variable name → decimal **string** value. Strings (not JSON numbers) preserve exact precision.                                                                                                              |
| `expectedResult`     | The value FCB's engine produces. For `divergenceExpected:false` it is hand-verified and equals FCB. For `divergenceExpected:true` it records the **EvalEx-observed** value (which is *not* FCB's), or may be omitted. |
| `tolerance`          | Max allowed `\|actual − expected\|` (BigDecimal). Defaults to `0.000001` if omitted.                                                                                                                                 |
| `divergenceExpected` | `false` → assert FCB parity within tolerance. `true` → document EvalEx behaviour, do not assert FCB equality.                                                                                                         |
| `note`               | Free-text explanation (why it is safe, or how the engines diverge).                                                                                                                                                  |

`expectedResult` semantics for divergent fixtures:
- **present** → EvalEx evaluates the construct but with different semantics; the test asserts EvalEx
  evaluates without error and yields this recorded EvalEx value.
- **omitted / null** → EvalEx *rejects* the FCB-only construct; the test asserts an
  `EvaluationException` is thrown.

## Friendly → FCB variable names

The friendly names used in `expression`/`inputs` are nova's domain-facing names. The corridor sends
evaluation values keyed by FCB `LOAN_PARAMETERS` codes; nova's
`FcbFormulaMapper.FRIENDLY_TO_FCB_ALIAS` maps them:

| Friendly (used here) | FCB `LOAN_PARAMETERS` code     |
|----------------------|--------------------------------|
| `approvedAmount`     | `LOAN_APPROVED_AMOUNT`         |
| `requestedAmount`    | `LOAN_REQUESTED_AMOUNT`        |
| `durationMonths`     | `LOAN_DURATION`                |
| `installmentCount`   | `INSTALLMENT_COUNT`            |
| `gracePeriodMonths`  | `BREAK_PERIOD_MONTH_DURATION`  |
| `interestRate`       | `LOAN_RATE`                    |
| `penaltyRate`        | `LOAN_PENALTY_RATE`            |

Keep fixture variable names aligned to the friendly names where natural — the mapping is the same
contract the production corridor uses, so a safe-subset fixture written with friendly names is a
faithful stand-in for the real FCB tuple.

## Capturing real FCB-stage tuples

The hand-verified safe-subset fixtures are deterministic, but you can also pin the harness against a
genuine FCB result:

1. Pick a loan formula and a concrete set of input values.
2. Evaluate it on **FCB stage** (run the formula through FCB's engine with those inputs) and read the
   numeric result FCB returns.
3. Drop a new JSON file in this directory with the `expression`, the `inputs` (friendly names), the
   FCB result as `expectedResult`, and `divergenceExpected: false`.

`FormulaParityTest` discovers every `*.json` here automatically — no code change is needed to add a
fixture. If the formula uses any FCB-only construct (below), mark it `divergenceExpected: true`
instead, because EvalEx will not reproduce FCB's value.

## Documented FCB ↔ EvalEx engine divergences

Authors writing new fixtures must know which formulas will diverge. FCB's bespoke engine differs
from EvalEx as follows:

| FCB construct                              | FCB meaning                                              | EvalEx behaviour                                                                                                  |
|--------------------------------------------|---------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| `<`                                        | **MIN** of the two operands                             | Boolean *less-than* comparison; the boolean result is coerced to `1` (true) / `0` (false). **Diverges.**         |
| `>`                                        | **MAX** of the two operands                             | Boolean *greater-than* comparison; coerced to `1` / `0`. **Diverges.**                                           |
| `~`                                        | **DIFFDATE** (date difference)                          | Not a recognised EvalEx operator → parse/evaluation error. **Diverges.**                                         |
| `dgs`, `dga`, `uu`, `zr`, `rnd`, `midRnd`, `sigma`, `log`, `root` | Custom FCB functions (some 2-arg via brackets) | Not EvalEx functions (note FCB's `log`/`rnd` differ from EvalEx `LOG`/`ROUND`; `rnd` **truncates**). **Diverges.** |
| `{CODE}`                                   | Inlines a linked formula by code                        | `{ }` is not EvalEx syntax → error. **Diverges.**                                                                |
| single-char internal variables             | Engine-internal variable namespace                      | EvalEx treats any identifier as a variable; only meaningful if you bind it.                                       |
| unbound variable                            | `MissingVariableValueException`                         | `expression-kit` throws `EvaluationException` ("Missing required variable"). Both reject — semantics align here.  |

**Safe subset that agrees exactly:** `+`, `-`, `*`, `/`, the power operator `^` with an **integer**
exponent (exact BigDecimal power; avoid the custom `pow(base,exponent)` *function*, which routes
through `Math.pow` on doubles and is lossy), parentheses, and multi-digit decimal literals.

## Shipped fixtures

| File                              | `divergenceExpected` | What it checks                                                                                |
|-----------------------------------|----------------------|-----------------------------------------------------------------------------------------------|
| `interest-simple.json`            | false                | `approvedAmount * interestRate / 100 * durationMonths / 12` → `90000000`.                      |
| `principal-per-installment.json`  | false                | `approvedAmount / installmentCount` (exact division) → `25000000`.                             |
| `penalty-amount.json`             | false                | `approvedAmount * penaltyRate / 100` → `15000000`.                                             |
| `compound-parenthetical.json`     | false                | `approvedAmount * (1 + interestRate / 100) ^ 2` (parentheses + integer power) → `1210000`.     |
| `divergence-lt-as-min.json`       | true                 | `a < b` (3,5): FCB MIN=3, EvalEx boolean→`1`. Documents the divergence.                        |
| `divergence-gt-as-max.json`       | true                 | `a > b` (8,2): FCB MAX=8, EvalEx boolean→`1`. Documents the divergence.                        |

## Running

```bash
mvn -f /home/m.amirabdollahi/workspaces/nova/pom.xml -pl container -am test -Dtest='FormulaParity*'
```

No Docker daemon and no FCB are required.
