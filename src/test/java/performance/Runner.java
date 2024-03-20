package performance;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import testutil.Utility;

public class Runner {

    public static void main(String[] args){
        // Logging
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();
        Utility.addFileLogger(builder, "pass_through", "performance/write/pass_through.log");
        Utility.addFileLogger(builder, "run_length", "performance/write/run_length.log");
        Utility.addFileLogger(builder, "run_length_mk2", "performance/write/run_length_mk2.log");
        Utility.addFileLogger(builder, "huffman", "performance/write/huffman.log");
        Utility.addFileLogger(builder, "brightness", "performance/read/brightness.log");
        Utility.addFileLogger(builder, "contrast", "performance/read/contrast.log");
        Utility.addFileLogger(builder, "blur", "performance/read/blur.log");
        Utility.addFileLogger(builder, "rotate", "performance/read/rotate.log");
        Utility.addFileLogger(builder, "perspective", "performance/read/perspective.log");
        LoggerContext ctx = Configurator.initialize(builder.build());

        // Testing
        if(args[0].equals("read")){
            new ReadPerformance().testReadPerformance(ctx);
        }
        if(args[0].equals("write")){
            if(args.length == 2){
                new WritePerformance().testPerformance(ctx, Integer.parseInt(args[1]));
            }
            else{
                new WritePerformance().testPerformance(ctx, 0);
            }
        }
    }
}
