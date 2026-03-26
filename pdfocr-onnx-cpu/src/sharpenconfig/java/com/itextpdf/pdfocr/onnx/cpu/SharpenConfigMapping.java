/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.pdfocr.onnx.cpu;

import sharpen.config.MappingConfiguration;
import sharpen.config.MappingConfigurator;
import sharpen.config.ModuleOption;
import sharpen.config.ModulesConfigurator;
import sharpen.config.OptionsConfigurator;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Service implementation of {@link sharpen.config.MappingConfiguration} containing the module's Sharpen configuration.
 */
public class SharpenConfigMapping implements MappingConfiguration {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMappingPriority() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModuleName() {
        return "pdfocr-onnx-cpu";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyMappingConfiguration(MappingConfigurator configurator) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applySharpenOptions(OptionsConfigurator configurator) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyConfigModuleSettings(ModulesConfigurator configurator) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfigModuleSettings(ModulesConfigurator modulesConfigurator) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ModuleOption> getAvailableModuleSettings() {
        return Collections.EMPTY_SET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getDependencies() {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getIgnoredSourceFiles() {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getIgnoredResources() {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SimpleImmutableEntry<String, String>> getOverwrittenResources() {
        return Collections.EMPTY_LIST;
    }
}
