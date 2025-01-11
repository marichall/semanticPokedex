package semantic.pokedex.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.springframework.stereotype.Service;

@Service
public class TsvParserService {
    /**
     * Parse un fichier TSV et retourne les données sous forme de liste.
     * Chaque ligne correspond à une entrée, et chaque colonne est une clé-valeur.
     *
     * @param filePath Chemin vers le fichier TSV
     * @return Liste de mappages représentant les données du fichier
     */
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

    // public List<String> getAllParameters(String templateType){
    //     List<String> allParam = null;
    //     
    //     return allParam;
    // }

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

    public Map<String, String> getEnglishPokemonName(List<Map<String, String>> pokemonData) {
        Map<String, String> englishPokemonName = new HashMap<>();
        for (Map<String, String> row : pokemonData) {
            if ("English".equals(row.get("language"))) {
                englishPokemonName.put(row.get("id"), row.get("label"));
            }
        }
        return englishPokemonName;
    }

    public Model generateRdfForPokemonInTsvFile(List<Map<String,String>> pokemonData) {
        Model model = ModelFactory.createDefaultModel();
        Property name = model.createProperty("https://schema.org/name");
        Resource pokemonPage;
        Map<String,String> englishPokemonName = getEnglishPokemonName(pokemonData);
        
        for (Map<String,String> pokemon : pokemonData) {
            pokemonPage = model.createResource("http://localhost:8080/pokemon/" + englishPokemonName.get(pokemon.get("id")));
            model.add(pokemonPage, name, model.createLiteral(pokemon.get("label"), pokemon.get("language").toLowerCase().substring(0, 2)));
        }
        return model;
    }
}
