package semantic.pokedex.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WikitextParserService {

    // Parses the wikitext infobox and extracts key-value pairs.
    public Map<String, String> parseInfobox(String filePath, String InfoBoxType) throws IOException {
        String wikitext = new String(Files.readAllBytes(Paths.get(filePath)));
        return extractTemplateParameters(wikitext, InfoBoxType);
    }

    // Extracts parameters from a specified template in the wikitext.
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

    // Parses the wikitext and extracts all templates.
    public List<TemplateData> parseTemplates(String wikitext) {
        MyWikiModel model = new MyWikiModel("", "https://bulbapedia.bulbagarden.net/wiki/${title}");
        try {
            model.render(wikitext);
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return model.getCollectedTemplates();
    }


}