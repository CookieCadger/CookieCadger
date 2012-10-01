package com.mattslifebytes.cookiecadger;

/**
 * Contains the main method for CookieCadger.
 */
public class CookieCadger {
	
	/* program arguments */
	private CookieCadgerInterface ccInterface;
	private String argsTsharkPath;
	private boolean argsUseSessionDetection;
	private boolean argsUseDemoMode;
	
	public static void main(String[] args) {
		// Someday, our prince will come

	}

	/**
	 * Handles program arguments for CookieCadger.
	 * @param args the command line arguments to handle
	 */
	private void HandleProgramArguments(String[] args) {
		String arg;
		boolean errorEncountered = false;
		for(int i = 0; i < args.length && (args[i].startsWith("-") || args[i].startsWith("/")); i++) {
			arg = args[i];
		    // use this type of check for arguments that require arguments
			if (arg.contains("tshark")) {
				boolean tsharkReqs = false;
				if(arg.contains("tshark=")) {
					String tsharkPath = arg.split("=")[1];
					if(tsharkPath.length() > 0) {
						tsharkReqs = true;
						this.argsTsharkPath = tsharkPath;
					}
				}
				if(tsharkReqs == false) {
					System.err.println("--tshark requires a path to the tshark binary");
					errorEncountered = true;
				}
			} else if (arg.contains("detection")) {
				boolean detectionReqs = false;
				if(arg.contains("detection=")) {
					String detectionValue = arg.split("=")[1];
					if(detectionValue.equals("on")) {
						this.argsUseSessionDetection = true;
						detectionReqs = true;
					} else if (detectionValue.equals("off")) {
						this.argsUseSessionDetection = false;
						detectionReqs = true;
					}
				}
				if(detectionReqs == false) {
					errorEncountered = true;
					System.err.println("--detection (Session Detection) requires an 'on' or 'off' value");
				}
			} else if (arg.contains("demo")) {
				boolean demoReqs = false;
				if(arg.contains("demo=")) {
					String demoValue = arg.split("=")[1];
					if(demoValue.equals("on")) {
						this.argsUseDemoMode = true;
						demoReqs = true;
					} else if (demoValue.equals("off")) {
						this.argsUseDemoMode = false;
						demoReqs = true;
					}
				}
				if(demoReqs == false) {
					errorEncountered = true;
					System.err.println("--demo (automatic loading of session into the browser) requires an 'on' or 'off' value. Session Detection must also be enabled.");
				}
			}
			if(errorEncountered) {
				ccInterface.onBadArgsClose();
				System.exit(1);
			}
		}
	}
	
}
