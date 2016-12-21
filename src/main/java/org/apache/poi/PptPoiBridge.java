package org.apache.poi;

/**
 * This is an internal class, it should not be considered as a public API:
 * this class will be deleted without any warning.
 */
@Deprecated
public class PptPoiBridge {

	public static void removeRelation(POIXMLDocumentPart parent, POIXMLDocumentPart child) {
		parent.removeRelation(child);
	}

}
