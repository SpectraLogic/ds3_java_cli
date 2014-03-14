package com.spectralogic.ds3cli;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;

public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("c", false, "The Command to execute");
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("c")) {
            System.out.println("Got a command");
        }
    }
}
