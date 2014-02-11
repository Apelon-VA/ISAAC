/**
 * Copyright Notice
 * 
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.gui.util;

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

	//SCT Tree Icons
    ROOT(setupImage("/icons/fugue/16x16/icons-shadowless/node.png")),
    PRIMITIVE_SINGLE_PARENT(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-nowhere-button-white.png")),
    PRIMITIVE_MULTI_PARENT_CLOSED(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-090-button-white.png")), 
    PRIMITIVE_MULTI_PARENT_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-045-button-white.png")), 
    DEFINED_SINGLE_PARENT(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-nowhere-2.png")), 
    DEFINED_MULTI_PARENT_CLOSED(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-090.png")), 
    DEFINED_MULTI_PARENT_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-045.png")), 
    TAXONOMY_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/plus-small.png")), 
    TAXONOMY_CLOSE(setupImage("/icons/fugue/16x16/icons-shadowless/minus-small.png")),
    
    //Other GUI Icons
    CONCEPT_VIEW(setupImage("/icons/misc/16x16/gear.png")),
    COPY(setupImage("/icons/misc/16x16/document-copy.png"));

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