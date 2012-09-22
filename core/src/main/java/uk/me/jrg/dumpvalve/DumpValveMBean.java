package uk.me.jrg.dumpvalve;

public interface DumpValveMBean {

	public abstract void setTimeRequests(boolean timeRequests);

	public abstract boolean isTimeRequests();

	public abstract void setEnabled(boolean enabled);

	public abstract boolean isEnabled();

	public abstract void setLogRequestPattern(String logRequestPattern);

	public abstract String getLogRequestPattern();

	public abstract void setUrlPattern(String urlPattern);

	public abstract String getUrlPattern();
	
}
