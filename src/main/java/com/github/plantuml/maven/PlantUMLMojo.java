/**
 * ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009, Arnaud Roques
 *
 * Project Info:  http://plantuml.sourceforge.net
 *
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * Original Author:  Arnaud Roques
 */

package com.github.plantuml.maven;


import java.io.File;
import java.util.Collection;

import net.sourceforge.plantuml.DirWatcher;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.OptionFlags;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal generate
 */
public class PlantUMLMojo extends AbstractMojo {

    private final Option option = new Option();

    /**
     * @parameter
     * @required
     * @default="${project.build.directory}/plantuml"
     * @expression="${plantuml.directory}"
     */
    private File directory;

    /**
     * @parameter
     * @required
     * @default="${project.build.directory}/plantuml"
     * @expression="${plantuml.outputDirectory}"
     */
    private File outputDirectory;

    /**
     * @parameter
     * @expression="${plantuml.charset}"
     */
    private String charset;

    /**
     * @parameter
     * @expression="${plantuml.config}"
     */
    private String config;

    /**
     * @parameter default=false
     * @expression="${plantuml.keepTmpFiles}"
     */
    private boolean keepTmpFiles;

    /**
     * @parameter
     * @expression="${plantuml.format}"
     */
    private String format;

    /**
     * @parameter
     * @expression="${plantuml.graphvizDot}"
     */
    private String graphvizDot;

    /**
     * @parameter default=false
     * @expression="${plantuml.verbose}"
     */
    private boolean verbose;

    protected final void setFormat(final String format) {
        if ("xmi".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.XMI_STANDARD);
        } else if ("xmi:argo".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.XMI_ARGO);
        } else if ("xmi:start".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.XMI_STAR);
        } else if ("eps".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.EPS);
        } else if ("svg".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.SVG);
        } else if ("txt".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.ATXT);
        } else if ("utxt".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.UTXT);
        } else {
            throw new IllegalArgumentException("Unrecongnized format <"+format+">");
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            this.option.setOutputDir(this.outputDirectory);
            if (this.charset != null) {
                this.option.setCharset(this.charset);
            }
            if (this.config != null) {
                this.option.initConfig(this.config);
            }
            if (this.keepTmpFiles) {
                OptionFlags.getInstance().setKeepTmpFiles(this.keepTmpFiles);
            }
            if (this.graphvizDot != null) {
                OptionFlags.getInstance().setDotExecutable(this.graphvizDot);
            }
            if (this.format != null) {
                setFormat(this.format);
            }
            if (this.verbose) {
                OptionFlags.getInstance().setVerbose(true);
            }

            final DirWatcher dirWatcher = new DirWatcher(this.directory, this.option, Option.getPattern());
            final Collection<GeneratedImage> result = dirWatcher.buildCreatedFiles();
            for (final GeneratedImage image : result) {
                getLog().debug(image + " " + image.getDescription());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Exception during plantuml process", e);
        }
    }

}