/**
 * Copyright 2012 Julien Eluard
 * This project includes software developed by Julien Eluard: https://github.com/jeluard/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.plantuml.maven;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * @phase generate-resources
 * @goal generate
 */
public final class PlantUMLMojo extends AbstractMojo {

  private final Option option = new Option();

  /**
   * Fileset to search plantuml diagrams in.
   * @parameter property="plantuml.sourceFiles"
   * @required
   * @since 7232
   */
  private FileSet sourceFiles;

  /**
   * Directory where generated images are generated.
   * @parameter property="plantuml.outputDirectory" default-value="${basedir}/target/plantuml"
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
   * @parameter property="plantuml.outputInSourceDirectory" default-value="false"
   */
  private boolean outputInSourceDirectory;

  /**
   * Charset used during generation.
   * @parameter property="plantuml.charset"
   */
  private String charset;

  /**
   * External configuration file location.
   * @parameter property="plantuml.config"
   */
  private String config;

  /**
   * Wether or not to keep tmp files after generation.
   * @parameter property="plantuml.keepTmpFiles" default-value="false"
   */
  private boolean keepTmpFiles;

  /**
   * Specify output format. Supported values: xmi, xmi:argo, xmi:start, eps,  pdf, eps:txt, svg, png, dot, txt and utxt.
   * @parameter property="plantuml.format"
   */
  private String format;

  /**
   * Fully qualified path to Graphviz home directory.
   * @parameter property="plantuml.graphvizDot"
   */
  private String graphvizDot;

  /**
   * Wether or not to output details during generation.
   * @parameter property="plantuml.verbose" default-value="false"
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
    // early exit if sourceFiles directory is not available
    final String invalidSourceFilesDirectoryWarnMsg = this.sourceFiles.getDirectory() + " is not a valid path";
    if( null == this.sourceFiles.getDirectory() || this.sourceFiles.getDirectory().isEmpty()) {
        getLog().warn(invalidSourceFilesDirectoryWarnMsg);
        return;
    }
    File baseDir = null;
    try {
        baseDir = new File(this.sourceFiles.getDirectory());
    } catch (Exception e) {
        getLog().debug(invalidSourceFilesDirectoryWarnMsg, e);
    }
    if( null == baseDir || !baseDir.exists() || !baseDir.isDirectory()) {
        getLog().warn(invalidSourceFilesDirectoryWarnMsg);
        return;
    }
    if (!this.outputInSourceDirectory) {
      if (!this.outputDirectory.exists()) {
        // If output directoy does not exist yet create it.
        this.outputDirectory.mkdirs();
      }
      if (!this.outputDirectory.isDirectory()) {
        throw new IllegalArgumentException("<" + this.outputDirectory + "> is not a valid directory.");
      }
    }

    try {
      if (!this.outputInSourceDirectory) {
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

      final List<File> files = FileUtils.getFiles(
        baseDir,
        getCommaSeparatedList(this.sourceFiles.getIncludes()),
        getCommaSeparatedList(this.sourceFiles.getExcludes())
      );
      for(final File file : files) {
        getLog().info("Processing file <"+file+">");

        if (this.outputInSourceDirectory) {
          this.option.setOutputDir(file.getParentFile());
        } else {
          this.option.setOutputDir(outputDirectory.toPath().resolve(
              baseDir.toPath().relativize(file.toPath().getParent())).toFile());
        }

        final SourceFileReader sourceFileReader =
          new SourceFileReader(
            new Defines(), file, this.option.getOutputDir(),
            this.option.getConfig(), this.option.getCharset(),
            this.option.getFileFormatOption());
        for (final GeneratedImage image : sourceFileReader.getGeneratedImages()) {
          getLog().debug(image + " " + image.getDescription());
        }
      }
    } catch (Exception e) {
        throw new MojoExecutionException("Exception during plantuml process", e);
    }
  }

  protected String getCommaSeparatedList(final List<String> list) {
    final StringBuilder builder = new StringBuilder();
    final Iterator it = list.iterator();
    while(it.hasNext()) {
      final Object object = it.next();
      builder.append(object.toString());
      if (it.hasNext()) {
        builder.append(",");
      }
    }
    return builder.toString();
  }

}
