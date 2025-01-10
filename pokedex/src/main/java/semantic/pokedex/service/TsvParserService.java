package semantic.pokedex.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<String> getAllParameters(String templateType){
        List<String> allParam = null;
        
        return allParam;
    }
}
