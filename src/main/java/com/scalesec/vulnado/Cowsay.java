package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Cowsay {
  
  private static final Logger logger = Logger.getLogger(Cowsay.class.getName());
  
  // Private constructor to hide the implicit public one.
  private Cowsay() {
    throw new IllegalStateException("Utility class");
  }

  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";
    
    // Replace this use of System.out by a logger.
    logger.info(cmd);
    
    // Make sure that this user-controlled command argument doesn't lead to unwanted behavior.
    if(input != null && !input.isEmpty() && !input.contains("rm") && !input.contains("sudo")) {
      processBuilder.command("bash", "-c", cmd);
    } else {
      return "Invalid command";
    }

    StringBuilder output = new StringBuilder();

    try {
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    
    // Make sure this debug feature is deactivated before delivering the code in production.
    if (!logger.isLoggable(java.util.logging.Level.INFO)) {
      output = new StringBuilder();
    }
    
    return output.toString();
  }
}