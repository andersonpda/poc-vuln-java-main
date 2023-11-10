package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Cowsay {
  private static final Logger logger = Logger.getLogger(Cowsay.class.getName());
  
  private Cowsay() {
    // Private constructor to hide the implicit public one.
  }

  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String cmd = "/usr/games/cowsay '" + input + "'";

    // Replace System.out with logger
    logger.info(cmd);
    processBuilder.command("bash", "-c", cmd);

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
    if (logger.getLevel() != null && logger.getLevel().intValue() <= 800) { // 800 is the WARNING Level value
      return "";
    }

    // Make sure the "PATH" used to find this command includes only what you intend.
    if (!cmd.startsWith("/usr/games/cowsay")) {
      return "";
    }

    return output.toString();
  }
}