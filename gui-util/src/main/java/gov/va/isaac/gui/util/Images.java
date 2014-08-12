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
    LEGO_ADD(setupImage("/icons/silk/16x16/brick_add.png")),
    LEGO_DELETE(setupImage("/icons/silk/16x16/brick_delete.png")),
    LEGO_EDIT(setupImage("/icons/silk/16x16/brick_edit.png")),
    IMPORT(setupImage("/icons/fugue/16x16/icons-shadowless/application-import.png")),
    LEGO(setupImage("/icons/silk/16x16/brick.png")),
    LEGO_EXPORT(setupImage("/icons/silk/16x16/brick_go.png")),
    LEGO_LIST_VIEW(setupImage("/icons/silk/16x16/bricks.png")),
    SEARCH(setupImage("/icons/fugue/16x16/icons-shadowless/application-search-result.png")),
    CONCEPT_VIEW(setupImage("/icons/fugue/16x16/icons-shadowless/gear.png")),
    COPY(setupImage("/icons/fugue/16x16/icons-shadowless/document-copy.png")),
    DELETE(setupImage("/icons/silk/16x16/delete.png")),
    EXCLAMATION(setupImage("/icons/fugue/16x16/icons-shadowless/exclamation-red.png")),
    INFORMATION(setupImage("/icons/fugue/16x16/icons-shadowless/information.png")),
    XML_VIEW_16(setupImage("/icons/text-xml-icon-16x16.png")),
    XML_VIEW_32(setupImage("/icons/text-xml-icon-32x32.png")),
    HTML_VIEW_16(setupImage("/icons/xhtml-icon-16x16.png")),
    PROPERTIES(setupImage("/icons/document-properties-icon.png")),
    MINUS(setupImage("/icons/fugue/16x16/icons-shadowless/minus.png")), 
    PLUS(setupImage("/icons/fugue/16x16/icons-shadowless/plus.png")),
    EXPAND_ALL(setupImage("/icons/diagona/16x16/109.png")),
    LIST_VIEW(setupImage("/icons/fugue/16x16/icons-shadowless/edit-list.png")),
    STAMP(setupImage("/icons/fugue/16x16/icons-shadowless/stamp-medium.png")),
    INBOX(setupImage("/icons/fugue/16x16/icons-shadowless/inbox.png")),
    COMMIT(setupImage("/icons/silk/16x16/database_save.png")),
    CANCEL(setupImage("/icons/misc/16x16/cancel-change.png")),
    EDIT(setupImage("icons/silk/16x16/pencil.png")),
    ATTACH(setupImage("icons/silk/16x16/attach.png"));

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