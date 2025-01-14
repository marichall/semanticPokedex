package semantic.pokedex.service;

import java.util.Map;

/**
 * This class is used to represent a template extracted from the wikitext.
 * Contains the name of the template (for example MoveInfobox or PokemonInfobox) and its associated parameters.
 */
public class TemplateData {
    public String name;
    public Map<String, String> params;

    public TemplateData(String name, Map<String, String> params) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "TemplateData{" +
                "name='" + name + '\'' +
                ", params=" + params +
                '}';
    }
    
}
