/**
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright 2010 Julien Eluard
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     [http://www.apache.org/licenses/LICENSE-2.0]
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
     * @parameter expression="${plantuml.directory}" default-value="${basedir}/src/main/plantuml"
     * @required
     */
    private File directory;

    /**
     * @parameter expression="${plantuml.outputDirectory}" default-value="${basedir}/target/plantuml"
     * @required
     */
    private File outputDirectory;

    /**
     * Charset used during generation.
     * @parameter expression="${plantuml.charset}"
     */
    private String charset;

    /**
     * External configuration file location.
     * @parameter expression="${plantuml.config}"
     */
    private String config;

    /**
     * Wether or not to keep tmp files after generation.
     * @parameter expression="${plantuml.keepTmpFiles}" default-value="false"
     */
    private boolean keepTmpFiles;

    /**
     * Specify output format. Supported values: xmi, xmi:argo, xmi:start, eps, pdf, eps:txt, svg, txt and utxt.
     * @parameter expression="${plantuml.format}"
     */
    private String format;

    /**
     * Fully qualified path to Graphviz home directory.
     * @parameter expression="${plantuml.graphvizDot}"
     */
    private String graphvizDot;

    /**
     * Wether or not to output details during generation.
     * @parameter expression="${plantuml.verbose}" default-value="false"
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
            throw new IllegalArgumentException("Unrecognized format <"+format+">");
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (!this.directory.isDirectory()) {
            getLog().warn("<"+this.directory+"> is not a valid directory.");
            return;
        }
        if (!this.outputDirectory.exists()) {
            //If output directoy does not exist yet create it.
            this.outputDirectory.mkdirs();
        }
        if (!this.outputDirectory.isDirectory()) {
            throw new IllegalArgumentException("<"+this.outputDirectory+"> is not a valid directory.");
        }

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

            getLog().info("Using <"+this.directory+"> as directory and <"+this.outputDirectory+"> as output directory.");

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
