importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if(cookies.indexOf("SESS") != -1 && cookies.indexOf("has_js=") != -1)
	{
		var pageContent = Packages.com.cookiecadger.Utils.readUrl("http://" + host + uri, userAgent, accept, cookies);

		// this is kind of them dependent so we try a few variations
		if(pageContent.indexOf('Log out') != -1 || pageContent.indexOf('Logout') != -1 || pageContent.indexOf('logout') != -1)
		{
			// definite session found, try to get user name
			var username = "Unknown";
			
			try {
				if(pageContent.indexOf('Log out ') != -1){
					username = pageContent.substring(pageContent.indexOf('Log out ') + 8);
					username = username.substring(0, username.indexOf("<"));
				} 
				// this tries to grab the username from the dashboard top bar overlay when the user is logged in
				else if(pageContent.indexOf('title="User account">Hello <strong>')){
					username = pageContent.substring(pageContent.indexOf('title="User account">Hello <strong>') + 35);
					username = username.substring(0, username.indexOf("</strong>"));
				}
			} catch (err){
				username = "Unknown";
			}

			description = "<html>Drupal installation on<br><font size=5>" + host + "</font><br>User: " + username;
			sessionUri = "/user";
		}
	}
}
