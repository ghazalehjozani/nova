package ir.dotin.loan.trade.formula;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ir.dotin.platform.formula.api.EvaluationContext;
import ir.dotin.platform.formula.api.EvaluationOptions;
import ir.dotin.platform.formula.api.EvaluationResult;
import ir.dotin.platform.formula.api.FormulaExpression;
import ir.dotin.platform.formula.api.exception.EvaluationException;
import ir.dotin.platform.formula.core.engine.DefaultFormulaEngine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * OFFLINE formula-parity harness (Phase 3i).
 *
 * <p>Proves that {@code expression-kit}'s EvalEx-backed engine reproduces the numeric result a formula would yield in
 * FCB for the cases we can verify offline, WITHOUT a live FCB and WITHOUT Docker. FCB's engine is a bespoke
 * {@code BigDecimal} recursive-descent parser, NOT EvalEx; for the SAFE COMMON SUBSET ({@code + - * / ^}, parentheses,
 * multi-digit decimals) the two engines agree exactly, while for FCB-only constructs ({@code <}=MIN, {@code >}=MAX,
 * {@code ~}=DIFFDATE, {@code dgs/dga/uu/zr/rnd/midRnd/sigma/log/root}, {@code {CODE}} linked-formula inlining) they
 * diverge. See {@code container/src/test/resources/formula-parity/README.md} for the format and the full divergence
 * catalogue.
 *
 * <p><b>Test kind:</b> plain surefire {@code *Test} (JUnit 5 + AssertJ), NOT a failsafe {@code *IT}/{@code *E2E}. It
 * boots no Spring context and starts no Testcontainers, so it runs under {@code mvn -pl container -am test} with no
 * Docker and no FCB. Naming it {@code *IT} would bind it to the default failsafe {@code verify} execution; naming it
 * {@code *E2E} (or placing it under {@code ...e2e.*}) would drag in the Docker Compose stack via {@code AbstractE2E}.
 *
 * <p><b>Evaluation API:</b> the lowest-level engine entrypoint that takes a {@code Map} of variable values directly —
 * {@link DefaultFormulaEngine#evaluate(EvaluationContext)} with an {@link EvaluationContext} built from
 * {@link FormulaExpression#of(String)} and {@link EvaluationContext.Builder#variables(Map)}. No Spring, no registry, no
 * provider/binding ceremony is required. The engine uses {@link EvaluationOptions#financial()} = DECIMAL128 + HALF_EVEN
 * (banker's rounding), which is exactly the option production code ({@code TradeLoanFormulaEvaluationService})
 * evaluates money formulas with.
 *
 * <p>To capture a real FCB-stage tuple, run a formula's evaluation on FCB stage and drop a new JSON file under
 * {@code container/src/test/resources/formula-parity/}; this test picks it up automatically.
 */
@DisplayName("Formula parity: expression-kit (EvalEx) vs FCB engine — offline")
class FormulaParityTest {

    private static final String FIXTURE_DIR = "formula-parity";

    /** DECIMAL128 + HALF_EVEN, scale 6 — matches production money math (EvaluationOptions.financial()). */
    private static final EvaluationOptions FINANCIAL = EvaluationOptions.builder()
            .mathContext(MathContext.DECIMAL128)
            .roundingMode(RoundingMode.HALF_EVEN)
            .scale(6)
            .build();

    private static final DefaultFormulaEngine ENGINE = new DefaultFormulaEngine(FINANCIAL);

    private static final ObjectMapper JSON = new ObjectMapper();

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    @DisplayName("each fixture: safe-subset asserts FCB parity; divergent fixture documents EvalEx behaviour")
    void parity(ParityFixture fixture) {
        if (fixture.divergenceExpected()) {
            assertDivergence(fixture);
        } else {
            assertParity(fixture);
        }
    }

    /** Safe-subset fixture: the formula must reproduce FCB's hand-verified numeric result within tolerance. */
    private void assertParity(ParityFixture fixture) {
        BigDecimal actual = evaluate(fixture);
        BigDecimal delta = actual.subtract(fixture.expectedResult()).abs();

        assertThat(delta)
                .as(
                        "fixture '%s' expr '%s': EvalEx=%s expectedFCB=%s delta=%s tolerance=%s",
                        fixture.name(),
                        fixture.expression(),
                        actual.toPlainString(),
                        fixture.expectedResult().toPlainString(),
                        delta.toPlainString(),
                        fixture.tolerance().toPlainString())
                .isLessThanOrEqualTo(fixture.tolerance());
    }

    /**
     * FCB-only-semantics fixture: do NOT assert FCB parity. Document EvalEx's actual behaviour instead — either it
     * evaluates without error (and we record what it produced via {@code expectedResult}), or, where EvalEx rejects the
     * construct entirely, it throws an {@link EvaluationException}.
     */
    private void assertDivergence(ParityFixture fixture) {
        if (fixture.expectedResult() == null) {
            // No expected value supplied => the divergence is that EvalEx REJECTS the FCB-only construct.
            assertThatCode(() -> evaluate(fixture))
                    .as(
                            "fixture '%s' expr '%s': expected EvalEx to reject FCB-only construct",
                            fixture.name(), fixture.expression())
                    .isInstanceOf(EvaluationException.class);
            return;
        }

        // EvalEx evaluates the construct but with DIFFERENT semantics than FCB. Assert it evaluates without error and
        // produces the EvalEx-observed value recorded in the fixture (NOT FCB's value).
        BigDecimal actual = evaluate(fixture);
        BigDecimal delta = actual.subtract(fixture.expectedResult()).abs();

        assertThat(delta)
                .as(
                        "fixture '%s' expr '%s' (DIVERGENT): EvalEx=%s differs from FCB; recorded EvalEx value=%s",
                        fixture.name(),
                        fixture.expression(),
                        actual.toPlainString(),
                        fixture.expectedResult().toPlainString())
                .isLessThanOrEqualTo(fixture.tolerance());
    }

    private static BigDecimal evaluate(ParityFixture fixture) {
        Map<String, Object> variables = new LinkedHashMap<>(fixture.inputs());
        EvaluationContext context = EvaluationContext.builder()
                .formula(FormulaExpression.of(fixture.expression()))
                .variables(variables)
                .options(FINANCIAL)
                .build();
        EvaluationResult result = ENGINE.evaluate(context);
        return result.value();
    }

    // ─────────────────────── Fixture loading ───────────────────────

    static Stream<ParityFixture> fixtures() {
        List<ParityFixture> loaded = new ArrayList<>();
        for (Path file : listFixtureFiles()) {
            loaded.add(parse(file));
        }
        if (loaded.isEmpty()) {
            throw new IllegalStateException("No parity fixtures found on classpath under '" + FIXTURE_DIR + "'");
        }
        loaded.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return loaded.stream();
    }

    private static List<Path> listFixtureFiles() {
        URL dirUrl = Thread.currentThread().getContextClassLoader().getResource(FIXTURE_DIR);
        if (dirUrl == null) {
            throw new IllegalStateException("Classpath resource directory '" + FIXTURE_DIR + "' not found");
        }
        Path dir = toPath(dirUrl);
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path p : stream) {
                files.add(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list fixtures in " + dir, e);
        }
        return files;
    }

    private static Path toPath(URL url) {
        try {
            URI uri = url.toURI();
            return Paths.get(uri);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot resolve fixture directory path from " + url, e);
        }
    }

    private static ParityFixture parse(Path file) {
        JsonNode root;
        try {
            root = JSON.readTree(Files.readString(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read fixture " + file, e);
        }

        String name = text(root, "name", file.getFileName().toString());
        String expression = required(root, "expression", file);

        Map<String, BigDecimal> inputs = new LinkedHashMap<>();
        JsonNode inputsNode = root.get("inputs");
        if (inputsNode == null || !inputsNode.isObject()) {
            throw new IllegalStateException("Fixture " + file + " missing object field 'inputs'");
        }
        inputsNode
                .propertyStream()
                .forEach(entry -> inputs.put(
                        entry.getKey(), new BigDecimal(entry.getValue().asString())));

        JsonNode expectedNode = root.get("expectedResult");
        BigDecimal expected =
                (expectedNode == null || expectedNode.isNull()) ? null : new BigDecimal(expectedNode.asString());

        JsonNode toleranceNode = root.get("tolerance");
        BigDecimal tolerance = (toleranceNode == null || toleranceNode.isNull())
                ? new BigDecimal("0.000001")
                : new BigDecimal(toleranceNode.asString());

        boolean divergenceExpected =
                root.has("divergenceExpected") && root.get("divergenceExpected").asBoolean(false);

        return new ParityFixture(name, expression, inputs, expected, tolerance, divergenceExpected);
    }

    private static String required(JsonNode node, String field, Path file) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.asString().isBlank()) {
            throw new IllegalStateException("Fixture " + file + " missing required field '" + field + "'");
        }
        return value.asString();
    }

    private static String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? fallback : value.asString();
    }

    /** A single parity tuple loaded from a JSON fixture. */
    record ParityFixture(
            String name,
            String expression,
            Map<String, BigDecimal> inputs,
            BigDecimal expectedResult,
            BigDecimal tolerance,
            boolean divergenceExpected) {

        @Override
        public String toString() {
            return name + (divergenceExpected ? " [divergent]" : "");
        }
    }
}
