package com.dnd.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
public class TranslationProvider implements I18NProvider {

    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale ROMANIAN = new Locale("ro");

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(ENGLISH, ROMANIAN);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            return "";
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
            String value = bundle.getString(key);

            if (params.length > 0) {
                return MessageFormat.format(value, params);
            }
            return value;
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}
