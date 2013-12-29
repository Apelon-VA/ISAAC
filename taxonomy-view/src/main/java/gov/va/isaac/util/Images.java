package gov.va.isaac.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Common images for the taxonomy viewer.
 *
 * @author kec
 * @author Dan Armbrust
 * @author ocarlsen
 */
public enum Images {

	ROOT(setupImage("/icons/16x16/node.png")),
	PRIMITIVE_SINGLE_PARENT(setupImage("/icons/16x16/navigation-nowhere-button-white.png")),
	PRIMITIVE_MULTI_PARENT_CLOSED(setupImage("/icons/16x16/navigation-090-button-white.png")),
	PRIMITIVE_MULTI_PARENT_OPEN(setupImage("/icons/16x16/navigation-045-button-white.png")),
	DEFINED_SINGLE_PARENT(setupImage("/icons/16x16/navigation-nowhere-2.png")),
	DEFINED_MULTI_PARENT_CLOSED(setupImage("/icons/16x16/navigation-090.png")),
	DEFINED_MULTI_PARENT_OPEN(setupImage("/icons/16x16/navigation-045.png")),
	TAXONOMY_OPEN(setupImage("/icons/16x16/plus-small.png")),
	TAXONOMY_CLOSE(setupImage("/icons/16x16/minus-small.png"));

	private final Image image;

	private Images(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return this.image;
	}

	public ImageView createImageView() {
		return new ImageView(image);
	}

	private static Image setupImage(String imageUrl) {
		return new Image(imageUrl);//, false);
	}
}