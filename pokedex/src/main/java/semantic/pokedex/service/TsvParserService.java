package semantic.pokedex.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TsvParserService {

    @Autowired
    RDFGeneratorService generatorService;
    
    // Parse a TSV file and return the data as a list.
    public List<Map<String, String>> parseTsv(String filePath) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); 
            if (headerLine == null) {
                throw new IOException("Le fichier TSV est vide !");
            }

            // Column names
            String[] headers = headerLine.split("\t");

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\t");
                Map<String, String> row = new HashMap<>();

                // Create a map of column names and values
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].trim();
                    String value = i < values.length ? values[i].trim() : "";
                    row.put(key, value);
                }

                data.add(row);
            }
        }

        return data;
    }

    // Filter the data by type.
    public List<Map<String,String>> filterPokemonData(List<Map<String, String>> tsvData, String type) {
        List<Map<String,String>> finalData = new ArrayList<>();
        for(int i=0; i<tsvData.size(); i++){
            Map<String, String> row = tsvData.get(i);
            if(row.get("type").equals(type)){
                finalData.add(row);
            }

        }
        return finalData;
    }

    // Get the English name of each Pokémon.
    public Map<String, String> getEnglishPokemonName(List<Map<String, String>> pokemonData) {
        Map<String, String> englishPokemonName = new HashMap<>();
        for (Map<String, String> row : pokemonData) {
            if ("English".equals(row.get("language"))) {
                englishPokemonName.put(row.get("id"), row.get("label"));
            }
        }
        return englishPokemonName;
    }

    // Generate RDF data for each Pokémon in the TSV file.
    public Model generateRdfForPokemonInTsvFile(List<Map<String,String>> pokemonData, String templateType) {
        Model model = ModelFactory.createDefaultModel();
        Property name = model.createProperty("https://schema.org/name");
        Resource pokemonPage;
        Map<String,String> englishPokemonName = getEnglishPokemonName(pokemonData);
        String uri = "http://localhost:8080/" + templateType + "/";
        String typeId = "" ; 
        String language = ""; 
        String label = ""; 
        
        for (Map<String,String> pokemon : pokemonData) {
            typeId = pokemon.get("id");
            language = getEncodeLanguage(pokemon.get("language"));
            label = pokemon.get("label");
            String englishName = englishPokemonName.get(typeId);
            if(englishName != null){
                pokemonPage = model.createResource(uri + generatorService.encodeName(englishName));
                model.add(pokemonPage, name, model.createLiteral(label, language));
            }
            else{
                continue;
            }
        }
        return model;
    }

    private String getEncodeLanguage(String language) {
        if ("english".equals(language)) {
            return "en";
        } else if ("French".equals(language)) {
            return "fr";
        } else if ("German".equals(language)) {
            return "de";
        } else if ("Japanese".equals(language)) {
            return "ja";
        } else if ("Korean".equals(language)) {
            return "ko";
        } else if ("Chinese".equals(language)) {
            return "zh";
        } else if ("Spanish".equals(language)) {
            return "es";
        } else if ("Italian".equals(language)) {
            return "it";
        } else if("Czech".equals(language)){
            return "cs";
        } else {
            return null;
        }
    }
}
