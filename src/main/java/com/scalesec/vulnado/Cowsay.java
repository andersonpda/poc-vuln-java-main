package com.scalesec.vulnado;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Cowsay {
  private static final Logger LOGGER = Logger.getLogger(Cowsay.class.getName());
  
  // Private constructor to prevent instantiation
  private Cowsay() {
    throw new IllegalStateException("Cowsay class");
  }

  public static String run(String input) {
    ProcessBuilder processBuilder = new ProcessBuilder();
    // Sanitize the input to prevent command injection
    String sanitizedInput = input.replaceAll("['\"]", "");
    String cmd = "/usr/games/cowsay '" + sanitizedInput + "'";
    // Use logger instead of System.out.println
    LOGGER.log(Level.INFO, cmd);
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
      LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }
    return output.toString();
  }
}