package com.itextpdf.pdfocr;

import com.itextpdf.commons.actions.contexts.IMetaInfo;

/**
 * Container to keep meta info.
 */
public class PdfOcrMetaInfoContainer {

    private final IMetaInfo metaInfo;

    /**
     * Creates instance of container to keep passed meta info.
     *
     * @param metaInfo meta info
     */
    public PdfOcrMetaInfoContainer(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    IMetaInfo getMetaInfo() {
        return metaInfo;
    }
}
