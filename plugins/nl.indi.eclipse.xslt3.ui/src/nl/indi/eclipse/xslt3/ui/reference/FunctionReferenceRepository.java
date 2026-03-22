package nl.indi.eclipse.xslt3.ui.reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class FunctionReferenceRepository {

    private static final String PLUGIN_ID = "nl.indi.eclipse.xslt3.ui";
    private static final String INDEX_PATH = "docs/function-reference/functions.tsv";
    private static final String STYLE_PATH = "docs/function-reference/reference.css";
    private static final FunctionReferenceRepository INSTANCE = new FunctionReferenceRepository();

    private volatile Map<String, FunctionReferenceEntry> entriesByLookupKey;
    private volatile String styleSheet;

    private FunctionReferenceRepository() {
    }

    public static FunctionReferenceRepository getInstance() {
        return INSTANCE;
    }

    public Optional<FunctionReferenceEntry> findByLookupKey(String lookupKey) {
        ensureLoaded();
        return Optional.ofNullable(entriesByLookupKey.get(normalizeLookupKey(lookupKey)));
    }

    public String getStyleSheet() {
        ensureLoaded();
        return styleSheet;
    }

    private void ensureLoaded() {
        if (entriesByLookupKey != null && styleSheet != null) {
            return;
        }

        synchronized (this) {
            if (entriesByLookupKey != null && styleSheet != null) {
                return;
            }

            entriesByLookupKey = Collections.unmodifiableMap(loadEntries());
            styleSheet = loadStyleSheet();
        }
    }

    private Map<String, FunctionReferenceEntry> loadEntries() {
        Map<String, FunctionReferenceEntry> loadedEntries = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openStream(INDEX_PATH), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] columns = line.split("\\t", -1);
                if (columns.length < 5) {
                    throw new IllegalStateException("Invalid function reference row: " + line);
                }

                String[] lookupKeys = columns[0].split("\\s*,\\s*");
                String primaryLookupKey = normalizeLookupKey(lookupKeys[0]);
                FunctionReferenceEntry entry = new FunctionReferenceEntry(
                    primaryLookupKey,
                    columns[1].trim(),
                    columns[2].trim(),
                    columns[3].trim(),
                    columns[4].trim()
                );

                for (String lookupKey : lookupKeys) {
                    loadedEntries.put(normalizeLookupKey(lookupKey), entry);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load function reference index.", exception);
        }
        return loadedEntries;
    }

    private String loadStyleSheet() {
        try (InputStream inputStream = openStream(STYLE_PATH)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return "";
        }
    }

    private InputStream openStream(String resourcePath) throws IOException {
        InputStream inputStream = FunctionReferenceRepository.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not available in bundle " + PLUGIN_ID + ": " + resourcePath);
        }
        return inputStream;
    }

    private String normalizeLookupKey(String lookupKey) {
        return lookupKey.trim().toLowerCase(Locale.ROOT);
    }
}
