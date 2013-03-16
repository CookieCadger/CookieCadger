importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if(cookies.indexOf("wiki_session=") != -1)
	{
		// conveniently username is stored in the cookie
		try{
			var usernameStart = cookies.substring(cookies.indexOf("wikiUserName=") + 13, cookies.length);
			var username = usernameStart.substring(0, usernameStart.indexOf(";"));
			description = "<html><font size=5>Wikipedia</font><br>User: " + username;
		} catch (err){
			// maybe not a session
		}
	}
}
