package constructs.template.transforming;

import constructs.template.Template;
import constructs.template.metadata.*;
import constructs.template.templates.ParsedTemplate;
import settings.Settings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Logger;

/**
 * Created by gusta on 6.3.18.
 */
public class MetadataProcessor {
    private static final Logger LOG = Logger.getLogger(MetadataProcessor.class.getName());

    public MetadataProcessor(Settings settings) {
    }

    public Template processMetadata(Template template, TemplateMetadata metadata) {
        throw new NotImplementedException();
    }

    public Template processMetadata(Template template, PredicateMetadata metadata) {

    }

    public Template processMetadata(Template template, WeightMetadata metadata) {

    }

    public Template processMetadata(Template template, RuleMetadata metadata) {

    }

    public ParsedTemplate processMetadata(ParsedTemplate template) {
        //TODO process all metadata for all elements
    }
}