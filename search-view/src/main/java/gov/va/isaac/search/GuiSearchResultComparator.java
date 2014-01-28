package gov.va.isaac.search;

import java.util.Comparator;

/**
 * A {@link Comparator} for {@link SearchResult} objects.
 *
 * @author ocarlsen
 */
public class GuiSearchResultComparator implements Comparator<GuiSearchResult> {

    /**
     * Note, this sorts in reverse, so it goes highest to lowest
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(GuiSearchResult o1, GuiSearchResult o2) {
        if (o1.getBestScore() < o2.getBestScore()) {
            return 1;
        } else if (o1.getBestScore() > o2.getBestScore()) {
            return -1;
        }
        return 0;
    }
}
