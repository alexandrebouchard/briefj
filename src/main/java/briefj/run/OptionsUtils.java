package briefj.run;


import static briefj.run.ExecutionInfoFiles.*;
import briefj.opt.OptionsParser;

public class OptionsUtils
{
  public static void recordOptions(OptionsParser parser)
  {
    parser.getOptionPairs().printEasy(getFile(OPTIONS_MAP)); 
    parser.getOptionPairs().printEasy(getFile(OPTIONS_MAP + ".txt")); 
    parser.getOptionStrings().printEasy(getFile(OPTIONS_DESCRIPTIONS)); 
  }

  public static OptionsParser parseOptions(String[] args, Object ... objects)
  {
    final OptionsParser parser = new OptionsParser();
    for (Object object : objects)
      parser.register(object);
    if (!parser.parse(args))
      throw new InvalidOptionsException();
    return parser;
  }
  
  public static class InvalidOptionsException extends RuntimeException
  {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
  }
}
