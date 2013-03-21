Cookie Cadger
============
## An auditing tool for Wi-Fi or wired Ethernet connections


https://cookiecadger.com

https://github.com/sullivanmatt/CookieCadger

----------------------------------------------
Requirements
----------------------------------------------

1. Java 7
2. The Wireshark Suite (must include the 'tshark' binary)
3. An up-to-date version of Mozilla Firefox
4. Cookie Cadger has optional support for saving data to a MySQL database.  A MySQL installation is required for this feature.

----------------------------------------------
Installation & Operation
----------------------------------------------

1. Download the Cookie Cookie Cadger package from https://cookiecadger.com and extract
2. Run the Cookie Cadger JAR file by double-clicking it, or invoke from command line with java -jar CookieCadger.jar
3. For session detection to work properly, plugins must be placed in the "plugins" directory. This directory must be in the same folder as the Cookie Cadger JAR file.

----------------------------------------------
Example Usage & Command-line Options
----------------------------------------------
java -jar CookieCadger.jar
* --tshark=/usr/sbin/tshark
* --headless=on
* --interfacenum=2 (requires --headless=on)
* --detection=on
* --demo=on
* --update=on
* --dbengine=mysql (default is 'sqlite' for local, file-based storage)
* --dbhost=localhost (requires --dbengine=mysql)
* --dbuser=user (requires --dbengine=mysql)
* --dbpass=pass (requires --dbengine=mysql)
* --dbname=cadgerdata (requires --dbengine=mysql)
* --dbrefreshrate=15 (in seconds, requires --dbengine=mysql, requires --headless=off)

----------------------------------------------
Support
----------------------------------------------

Feature requests / bug reports:

https://github.com/sullivanmatt/CookieCadger/issues


General information / anything else:

sullivan.matt@gmail.com

----------------------------------------------
License (FreeBSD)
----------------------------------------------
Copyright (c) 2013, Matthew Sullivan <MattsLifeBytes.com / @MattsLifeBytes>

All rights reserved.

 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:


1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.


THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
