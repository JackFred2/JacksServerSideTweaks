package red.jackf.jsst.features;

import red.jackf.jsst.command.Response;

public abstract class Feature {
    private boolean enabled = true;

    public abstract void init();

    public abstract String id();

    public abstract String prettyName();

    public Response enable() {
        if (!this.enabled) {
            this.enabled = true;
            return Response.OK;
        } else {
            return Response.NO_CHANGE;
        }
    }

    public Response disable() {
        if (this.enabled) {
            this.enabled = false;
            return Response.OK;
        } else {
            return Response.NO_CHANGE;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
