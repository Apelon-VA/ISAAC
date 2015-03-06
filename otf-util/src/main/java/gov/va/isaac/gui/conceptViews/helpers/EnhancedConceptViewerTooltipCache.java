package gov.va.isaac.gui.conceptViews.helpers;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Tooltip;

public class EnhancedConceptViewerTooltipCache {
	Map<Integer, Tooltip> tooltipCache = new HashMap<Integer, Tooltip>();
	Map<Integer, Long> lastUpdateCache = new HashMap<Integer, Long>();
	
	protected Tooltip getTooltip(int conNid) {
		return tooltipCache.get(conNid);
	}

	public boolean hasLatestTooltip(int compNid, long lastCommitTime) {
		if (lastUpdateCache.containsKey(compNid)) {
			if (lastUpdateCache.get(compNid).equals((lastCommitTime)) &&
				!lastUpdateCache.get(compNid).equals(Long.MAX_VALUE)) {
				return true;
			}
		}
		
		return false;
	}

	public void updateCache(int compNid, long lastCommitTime, Tooltip tp) {
		tooltipCache.put(compNid, tp);
		lastUpdateCache.put(compNid, lastCommitTime);		
	}
}
