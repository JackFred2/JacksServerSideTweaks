package red.jackf.jsst.client.impl.config;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import net.minecraft.network.chat.Component;

public class FormattableStringController extends StringController {
    private final Formatter formatter;

    /**
     * Constructs a string controller
     *
     * @param option bound option
     */
    private FormattableStringController(Option<String> option, Formatter formatter) {
        super(option);
        this.formatter = formatter;
    }

    @Override
    public Component formatValue() {
        return this.formatter.format(getString());
    }

    public static Builder create(Option<String> option) {
        return new Builder(option);
    }

    public static class Builder implements ControllerBuilder<String> {
        private final Option<String> option;
        private Formatter formatter = Component::literal;

        private Builder(Option<String> option) {
            this.option = option;
        }

        public Builder formatter(Formatter formatter) {
            this.formatter = formatter;
            return this;
        }

        @Override
        public Controller<String> build() {
            return new FormattableStringController(option, formatter);
        }
    }

    public interface Formatter {
        Component format(String value);
    }
}
