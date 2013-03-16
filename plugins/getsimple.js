importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if(cookies.indexOf("GS_ADMIN_USERNAME=") != -1)
	{
		// convientently username is stored in the cookie
		try{
			var usernameStart = cookies.substring(cookies.indexOf("GS_ADMIN_USERNAME=") + 18, cookies.length);
			var username = "";
			if(usernameStart.indexOf(";") != -1){
				username = usernameStart.substring(0, usernameStart.indexOf(";"));
			} else {
				username = usernameStart.substring(0, usernameStart.length());
			}
			description = "<html>GetSimple CMS Installation on<br><font size=5>" + host + "</font><br>User: " + username;
			sessionUri = "/admin";
		} catch (err){
			// maybe not a session
			description = null;
			profileImageUrl = null;
			sessionUri = null;
		}
	}
}
