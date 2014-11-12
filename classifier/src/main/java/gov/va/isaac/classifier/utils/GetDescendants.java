/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.isaac.classifier.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;

/**
 * The Class GetDescendants. Gets a conceptId list, which are descendants of
 * conceptId list passed as parameter. The process usages a rf2 relationship
 * file to getting is-a relationships.
 * 
 * Results are saved in output file passed as parameter
 * 
 * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */
public class GetDescendants {

  /** The Constant ISA_SCTID. */
  public static final String ISA_SCTID = "116680003";

  /** The relationship file. */
  private File relationshipFile;

  /** The parent concepts. */
  private HashSet<String> parentConcepts;

  /** The descendants file. */
  private File descendantsFile;

  /** The descendants. */
  private HashSet<String> descendants;

  /**
   * Gets the descendants.
   *
   * @return the descendants
   */
  public HashSet<String> getDescendants() {
    return descendants;
  }

  /**
   * Instantiates a new gets the descendants.
   *
   * @param parentConcepts the parent concepts
   * @param relationshipFile the relationship file
   * @param descendantsFile the descendants file
   */
  public GetDescendants(HashSet<String> parentConcepts, File relationshipFile,
      File descendantsFile) {
    super();
    this.parentConcepts = parentConcepts;
    this.relationshipFile = relationshipFile;
    this.descendantsFile = descendantsFile;
    descendants = new HashSet<>();
  }

  /**
   * Execute.
   *
   * @throws Exception the exception
   */
  public void execute() throws Exception {

    long start1 = System.currentTimeMillis();

    String nextLine;
    String[] splittedLine;

    if (descendantsFile != null && descendantsFile.exists()) {
      descendantsFile.delete();
    }
    HashSet<String> toFile = new HashSet<>();
    // for (String key:parentConcepts){
    // toFile.add(key);
    // }
    while (parentConcepts.size() > 0) {

      HashSet<String> child = new HashSet<>();

      FileInputStream rfis = new FileInputStream(relationshipFile);
      InputStreamReader risr = new InputStreamReader(rfis, "UTF-8");
      BufferedReader rbr = new BufferedReader(risr);

      rbr.readLine();

      nextLine = null;
      splittedLine = null;

      while ((nextLine = rbr.readLine()) != null) {
        splittedLine = nextLine.split("\t", -1);
        if (splittedLine[7].compareTo(ISA_SCTID) == 0
            && splittedLine[2].compareTo("1") == 0
            && !toFile.contains(splittedLine[4])
            && parentConcepts.contains(splittedLine[5])) {
          toFile.add(splittedLine[4]);
          child.add(splittedLine[4]);
        }
      }
      rbr.close();
      rfis = null;
      risr = null;
      System.gc();
      parentConcepts = child;
    }
    if (descendantsFile != null) {

      FileOutputStream fos = new FileOutputStream(descendantsFile);
      OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
      BufferedWriter bw = new BufferedWriter(osw);

      bw.append("COMPONENTID");
      bw.append("\r\n");
      for (String desc : toFile) {
        bw.append(desc);
        bw.append("\r\n");
      }
      bw.close();
      bw = null;
      osw = null;
      fos = null;
    } else {

      for (String desc : toFile) {
        descendants.add(desc);
      }
    }
    long end1 = System.currentTimeMillis();
    long elapsed1 = (end1 - start1);
    System.out.println("Completed in " + elapsed1 + " ms");
  }

}
