package ir.dotin.loan.trade.architecture.builder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdrImporter {

    private static final Logger log = LoggerFactory.getLogger(AdrImporter.class);
    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    public void importAdrs(Workspace workspace, Path adrPath) {
        if (!Files.exists(adrPath)) {
            log.warn("⚠️ ADR path not found: {}", adrPath.toAbsolutePath());
            return;
        }

        try (Stream<Path> stream = Files.list(adrPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".md"))
                    .filter(p -> !p.getFileName().toString().equalsIgnoreCase("README.md"))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(path -> parseAndAddDecision(workspace, path));
        } catch (IOException e) {
            log.error("Failed to list ADR files", e);
        }
    }

    private void parseAndAddDecision(Workspace workspace, Path path) {
        try {
            String rawContent = Files.readString(path, StandardCharsets.UTF_8);
            String filename = path.getFileName().toString();
            List<String> lines = rawContent.lines().toList();

            String id = "0";
            Matcher numberMatcher = ID_PATTERN.matcher(filename);
            if (numberMatcher.find()) {
                id = numberMatcher.group(1);
            }

            String title = "Untitled";
            for (String line : lines) {
                if (line.trim().startsWith("#")) {
                    title = line.replaceAll("^#\\s*(ADR-\\d+)?[:\\s]*", "").trim();
                    break;
                }
            }

            String status = "پیشنهادی";
            Date date = new Date();

            for (String line : lines) {
                if (line.contains("وضعیت") || line.contains("Status")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        status = parts[1].trim().replace("*", "").trim();
                    }
                }

                if (line.contains("تاریخ") || line.contains("Date")) {
                    Matcher dateMatcher = DATE_PATTERN.matcher(line);
                    if (dateMatcher.find()) {
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd").parse(dateMatcher.group(1));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            String rtlContent =
                    "<div dir=\"rtl\" style=\"text-align: right; font-family: Tahoma, 'Vazir', sans-serif;\">\n\n"
                            + rawContent
                            + "\n\n</div>";

            Decision decision = new Decision(id);
            decision.setTitle(title);
            decision.setDate(date);
            decision.setStatus(status);
            decision.setFormat(Format.Markdown);
            decision.setContent(rtlContent);

            workspace.getDocumentation().addDecision(decision);

            log.info("✅ Imported Farsi ADR {}: {} [{}]", id, title, status);

        } catch (Exception e) {
            log.error("❌ Failed to import {}: {}", path.getFileName(), e.getMessage());
        }
    }
}
