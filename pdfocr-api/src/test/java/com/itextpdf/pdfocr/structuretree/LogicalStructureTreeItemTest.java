/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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

import com.itextpdf.kernel.pdf.tagutils.DefaultAccessibilityProperties;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class LogicalStructureTreeItemTest extends ExtendedITextTest {

    @Test
    public void addChildTest() {
        LogicalStructureTreeItem parent = new LogicalStructureTreeItem();
        LogicalStructureTreeItem child1 = new LogicalStructureTreeItem();
        LogicalStructureTreeItem child2 = new LogicalStructureTreeItem();
        child1.addChild(child2);
        parent.addChild(child1);
        parent.addChild(child2);

        Assert.assertEquals(2, parent.getChildren().size());
        Assert.assertEquals(0, child1.getChildren().size());
        Assert.assertEquals(parent, child1.getParent());
        Assert.assertEquals(parent, child2.getParent());
    }

    @Test
    public void removeChildTest() {
        LogicalStructureTreeItem parent = new LogicalStructureTreeItem();
        LogicalStructureTreeItem child1 = new LogicalStructureTreeItem();
        LogicalStructureTreeItem child2 = new LogicalStructureTreeItem();
        child1.addChild(child2);
        parent.addChild(child1);
        parent.addChild(child2);

        Assert.assertTrue(parent.removeChild(child1));
        Assert.assertFalse(parent.removeChild(child1));
        Assert.assertEquals(1, parent.getChildren().size());
    }

    @Test
    public void accessibilityPropertiesTest() {
        LogicalStructureTreeItem item = new LogicalStructureTreeItem()
                .setAccessibilityProperties(new DefaultAccessibilityProperties("Some role"));

        Assert.assertEquals("Some role", item.getAccessibilityProperties().getRole());
    }
}