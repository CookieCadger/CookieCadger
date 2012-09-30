package com.mattslifebytes.cookiecadger;

import javax.swing.JOptionPane;

/**
 * Class to provide 'About' utilities for this program.
 */
public class CookieCadgerAbout {
	
	/**
	 * Software version
	 */
	public static final String version = "1.0";
	
	/**
	 * Displays an about window containing the product name, version, and license information.
	 */
	public static void DisplayAboutWindow()
	{
		JOptionPane.showMessageDialog(null, "Cookie Cadger (v"+ version +")\n\n" +
				"Copyright (c) 2012, Matthew Sullivan <MattsLifeBytes.com / @MattsLifeBytes>\n" +
				"All rights reserved.\n" +
				"\n" +
				"Redistribution and use in source and binary forms, with or without\n" +
				"modification, are permitted provided that the following conditions are met: \n" +
				"\n" +
				"1. Redistributions of source code must retain the above copyright notice, this\n" +
				"   list of conditions and the following disclaimer. \n" +
				"2. Redistributions in binary form must reproduce the above copyright notice,\n" +
				"   this list of conditions and the following disclaimer in the documentation\n" +
				"   and/or other materials provided with the distribution. \n" +
				"3. By using this software, you agree to provide the Software Creator (Matthew\n" +
				"   Sullivan) exactly one drink of his choice under $10 USD in value if he\n" +
				"   requests it of you.\n" +
				"\n" +
				"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND\n" +
				"ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\n" +
				"WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE\n" +
				"DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR\n" +
				"ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES\n" +
				"(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;\n" +
				"LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\n" +
				"ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n" +
				"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS\n" +
				"SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
				);
	}
}
