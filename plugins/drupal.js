importPackage(Packages.cookie.cadger.mattslifebytes.com);

var description;
var profileImageUrl;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;

	if(cookies.indexOf("SESS") != -1 && cookies.indexOf("has_js=") != -1)
	{
		var pageContent = Packages.cookie.cadger.mattslifebytes.com.CookieCadgerInterface.readURL("http://" + host + uri, userAgent, accept, cookies);

		if(pageContent.indexOf('Log out ') != -1)
		{
			// Definite session found, get user name
			var drupalLogoutTextPosition = pageContent.indexOf('Log out ');
			var drupalUser = pageContent.substring(drupalLogoutTextPosition + 8);
			drupalUser = drupalUser.substring(0, drupalUser.indexOf("<"));

			description = "<html>Drupal installation on<br><font size=5>" + host + "</font><br>User: " + drupalUser;
		}
	}
}
