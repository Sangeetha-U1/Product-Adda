package com.productadda.service.notifications;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TemplateRenderingEngine {

    private static final Logger log = LoggerFactory.getLogger(TemplateRenderingEngine.class);

    // Matches {{variable_name}} -- plain substitution only, no
    // Handlebars conditionals/loops/helpers, scope
    // decision (no external Handlebars library either).
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    /*
     * ================================================================
     * RENDER
     * Description: Replaces every {{variable_name}} occurrence in text
     * with its value from context, converted via String.valueOf(). A
     * variable with no matching context key is left untouched in the
     * output (e.g. "{{foo}}" stays literal in the rendered result) and
     * logged as a warning, rather than throwing -- a missing variable
     * must never break real notification dispatch.
     * ================================================================
     */
    public String render(String text, Map<String, Object> context) {
        if (text == null) {
            return null;
        }
        if (context == null) {
            return text;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (context.containsKey(variableName)) {
                String replacement = String.valueOf(context.get(variableName));
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                log.warn("Template references unknown variable '{{{}}}' -- left unresolved in output", variableName);
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
