/**
 * SearchResultModelComparator
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.va.isaac.gui.enhancedsearchview.model;

import java.util.Comparator;

/**
 * @author joel
 *
 */
public class SearchResultModelComparator implements Comparator<SearchResultModel> {

	/**
     * Note, this sorts in reverse, so it goes highest to lowest
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(SearchResultModel o1, SearchResultModel o2) {
        if (o1.getScore() < o2.getScore()) {
            return 1;
        } else if (o1.getScore() > o2.getScore()) {
            return -1;
        }
        return 0;
    }
}
