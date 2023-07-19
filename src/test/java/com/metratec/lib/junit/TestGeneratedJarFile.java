/*******************************************************************************
 * Copyright (c) 2023 by metraTec GmbH All rights reserved.
 *******************************************************************************/
package com.metratec.lib.junit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import com.metratec.lib.rfidreader.MetratecReader;

/**
 * Test if the license files are generated and stored in the jar file
 * 
 * @author mn
 *
 */
public class TestGeneratedJarFile {

  /**
   * Check if the 'LICENSE.txt' file is contained in the generated jar file
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void checkLicenseFile() throws Exception {
    String fileName = "LICENSE";
    Class<?> projectMainClass = MetratecReader.class;
    // Test
    File fileInProject = new File(fileName);
    Assert.assertTrue("'" + fileName + "' is missing in the project", fileInProject.exists());
    String filePathInJar = "/" + fileName;
    File fileInJar = new File(projectMainClass.getResource(filePathInJar).toURI());
    Assert.assertTrue("'" + filePathInJar + "' is missing in the jar file", fileInProject.exists());
    Assert.assertTrue(
        "'" + fileName + "' files in the project and in the generated jar file are not equals",
        filesEquals(fileInJar, fileInProject));
  }
  
  /**
   * Check if the 'LICENSE.txt' file is contained in the generated jar file
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void checkChangelogFile() throws Exception {
    String fileName = "CHANGELOG.md";
    Class<?> projectMainClass = MetratecReader.class;
    // Test
    File fileInProject = new File(fileName);
    Assert.assertTrue("'" + fileName + "' is missing in the project", fileInProject.exists());
    String filePathInJar = "/" + fileName;
    File fileInJar = new File(projectMainClass.getResource(filePathInJar).toURI());
    Assert.assertTrue("'" + filePathInJar + "' is missing in the jar file", fileInProject.exists());
    Assert.assertTrue(
        "'" + fileName + "' files in the project and in the generated jar file are not equals",
        filesEquals(fileInJar, fileInProject));
  }

  /**
   * Check if the 'LICENSE.txt' file is contained in the generated jar file
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void checkReadmeFile() throws Exception {
    String fileName = "README.md";
    Class<?> projectMainClass = MetratecReader.class;
    // Test
    File fileInProject = new File(fileName);
    Assert.assertTrue("'" + fileName + "' is missing in the project", fileInProject.exists());
    String filePathInJar = "/" + fileName;
    File fileInJar = new File(projectMainClass.getResource(filePathInJar).toURI());
    Assert.assertTrue("'" + filePathInJar + "' is missing in the jar file", fileInProject.exists());
    Assert.assertTrue(
        "'" + fileName + "' files in the project and in the generated jar file are not equals",
        filesEquals(fileInJar, fileInProject));
  }

  /**
   * Check if the 'LICENSE-LINKS.xml' file is contained in the generated jar file
   * 
   * @throws Exception if an error occurs
   */
  @Test
  public void checkLicenseLinksFile() throws Exception {
    String fileName = "LICENSE-LINKS.xml";
    Class<?> projectMainClass = MetratecReader.class;
    // Test
    File fileInProject = new File("target", fileName);
    Assert.assertTrue("'" + fileName + "' is missing in the project", fileInProject.exists());
    // String filePathInJar =
    //     "/" + projectMainClass.getPackage().getName().replaceAll("\\.", "/") + "/" + fileName;
    // File fileInJar = new File(projectMainClass.getResource(filePathInJar).toURI());
    File fileInJar = new File(projectMainClass.getResource("/"+fileName).toURI());
    Assert.assertTrue("'" + fileName + "' is missing in the jar file", fileInProject.exists());
    Assert.assertTrue(
        "'" + fileName + "' files in the project and in the generated jar file are not equals",
        filesEquals(fileInJar, fileInProject));
  }

  /**
   * Check if the 'THIRD-PARTY.txt' file is contained in the generated jar file
   * 
   * @throws Exception if an error occurs
   */
  @Test
  @Ignore
  public void checkThirdPartyFile() throws Exception {
    String fileName = "THIRD-PARTY.txt";
    Class<?> projectMainClass = MetratecReader.class;
    // Test
    File fileInProject = new File(fileName);
    Assert.assertTrue("'" + fileName + "' is missing in the project", fileInProject.exists());
    String filePathInJar =
        "/" + projectMainClass.getPackage().getName().replaceAll("\\.", "/") + "/" + fileName;
    File fileInJar = new File(projectMainClass.getResource(filePathInJar).toURI());
    Assert.assertTrue("'" + filePathInJar + "' is missing in the jar file", fileInProject.exists());
    Assert.assertTrue(
        "'" + fileName + "' files in the project and in the generated jar file are not equals",
        filesEquals(fileInJar, fileInProject));

    filePathInJar = "/" + fileName;
    fileInJar = new File(projectMainClass.getResource(filePathInJar).toURI());
    Assert.assertTrue("'" + filePathInJar + "' is missing in the jar file", fileInProject.exists());
    Assert.assertTrue(
        "'" + fileName + "' files in the project and in the generated jar file are not equals",
        filesEquals(fileInJar, fileInProject));
  }

  private boolean filesEquals(File a, File b) throws IOException {
    return Arrays.equals(Files.readAllBytes(a.toPath()), Files.readAllBytes(b.toPath()));
  }
}
