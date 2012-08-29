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
import java.util.Iterator;
import java.util.List;

import net.sourceforge.plantuml.DirWatcher;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.model.FileSet;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal generate
 */
public class PlantUMLMojo extends AbstractMojo {

    private final Option option = new Option();

    /**
     * @parameter expression="${plantuml.directory}"
     * @deprecated Use sourceFiles parameter instead, which provides better capabilities of filtering.
     */
    private File directory;

    /**
     * Fileset to search plantuml diagrams in.
     * @parameter expression="${plantuml.sourceFiles}"
     * @required
     * @since 7232
     */
    private FileSet sourceFiles;

    /**
     * Directory where to put generated images.
     * @parameter expression="${plantuml.outputDirectory}" default-value="${basedir}/target/plantuml"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Whether or not to generate images in same directory as the source file.
     * This is useful for using PlantUML diagrams in Javadoc, 
     * as described here: 
     * <a href="http://plantuml.sourceforge.net/javadoc.html">http://plantuml.sourceforge.net/javadoc.html</a>.
     * 
     * If this is set to true then outputDirectory is ignored.
     * @parameter expression="${plantuml.outputInSourceDirectory}" default-value="false"
     */
    private boolean outputInSourceDirectory;

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
     * Specify output format. Supported values: xmi, xmi:argo, xmi:start, eps,  pdf, eps:txt, svg, png, dot, txt and utxt.
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
        } else if ("eps:txt".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.EPS_TEXT);
        } else if ("svg".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.SVG);
        } else if ("txt".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.ATXT);
        } else if ("utxt".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.UTXT);
        } else if ("png".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.PNG);
        } else if ("pdf".equalsIgnoreCase(format)) {
            this.option.setFileFormat(FileFormat.PDF);
        } else {
            throw new IllegalArgumentException("Unrecognized format <"+format+">");
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (this.directory != null && !this.directory.isDirectory()) {
            getLog().warn("<"+this.directory+"> is not a valid directory.");
            return;
        }
        if (!outputInSourceDirectory) {
            if (!this.outputDirectory.exists()) {
                // If output directoy does not exist yet create it.
                this.outputDirectory.mkdirs();
            }
            if (!this.outputDirectory.isDirectory()) {
                throw new IllegalArgumentException("<" + this.outputDirectory + "> is not a valid directory.");
            }
        }
        
        try {
        	if (!outputInSourceDirectory) {
                this.option.setOutputDir(this.outputDirectory);
            }
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

            File baseDir = null;
            try {
                baseDir  = new File(this.sourceFiles.getDirectory());
            }
            catch(Exception e) {
               getLog().warn(this.sourceFiles.getDirectory() + " is not a valid path");
            }
            if(baseDir != null) {
                final List<File> files = FileUtils.getFiles(
                     baseDir,
                     getCommaSeparatedList(this.sourceFiles.getIncludes()),
                     getCommaSeparatedList(this.sourceFiles.getExcludes())
                );
                for(final File f : files) {
                    getLog().info("Processing " + f);
                    
                    if (outputInSourceDirectory) {
                        this.option.setOutputDir(f.getParentFile());
                    }
                    
                    final SourceFileReader sourceFileReader =
                        new SourceFileReader(
                            new Defines(), f, this.option.getOutputDir(),
                            this.option.getConfig(), this.option.getCharset(),
                            this.option.getFileFormatOption());
                    for (final GeneratedImage image :
                             sourceFileReader.getGeneratedImages()) {
                        getLog().debug(image + " " + image.getDescription());
                    }
                }
            }
            else {
                getLog().info("Using <"+this.directory+"> as directory and <"+this.outputDirectory+"> as output directory.");

                final DirWatcher dirWatcher = new DirWatcher(this.directory, this.option, Option.getPattern());
                final Collection<GeneratedImage> result = dirWatcher.buildCreatedFiles();
                for (final GeneratedImage image : result) {
                    getLog().debug(image + " " + image.getDescription());
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Exception during plantuml process", e);
        }
    }

    protected String getCommaSeparatedList(final List list) {
        final StringBuffer buffer = new StringBuffer();
        final Iterator it = list.iterator();
        while(it.hasNext()) {
            Object object = it.next();
            buffer.append(object.toString());
            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

}
