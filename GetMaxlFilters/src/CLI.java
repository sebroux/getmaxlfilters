
import org.apache.commons.cli.*;

/**
 * CLI <p>
 *
 * Parse and verify command line arguments, displays help content on error
 *
 * @author Sebastien Roux @mail roux.sebastien@gmail.com
 *
 * The MIT License Copyright (c) 2010 Sébastien Roux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class CLI {

    private String[] arguments;

    public String[] getArgs() {
        return arguments;
    }

    public void setArgs(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Define and parse command line arguments Jakarta CLI library
     */
    @SuppressWarnings("static-access")
    public void parseArgs() {

        // Define options
        Options options = new Options();

        // add user option
        Option user = OptionBuilder.isRequired().withArgName("user").withLongOpt("essusr").hasArg().withDescription("user login").create("u");
        options.addOption(user);

        // add password option
        Option password = OptionBuilder.isRequired().withArgName("password").withLongOpt("esspwd").hasArg().withDescription("password").create("p");
        options.addOption(password);

        // add server option
        Option server = OptionBuilder.isRequired().withArgName("server").withLongOpt("esssvr").hasArg().withDescription("Essbase server").create("s");
        options.addOption(server);

        // add provider url option
        Option provider = OptionBuilder.withArgName("http://ProviderServer:port/aps/JAPI").withLongOpt("essaps").hasArg().withDescription("Provider server URL (optional) - if not specified default URL is used http://EssbaseServer:13080/aps/JAPI").create("v");
        options.addOption(provider);

        // add application option
        Option application = OptionBuilder.withArgName("application").withLongOpt("essapp").hasArg().withDescription("application name (optional)").create("a");
        options.addOption(application);

        // add database option
        Option database = OptionBuilder.withArgName("database").withLongOpt("essdb").hasArg().withDescription("database name (optional)").create("d");
        options.addOption(database);

        // add help option
        Option help = OptionBuilder.withArgName("help").withLongOpt("help").withDescription("display this help").create("h");
        options.addOption(help);

        // Parse commande line arguments
        CommandLine cmd;

        try {
            GetMaxlFilters getmaxlfilters = new GetMaxlFilters();

            CommandLineParser parser = new GnuParser();

            cmd = parser.parse(options, arguments);

            // Help
            if (cmd.hasOption("h")) {
                displayHelp(options);
            }

            // User
            if (cmd.hasOption("u")) {
                getmaxlfilters.setEssUsr(cmd.getOptionValue("u"));
            } else {
                System.out.println("Please specify a user!");
                displayHelp(options);
            }

            //Password
            if (cmd.hasOption("p")) {
                getmaxlfilters.setEssPwd(cmd.getOptionValue("p"));
            } else {
                System.out.println("Please specify a password!");
                displayHelp(options);
            }

            //Server
            if (cmd.hasOption("s")) {
                getmaxlfilters.setEssSvr(cmd.getOptionValue("s"));
            } else {
                System.out.println("Please specify an Essbase server!");
                displayHelp(options);
            }

            //Provider url if different or specific server
            getmaxlfilters.setEssProvider(cmd.getOptionValue("v"));

            getmaxlfilters.GetMaxlFilters();

        } // if arguments missing for specified options(user, password, server...)
        catch (ParseException e) {
            displayHelp(options);
        }
    }

    /**
     * Display usage and help
     */
    private void displayHelp(Options options) {

        final String HELP_DESC = "DESCRIPTION:\n" + "Extract Essbase security filters as MaxL ready scripts" + "\n";

        final String HELP_REQU = "REQUIREMENTS:\n" + "JRE 1.5 or higher (current JRE version: " + JavaVersionDisplayApplet() + ")\n";

        final String HELP_VERS = "VERSION:\n" + "version 1.0 2012\n";

        final String HELP_AUTH = "AUTHOR:\n" + "Sebastien Roux <roux.sebastien@gmail.com>\n";

        final String HELP_NOTE = "NOTES:\n" + "Use at your own risk!\n"
                + "You will be solely responsible for any damage\n"
                + "to your computer system or loss of data\n"
                + "that may result from the download\n"
                + "or the use of the following application.\n";

        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("java -jar GetMaxlFilters.jar -s server -u user -p password [OPTIONS]",
                HELP_DESC + "OPTIONS:\n", options, HELP_REQU + HELP_VERS
                + HELP_AUTH + HELP_NOTE);
        System.exit(0);
    }

    public static String JavaVersionDisplayApplet() {
        String jVersion = System.getProperty("java.version");
        return jVersion;
    }

    public static void main(String[] args) {
        CLI verifyArguments = new CLI();
        verifyArguments.setArgs(args);
        verifyArguments.parseArgs();
    }
}
