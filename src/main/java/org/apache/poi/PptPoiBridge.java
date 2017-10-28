package org.apache.poi;

import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextField;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;

/**
 * This is an internal class, it should not be considered as a public API:
 * this class will be deleted without any warning.
 */
@Deprecated
public class PptPoiBridge {

	public static void removeRelation(POIXMLDocumentPart parent, POIXMLDocumentPart child) {
		parent.removeRelation(child);
	}

	public static void removeHyperlink(XSLFTextRun textRun) {
		XmlObject xml = textRun.getXmlObject();
		if (xml instanceof CTTextField) {
			CTTextField tf = (CTTextField) xml;
			if (tf.isSetRPr()) {
				tf.getRPr().unsetHlinkClick();
			}
		} else if (xml instanceof CTTextLineBreak) {
			CTTextLineBreak tlb = (CTTextLineBreak) xml;
			if (tlb.isSetRPr()) {
				tlb.getRPr().unsetHlinkClick();
			}
		} else if (xml instanceof CTRegularTextRun) {
			CTRegularTextRun tr = (CTRegularTextRun) xml;
			if (tr.isSetRPr()) {
				tr.getRPr().unsetHlinkClick();
			}
		}
	}

}
