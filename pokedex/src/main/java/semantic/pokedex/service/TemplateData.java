package semantic.pokedex.service;

import java.util.Map;

public class TemplateData {
    public String name;
    public Map<String, String> params;

    public TemplateData(String name, Map<String, String> params) {
        this.name = name;
        this.params = params;
    }

    // Getters, toString, etc.
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
