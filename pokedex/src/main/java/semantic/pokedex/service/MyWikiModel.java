package semantic.pokedex.service;

import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.tags.WPATag;
import info.bliki.htmlcleaner.ContentToken;
import info.bliki.wiki.filter.WikipediaPreTagParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom class extending Bliki's WikiModel to intercept and collect templates.
 * Enables the extraction of information from templates present in the wikitext.
 */
public class MyWikiModel extends WikiModel {

    // This variable will contain all the encoutered tamplates
    public final List<TemplateData> templates = new ArrayList<>();

    public MyWikiModel(String imageBaseURL, String linkBaseURL) {
        super(imageBaseURL, linkBaseURL);
    }
    /**
     * Override of the method as described in the documentation:
     * public void substituteTemplateCall(String templateName,
     *                                    Map<String, String> parameterMap,
     *                                    Appendable writer)
     *     throws IOException
     */
    @Override
    public void substituteTemplateCall(String templateName,
                                       Map<String, String> parameterMap,
                                       Appendable writer) throws IOException {
        // Storing the template in the list
        TemplateData data = new TemplateData(templateName, parameterMap);
        templates.add(data);

        super.substituteTemplateCall(templateName, parameterMap, writer);
    }

    @Override
public void appendInternalLink(
    final String topic,
    final String hashSection,
    final String topicDescription,
    String cssClass,
    boolean parseRecursive,
    boolean topicExists
) {
    // If it's a template of type "Template:..."
    if (topic.startsWith("Template:")) {
        // Creating a URL that points towards for ex., Bulbapedia
        String templateUrl = getWikiBaseURL().replace("${title}", topic);
        
        // Creating a tag of type HTML <a> (implemented by Bliki in WPATag)
        WPATag aTagNode = new WPATag();
        // We can define the attribute "href"
        aTagNode.addAttribute("href", templateUrl, true);

        // Addind a title in case it's needed
        aTagNode.addAttribute("title", topic, true);

        // Case where we would like to use a css class
        if (cssClass != null) {
            aTagNode.addAttribute("class", cssClass, true);
        }

        // Stacking the tag in Bliki's stack
        pushNode(aTagNode);
        
        // Adding the text from the link (either `topicDescription`, or the topic)
        String description = (topicDescription != null) 
                               ? topicDescription.trim() 
                               : topic;
        
        if (parseRecursive) {
            // In case it's needed, making a recursive call to re analyse the description if it's an internal link
            WikipediaPreTagParser.parseRecursive(description, this, false, true);
        } else {
            // Otherwise, it's just a normal text
            aTagNode.addChild(new ContentToken(description));
        }

        // Closing the tag
        popNode();

        
        return;

    } else {
        // Otherwise, we keep the default treatement of the method from bliki
        super.appendInternalLink(
            topic, 
            hashSection, 
            topicDescription, 
            cssClass, 
            parseRecursive, 
            topicExists
        );
    }
}


    public List<TemplateData> getCollectedTemplates() {
        return templates;
    }
    
}