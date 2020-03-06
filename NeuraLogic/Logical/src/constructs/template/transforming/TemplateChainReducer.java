package constructs.template.transforming;

import constructs.example.QueryAtom;
import constructs.template.Template;
import settings.Settings;

import java.util.logging.Logger;

/**
 * Created by gusta on 14.3.17.
 */
public class TemplateChainReducer implements TemplateReducing {
    private static final Logger LOG = Logger.getLogger(TemplateChainReducer.class.getName());
    public TemplateChainReducer(Settings settings) {

    }

    @Override
    public Template reduce(Template itemplate) {
        LOG.warning("LinearChainReducer for Template not implemented yet");
        return itemplate;
    }

    @Override
    public <T extends Template> T reduce(T itemplate, QueryAtom queryAtom) {
        LOG.warning("LinearChainReducer for Template not implemented yet");
        return itemplate;
    }
}