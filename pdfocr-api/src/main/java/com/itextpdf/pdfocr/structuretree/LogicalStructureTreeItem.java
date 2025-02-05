/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfocr.structuretree;

import com.itextpdf.kernel.pdf.tagutils.AccessibilityProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents structure tree item of the text item put into the pdf document.
 * See {@link com.itextpdf.pdfocr.TextInfo}.
 */
public class LogicalStructureTreeItem {

    private AccessibilityProperties accessibilityProperties;
    private List<LogicalStructureTreeItem> children = new ArrayList<>();
    private LogicalStructureTreeItem parent;

    /**
     * Instantiate a new {@link LogicalStructureTreeItem} instance.
     */
    public LogicalStructureTreeItem() {
        this(null);
    }

    /**
     * Instantiate a new {@link LogicalStructureTreeItem} instance.
     *
     * @param accessibilityProperties properties to define and describe pdf structure elements.
     */
    public LogicalStructureTreeItem(AccessibilityProperties accessibilityProperties) {
        this.accessibilityProperties = accessibilityProperties;
    }

    /**
     * Retrieve structure tree element's properties.
     *
     * @return structure tree element's properties.
     */
    public AccessibilityProperties getAccessibilityProperties() {
        return accessibilityProperties;
    }

    /**
     * Set structure tree element's properties.
     *
     * @param accessibilityProperties structure tree element's properties.
     * @return this {@link LogicalStructureTreeItem} instance.
     */
    public LogicalStructureTreeItem setAccessibilityProperties(AccessibilityProperties accessibilityProperties) {
        this.accessibilityProperties = accessibilityProperties;
        return this;
    }

    /**
     * Retrieve parent structure tree item.
     *
     * @return parent structure tree item.
     */
    public LogicalStructureTreeItem getParent() {
        return parent;
    }

    /**
     * Add child structure tree item.
     *
     * @param child child structure tree item.
     * @return this {@link LogicalStructureTreeItem} instance.
     */
    public LogicalStructureTreeItem addChild(LogicalStructureTreeItem child) {
        children.add(child);
        if (child.getParent() != null) {
            child.getParent().removeChild(child);
        }
        child.parent = this;

        return this;
    }

    /**
     * Remove child structure tree item.
     *
     * @param child child structure tree item.
     * @return {@code true} if the child was removed, {@code false} otherwise.
     */
    public boolean removeChild(LogicalStructureTreeItem child) {
        if (children.remove(child)) {
            child.parent = null;
            return true;
        }

        return false;
    }

    /**
     * Retrieve all child structure tree items.
     *
     * @return all child structure tree items.
     */
    public List<LogicalStructureTreeItem> getChildren() {
        return children;
    }
}
