package gov.va.isaac.interfaces.gui.views;

import org.jvnet.hk2.annotations.Contract;

/**
 * {@link EnhancedSearchViewI}
 * 
 * An interface that allows the creation of an EnhancedSearchViewI implementation, which 
 * will be a JavaFX component that extends {@link DockedViewI}.  The docked ISAAC panel
 * is intended to allow creation, save and load of customizable searches with results
 * displayable in both interactive spreadsheet and taxonomic formats
 *
 * @author <a href="jkniaz@apelon.com">Joel Kniaz</a>
 */
@Contract
public interface EnhancedSearchViewI extends DockedViewI {
}
