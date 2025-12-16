package com.oceandate.backend.domain.notification.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimpleEmailTempalteRenderer implements TemplateRenderer {
    private static final Pattern VAR = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    @Override
    public String render(String template, Map<String, ?> vars) {
        if (template == null) {
            return null;
        }

        Matcher matcher = VAR.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = vars.get(key);
            String replacement = (value == null) ? "" : String.valueOf(value);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
