package us.noks.smallpractice.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class ComponentJoiner {
    private String delimiter, endLimiter;
    private TextComponent component = new TextComponent("");

    public ComponentJoiner(String delimiter) {
        this.delimiter = delimiter;
        this.endLimiter = null;
    }

    public ComponentJoiner(String delimiter, String endLimiter) {
        this.delimiter = delimiter;
        this.endLimiter = endLimiter;
    }

    public ComponentJoiner add(String newElement) {
        if (this.component.getExtra() != null) {
            this.component.addExtra(this.delimiter);
        }
        this.component.addExtra(newElement);
        return this;
    }

    public ComponentJoiner add(BaseComponent newElement) {
        if (this.component.getExtra() != null) {
            this.component.addExtra(this.delimiter);
        }
        this.component.addExtra(newElement);
        return this;
    }

    public TextComponent toTextComponent() {
        if (endLimiter != null) this.component.addExtra(this.endLimiter);
        return this.component;
    }
}