package briefj;

import org.junit.Test;

import tutorialj.Tutorial;

import static briefj.CommandLineUtils.*;


/**
 * Summary
 * -------
 * 
 * briefj contains utilities for writing succinct java.
 * 
 * Installation
 * ------------
 * 
 * - Compile using ``gradle installApp``
 * - Add the jars in  ``build/install/briefj/lib/`` to your classpath, OR, add
 * the following to your project gradle script 
 * 
 * ```groovy
 * dependencies {
 *   compile group: 'com.3rdf', name: 'briefj', version: '1.1'
 * }
 * repositories {
 *   mavenCentral()
 *   jcenter()
 *   maven {
 *     url "http://www.stat.ubc.ca/~bouchard/maven/"
 *   }
 * }
 * ```
 * 
 */
@Tutorial(startTutorial = "README.md", nextStep = BriefIOTutorial.class)
public class BriefJTutorial implements Runnable
{
  /**
   * Command line utils
   * ------------------
   * 
   * Currently limited to a thin wrapper around JCommander, for creating command line programs:
   */
  @Tutorial(showSource = true)
  @Test
  public static void mainExample(String [] args)
  {
    start(new BriefJTutorial(), args);
  }

  @Override
  public void run()
  {
    System.out.println("Execution of the program.");
  }
}