importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if(cookies.indexOf("mediawiki") != -1 && cookies.indexOf("session") != -1)
	{
		// convientently username is stored in the cookie
		try{
			var usernameStart = cookies.substring(cookies.indexOf("UserName=") + 9, cookies.length);
			var username = usernameStart.substring(0, usernameStart.indexOf(";"));
			description = "<html>MediaWiki installation on<br><font size=5>" + host + "</font><br>User: " + username;
		} catch (err){
			// maybe not a session
			description = null;
			profileImageUrl = null;
			sessionUri = null;
		}
	}
}
