package briefj.run;

import static briefj.BriefIO.write;
import static briefj.run.ExecutionInfoFiles.*;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import briefj.BriefStrings;
import briefj.db.Records;
import briefj.opt.OptionsParser;
import briefj.opt.OrderedStringMap;
import briefj.repo.RepositoryUtils;
import briefj.run.RedirectionUtils.Tees;

import com.google.common.base.Joiner;
import com.google.common.hash.HashCode;


public class Mains
{

  
  /**
   * Instrument various aspect of this run.
   * 
   * 
   * Note that we check for existence of each file in case this is being 
   * called within a larger context that already created these files (so
   * it is not necessary to perform these checks for some java-specific options).
   * 
   * @param args
   * @param mainClass
   */
  public static void instrumentedRun(String [] args, Runnable mainClass)
  {
    long startTime = System.currentTimeMillis();
    
    // redirect std in / std out
    Tees tees = exists(STD_OUT_FILE) ?
        null :
        RedirectionUtils.createTees(getExecutionInfoFolder());
    
    RecordPermanentStateResults optsRead = recordPermanentState(args, mainClass);
    recordTransientInfo(args, mainClass, startTime);
    
    if (optsRead.optionReadSuccessfully)
      try 
      {
        // start job
        mainClass.run();
      }
      catch (RuntimeException e)
      {
        write(getFile(EXCEPTION_FILE), ExceptionUtils.getStackTrace(e));
        throw e;
      }
      
    // record end time
    long endTime = System.currentTimeMillis();
    if (!exists(END_TIME_FILE))
      write(getFile(END_TIME_FILE), "" + endTime);
    
    System.out.println("executionMilliseconds : " + (endTime - startTime));
    System.out.println("outputFolder : " + Results.getResultFolder().getAbsolutePath());
    
    if (tees != null)
      tees.close();
    
    outputMap.printEasy(getFile(OUT_MAP));
    
    if(System.getenv().get("CONN_PATH") != null)
      Records.recordsFromEnvironmentVariable().recordFullRun(optsRead.options.asLinkedHashMap(), outputMap.asLinkedHashMap(), Results.getResultFolder());
  }

  private static void recordTransientInfo(String[] args, Runnable mainClass, long startTime)
  {
    // create start time file
    if (!exists(START_TIME_FILE))
      write(getFile(START_TIME_FILE), "" + startTime);
    
    // record machine info
    if (!exists(HOST_INFO_FILE))
      write(getFile(HOST_INFO_FILE), hostInfo());
  }

  private static CharSequence hostInfo()
  {
    return "Host\t" + SysInfoUtils.getHostName() + "\n" +
           "CPUSpeed\t" + SysInfoUtils.getCPUSpeedStr() + "\n" +
           "MaxMemory\t" + SysInfoUtils.getMaxMemoryStr() + "\n" + 
           "NumCPUs\t" + SysInfoUtils.getNumCPUs();
  }

  
  private static class RecordPermanentStateResults
  {
    public boolean optionReadSuccessfully;
    public OrderedStringMap options;
    
    public RecordPermanentStateResults(boolean optionReadSuccessfully, OrderedStringMap options)
    {
      this.optionReadSuccessfully = optionReadSuccessfully;
      this.options = options;
    }
   
  }
  
  
  /**
   * 
   * @param args
   * @param mainClass
   * @return If the options were read successfully.
   */
  private static RecordPermanentStateResults recordPermanentState(String[] args, Runnable mainClass)
  {
    // record main class
    write(
      getFile(MAIN_CLASS_FILE),
      mainClass.getClass().toGenericString());
      
    // record JVM options
    List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
    write(
      getFile(JVM_OPTIONS),
      Joiner.on(" ").join(arguments));
      
    // record pwd
    if (!exists(WORKING_DIR))
      write(
        getFile(WORKING_DIR),
        System.getProperty("user.dir"));
    
    // record raw arguments
    write(
      getFile(JAVA_ARGUMENTS),
      Joiner.on(" ").join(args));
    
    // record command line options
    OptionsParser parser;
    try 
    {
      parser = OptionsUtils.parseOptions(args, mainClass);
      if (!exists(OPTIONS_MAP))
        OptionsUtils.recordOptions(parser);
    }
    catch (OptionsUtils.InvalidOptionsException e)
    {
      return new RecordPermanentStateResults(false, null);
    }
    
    try
    {
      if (!exists(REPOSITORY_INFO))
        if (!RepositoryUtils.recordCodeVersion(mainClass))
          // if there were dirty file (i.e. not in version control) write a random string to avoid collisions
          write(
            getFile(DIRTY_FILE_RANDOM_HASH), 
            HashUtils.HASH_FUNCTION.hashUnencodedChars(BriefStrings.generateUniqueId()).toString());
    }
    catch (RuntimeException e)
    {
      System.err.println("WARNING: Bare Repository has neither a working tree, nor an index.");
      write(getFile(EXCEPTION_FILE), ExceptionUtils.getStackTrace(e));
    }

    if (!exists(CLASSPATH_INFO))
      DependencyUtils.recordClassPath();
     
    // create softlinks of input, checking they exist
    if (!exists(INPUT_LINKS_FOLDER))
    {
      boolean success = IOLinkUtils.createIOLinks(parser);
      if (!success)
        // if there were missing input file, write a random string to avoid collisions
        write(
          getFile(DIRTY_FILE_RANDOM_HASH), 
          HashUtils.HASH_FUNCTION.hashUnencodedChars(BriefStrings.generateUniqueId()).toString());
    }
    
    // global hash code of the execution inputs, repository, etc
    HashCode global = HashUtils.computeFileHashCodesRecursively(getExecutionInfoFolder());
    write(getFile(GLOBAL_HASH), global.toString());
    return new RecordPermanentStateResults(true, parser.getOptionPairs());
  }
  
  /**
   * Modified from Percy Liang's fig
   * https://github.com/percyliang/fig
   */
  public static void putLogRec(String key, Object value)
  {
    outputMap.put(key, value.toString());
  }
  
  private static OrderedStringMap outputMap = new OrderedStringMap();
}
