package com.oceandate.backend.domain.notification.service;

import com.oceandate.backend.domain.notification.entity.NotificationChannel;
import com.oceandate.backend.domain.notification.entity.NotificationTemplate;
import com.oceandate.backend.domain.notification.entity.NotificationType;
import com.oceandate.backend.domain.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private static final Pattern RAW_PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}}");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    private final NotificationTemplateRepository templateRepository;

    public RenderedTemplate renderEmail(NotificationType type, Map<String, ?> variables) {
        NotificationTemplate template;
        try {
            template = templateRepository.findByTypeAndChannelAndActiveTrue(type, NotificationChannel.EMAIL)
                    .orElseGet(() -> defaultTemplate(type));
        } catch (Exception e) {
            log.warn("Failed to load notification template from DB. type={}, fallback=defaultTemplate", type, e);
            template = defaultTemplate(type);
        }

        return new RenderedTemplate(
                replace(template.getTitleTemplate(), variables),
                replace(template.getBodyTemplate(), variables)
        );
    }

    private String replace(String template, Map<String, ?> variables) {
        String withRaw = replaceRaw(template, variables);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(withRaw);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object raw = variables.get(key);
            String value = raw == null ? "" : HtmlUtils.htmlEscape(String.valueOf(raw));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String replaceRaw(String template, Map<String, ?> variables) {
        Matcher matcher = RAW_PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object raw = variables.get(key);
            String value = raw == null ? "" : String.valueOf(raw);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private NotificationTemplate defaultTemplate(NotificationType type) {
        return switch (type) {
            case APPROVAL_COMPLETED -> NotificationTemplate.emailDefault(
                    NotificationType.APPROVAL_COMPLETED,
                    "[OceanDate] 신청이 승인되었습니다",
                    """
                    <html><body style="font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;">
                      <div style="max-width:560px;margin:0 auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px;">
                        <h2 style="margin:0 0 16px;">신청 승인이 완료되었습니다.</h2>
                        <p style="margin:0 0 16px;">아래 정보를 확인하시고 결제를 진행해 주세요.</p>
                        <div style="background:#f9fafb;padding:16px;border-radius:8px;">
                          <p style="margin:0 0 8px;"><strong>이벤트</strong>: {{eventName}}</p>
                          {{{eventDateTimeBlock}}}
                          {{{paymentUrlBlock}}}
                        </div>
                        <p style="margin:16px 0 0;">감사합니다.<br/>OceanDate</p>
                      </div>
                    </body></html>
                    """
            );
            case PAYMENT_COMPLETED -> NotificationTemplate.emailDefault(
                    NotificationType.PAYMENT_COMPLETED,
                    "[OceanDate] 결제가 완료되었습니다",
                    """
                    <html><body style="font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;">
                      <div style="max-width:560px;margin:0 auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px;">
                        <h2 style="margin:0 0 16px;">결제가 정상적으로 완료되었습니다.</h2>
                        <p style="margin:0 0 16px;">아래 결제 정보를 확인해 주세요.</p>
                        <div style="background:#f9fafb;padding:16px;border-radius:8px;">
                          <p style="margin:0 0 8px;"><strong>주문번호</strong>: {{orderId}}</p>
                          <p style="margin:0 0 8px;"><strong>결제금액</strong>: {{amount}}</p>
                          <p style="margin:0 0 8px;"><strong>이벤트</strong>: {{eventName}}</p>
                          {{{eventDateTimeBlock}}}
                        </div>
                        <p style="margin:16px 0 0;">감사합니다.<br/>OceanDate</p>
                      </div>
                    </body></html>
                    """
            );
            case DAY_BEFORE_REMINDER -> NotificationTemplate.emailDefault(
                    NotificationType.DAY_BEFORE_REMINDER,
                    "[OceanDate] 내일 일정 안내",
                    """
                    <html><body style="font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;">
                      <div style="max-width:560px;margin:0 auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px;">
                        <h2 style="margin:0 0 16px;">내일 참여 예정인 일정이 있습니다.</h2>
                        <div style="background:#f9fafb;padding:16px;border-radius:8px;">
                          <p style="margin:0 0 8px;"><strong>이벤트</strong>: {{eventName}}</p>
                          <p style="margin:0 0 8px;"><strong>일정</strong>: {{eventDateTime}}</p>
                          {{{placeBlock}}}
                        </div>
                        <p style="margin:16px 0 0;">미리 준비해 주세요.<br/>OceanDate</p>
                      </div>
                    </body></html>
                    """
            );
            case MATCHING_SUCCESS -> NotificationTemplate.emailDefault(
                    NotificationType.MATCHING_SUCCESS,
                    "[OceanDate] 매칭이 성사되었습니다",
                    """
                    <html><body style="font-family:Arial,sans-serif;line-height:1.6;color:#1f2937;">
                      <div style="max-width:560px;margin:0 auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px;">
                        <h2 style="margin:0 0 16px;">매칭이 성공적으로 완료되었습니다.</h2>
                        <div style="background:#f9fafb;padding:16px;border-radius:8px;">
                          <p style="margin:0 0 8px;"><strong>이벤트</strong>: {{eventName}}</p>
                          <p style="margin:0;"><strong>매칭 상대</strong>: {{partnerNickname}}</p>
                        </div>
                        <p style="margin:16px 0 0;">마이페이지에서 상세 정보를 확인해 주세요.<br/>OceanDate</p>
                      </div>
                    </body></html>
                    """
            );
        };
    }

    public record RenderedTemplate(String title, String bodyHtml) {
    }
}
