package com.oceandate.backend.domain.notification.service;

import java.util.Map;

public interface TemplateRenderer {
    String render(String template, Map<String, ?> vars);
}
