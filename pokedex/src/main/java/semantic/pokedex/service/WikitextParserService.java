package semantic.pokedex.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class WikitextParserService {

    /**
     * Parses the wikitext infobox and extracts key-value pairs.
     *
     * @param filePath Chemin vers le fichier wikitext.
     * @return Map of infobox properties.
     * @throws IOException Si la lecture du fichier échoue.
     */
    public Map<String, String> parseInfobox(String filePath) throws IOException {
        String wikitext = new String(Files.readAllBytes(Paths.get(filePath)));
        return extractTemplateParameters(wikitext, "Pokémon Infobox");
    }

    /**
     * Extracts parameters from a specified template in the wikitext.
     *
     * @param wikitext    The complete wikitext content.
     * @param templateName The name of the template to extract.
     * @return Map of parameter names and their values.
     */
    public Map<String, String> extractTemplateParameters(String wikitext, String templateName) {
        Map<String, String> params = new HashMap<>();

        String templateStart = "{{" + templateName;
        int startIdx = wikitext.indexOf(templateStart);
        if (startIdx == -1) {
            return params; // Template not found
        }

        int endIdx = wikitext.indexOf("}}", startIdx);
        if (endIdx == -1) {
            return params; // Malformed template
        }

        String templateContent = wikitext.substring(startIdx + templateStart.length(), endIdx).trim();
        String[] lines = templateContent.split("\\|");

        for (String line : lines) {
            String[] keyValue = line.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().toLowerCase().replace("-", "_");
                String value = keyValue[1].trim();
                params.put(key, value);
            }
        }

        return params;
    }
}
