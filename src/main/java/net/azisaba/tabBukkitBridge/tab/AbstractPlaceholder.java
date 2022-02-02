package net.azisaba.tabBukkitBridge.tab;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.placeholder.Placeholder;

/**
 * Representation of any placeholder
 */
public abstract class AbstractPlaceholder implements Placeholder {

	private static final String[] EMPTY_ARRAY = new String[0];

	//refresh interval of the placeholder
	private final int refresh;

	//placeholder identifier including %
	protected final String identifier;

	private boolean active;
	private boolean triggerMode;
	private Runnable onActivation;
	private Runnable onDisable;

	/**
	 * Constructs new instance with given parameters and loads placeholder output replacements
	 * @param identifier - placeholder identifier
	 * @param refresh - refresh interval in milliseconds
	 */
	protected AbstractPlaceholder(String identifier, int refresh) {
		if (refresh % 50 != 0 && refresh != -1) throw new IllegalArgumentException("Refresh interval must be divisible by 50");
		if (!identifier.startsWith("%") || !identifier.endsWith("%")) throw new IllegalArgumentException("Identifier must start and end with %");
		this.identifier = identifier;
		this.refresh = refresh;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public int getRefresh() {
		return refresh;
	}
	
	public String[] getNestedPlaceholders(String output) {
		if (!output.contains("%")) return EMPTY_ARRAY;
		return TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(output).toArray(EMPTY_ARRAY);
	}
	
	private String replace(String string, String original, String replacement) {
		if (!string.contains(original)) return string;
		if (string.equals(original)) return replacement;
		return string.replace(original, replacement);
	}
	
	@Override
	public void enableTriggerMode() {
		triggerMode = true;
	}
	
	@Override
	public void enableTriggerMode(Runnable onActivation, Runnable onDisable) {
		triggerMode = true;
		this.onActivation = onActivation;
		this.onDisable = onDisable;
		if (active && onActivation != null) onActivation.run();
	}
	
	public void markAsUsed() {
		if (active) return;
		active = true;
		if (onActivation != null) onActivation.run();
	}

	@Override
	public boolean isTriggerMode() {
		return triggerMode;
	}
	
	@Override
	public void unload() {
		if (onDisable != null && active) onDisable.run();
	}
}