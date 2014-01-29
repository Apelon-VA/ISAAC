/**
 * Copyright 2013
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.legoEdit.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public enum Images
{
//TODO cleanup, move up to shared code
//Tree Icons
//	PRIMITIVE_SINGLE_PARENT(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-nowhere-button-white.png")), 
//	PRIMITIVE_MULTI_PARENT_CLOSED(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-090-button-white.png")), 
//	PRIMITIVE_MULTI_PARENT_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-045-button-white.png")), 
//	DEFINED_SINGLE_PARENT(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-nowhere-2.png")), 
//	DEFINED_MULTI_PARENT_CLOSED(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-090.png")), 
//	DEFINED_MULTI_PARENT_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/navigation-045.png")), 
//	ROOT(setupImage("/icons/fugue/16x16/icons-shadowless/node.png")),
//
//	TAXONOMY_OPEN(setupImage("/icons/fugue/16x16/icons-shadowless/plus-small.png")), 
//	TAXONOMY_CLOSE(setupImage("/icons/fugue/16x16/icons-shadowless/minus-small.png")),
	
	//Other GUI icons
//	LEGO_ADD(setupImage("/icons/silk/16x16/brick_add.png")),
//	LEGO_DELETE(setupImage("/icons/silk/16x16/brick_delete.png")),
//	LEGO_EDIT(setupImage("/icons/silk/16x16/brick_edit.png")),
//	LEGO(setupImage("/icons/silk/16x16/brick.png")),
//	LEGO_IMPORT(setupImage("/icons/fugue/16x16/icons-shadowless/application-import.png")),
//	LEGO_EXPORT_ALL(setupImage("/icons/fugue/16x16/icons-shadowless/application-export.png")),
//	LEGO_EXPORT(setupImage("/icons/silk/16x16/brick_go.png")),
//	LEGO_LIST_VIEW(setupImage("/icons/silk/16x16/bricks.png")),
//	LEGO_SEARCH(setupImage("/icons/fugue/16x16/icons-shadowless/application-search-result.png")),
	EXCLAMATION(setupImage("/icons/fugue/16x16/icons-shadowless/exclamation-red.png"));
//	XML_VIEW_16(setupImage("/icons/text-xml-icon-16x16.png")),
//	XML_VIEW_32(setupImage("/icons/text-xml-icon-32x32.png")),
//	HTML_VIEW_16(setupImage("/icons/xhtml-icon-16x16.png")),
//	PROPERTIES(setupImage("/icons/document-properties-icon.png")),
//	PREFERENCES(setupImage("/icons/fugue/16x16/icons-shadowless/application-task.png")),
//	APPLICATION(setupImage("/icons/fugue/16x16/icons-shadowless/application-block.png")),
//	CONCEPT_VIEW(setupImage("/icons/fugue/16x16/icons-shadowless/gear.png")),
//	COPY(setupImage("/icons/fugue/16x16/icons-shadowless/document-copy.png")),
//	TEMPLATE(setupImage("/icons/fugue/16x16/icons-shadowless/document-template.png")),
//	PASTE(setupImage("/icons/fugue/16x16/icons-shadowless/clipboard-paste-document-text.png")),
//	ADD(setupImage("/icons/silk/16x16/add.png")),
//	DELETE(setupImage("/icons/silk/16x16/delete.png")),
//	INFO(setupImage("/icons/fugue/16x16/icons-shadowless/information-frame.png")),
//	SAVE(setupImage("/icons/fugue/16x16/icons-shadowless/disk-black.png")),
//	EXIT(setupImage("/icons/fugue/16x16/icons-shadowless/cross.png")),
//	UNDO(setupImage("/icons/fugue/16x16/icons-shadowless/arrow-curve-180.png")),
//	REDO(setupImage("/icons/fugue/16x16/icons-shadowless/arrow-curve-000-left.png")),
//	FILTER(setupImage("/icons/filter_16x16.png")),
//	EXPAND_ALL(setupImage("/icons/diagona-icons/src/main/resources/diagona/16x16/109.png")),
//	PENDING(setupImage("/icons/fugue/16x16/icons-shadowless/asterisk.png"));

	private Image iconImage_;

	private Images(Image icon)
	{
		this.iconImage_ = icon;
	}

	public ImageView createImageView()
	{
		return Images.createImageView(iconImage_);
	}

	public Image getImage()
	{
		return this.iconImage_;
	}

	public static ImageView createImageView(Image image)
	{
		return new ImageView(image);
	}

	private static Image setupImage(String imageUrl)
	{
		return new Image(imageUrl, false);
	}
}