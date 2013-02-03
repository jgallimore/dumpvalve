package uk.me.jrg.dumpvalve.mbean;

import java.util.List;

public class DumpValve implements DumpValveMBean {

    @Override
    public boolean isEnabled() {
        return Configuration.getInstance().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        Configuration.getInstance().setEnabled(enabled);
    }

    @Override
    public String getEnabledContexts() {
        StringBuilder sb = new StringBuilder();

        final List<String> contexts = Configuration.getInstance().getContexts();
        for (int i = 0; i < contexts.size(); i++) {
            String context = contexts.get(i);

            if (i > 0) {
                sb.append(",");
            }

            sb.append(context);
        }

        return sb.toString();
    }

    @Override
    public void setEnabledContexts(String enabledContexts) {
        Configuration.getInstance().getContexts().clear();

        final String[] contexts = enabledContexts.split(",");
        for (String context : contexts) {
            Configuration.getInstance().getContexts().add(context.trim());
        }
    }
}
