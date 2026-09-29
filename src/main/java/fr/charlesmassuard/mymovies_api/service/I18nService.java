package fr.charlesmassuard.mymovies_api.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class I18nService {

    private final MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, key, locale());
    }

    public Locale locale() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes requestAttributes) {
            String language = requestAttributes.getRequest().getHeader("Accept-Language");
            if (language != null && !language.isBlank()) {
                Locale requested = Locale.forLanguageTag(language.split(",")[0].trim());
                if (requested.getLanguage().equalsIgnoreCase("en")) return Locale.ENGLISH;
                if (requested.getLanguage().equalsIgnoreCase("fr")) return Locale.FRENCH;
            }
        }
        return LocaleContextHolder.getLocale().getLanguage().equalsIgnoreCase("en")
                ? Locale.ENGLISH
                : Locale.FRENCH;
    }

    public String tmdbLanguage() {
        return locale().equals(Locale.ENGLISH) ? "en-US" : "fr-FR";
    }

    public String cacheSuffix() {
        return locale().getLanguage();
    }
}
