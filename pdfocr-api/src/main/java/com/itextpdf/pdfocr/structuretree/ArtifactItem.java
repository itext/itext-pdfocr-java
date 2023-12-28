package com.itextpdf.pdfocr.structuretree;

/**
 * This class represents artifact structure tree item. Attaching such item to the text info means that
 * the text will be marked as artifact.
 */
public final class ArtifactItem extends LogicalStructureTreeItem {
    private final static ArtifactItem ARTIFACT_INSTANCE = new ArtifactItem();

    private ArtifactItem() {
        super();
    }

    /**
     * Retrieve an instance of {@link ArtifactItem}.
     *
     * @return an instance of {@link ArtifactItem}.
     */
    public static ArtifactItem getInstance() {
        return ARTIFACT_INSTANCE;
    }
}
