package uk.me.jrg.dumpvalve.mbean;

public interface DumpValveMBean {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    String getEnabledContexts();

    void setEnabledContexts(String enabledContexts);
}
