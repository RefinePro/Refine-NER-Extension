package org.freeyourmetadata.ner.services;

import static org.freeyourmetadata.util.UriUtil.createUri;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.freeyourmetadata.util.ParameterList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Alchemy service connector
 * @author Ruben Verborgh
 */
public class AlchemyAPI extends NERServiceBase {
    private final static URI SERVICEBASEURL = createUri("http://access.alchemyapi.com/calls/text/TextGetRankedNamedEntities?outputMode=json");
    private final static URI DOCUMENTATIONURI = createUri("http://freeyourmetadata.org/named-entity-extraction/alchemyapi/");
    private final static String[] SERVICESETTINGS = { "API key" };
    private final static String[] EXTRACTIONSETTINGS = { };
    private final static HashSet<String> NONURIFIELDS = new HashSet<String>(
            Arrays.asList(new String[]{ "subType", "name", "website" }));
    
    /**
     * Creates a new Alchemy service connector
     */
    public AlchemyAPI() {
        super(SERVICEBASEURL, DOCUMENTATIONURI, SERVICESETTINGS, EXTRACTIONSETTINGS);
    }
    
    /** {@inheritDoc} */
    public boolean isConfigured() {
        return getServiceSetting("API key").length() > 0;
    }
    
    /** {@inheritDoc} */
    protected HttpEntity createExtractionRequestBody(final String text, final Map<String, String> extractionSettings)
    throws UnsupportedEncodingException {
        final ParameterList parameters = new ParameterList();
        parameters.add("apikey", getServiceSetting("API key"));
        parameters.add("text", text);
        return parameters.toEntity();
    }
    
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    protected NamedEntity[] parseExtractionResponse(final JSONObject response) throws JSONException {
        // Check response status
        if (!"OK".equals(response.getString("status")))
            throw new RuntimeException(response.optString("statusInfo", "Extraction failed"));
        
        // Find all entities
        final JSONArray entities = response.getJSONArray("entities");
        final NamedEntity[] results = new NamedEntity[entities.length()];
        for (int i = 0; i < results.length; i++) {
            final JSONObject entity = entities.getJSONObject(i);
            final String entityText = entity.getString("text");
            
            // Find all disambiguations
            final ArrayList<Disambiguation> disambiguations = new ArrayList<Disambiguation>();
            if (entity.has("disambiguated")) {
                final JSONObject disambiguated = entity.getJSONObject("disambiguated");
                final String label = disambiguated.getString("name");
                final Iterator<String> keyIterator = disambiguated.keys();
                while (keyIterator.hasNext()) {
                    final String key = keyIterator.next();
                    if (!NONURIFIELDS.contains(key))
                        disambiguations.add(new Disambiguation(label, createUri(disambiguated.getString(key))));
                }
            }
            // Create new named entity for the result
            results[i] = new NamedEntity(entityText, disambiguations);
        }
        return results;
    }
}
