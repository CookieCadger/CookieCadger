importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri =  null;

	if(cookies.indexOf("phpbb3_") != -1)
	{
		var pageContent = Packages.com.cookiecadger.Utils.readUrl("http://" + host + uri, userAgent, accept, cookies);

		if(pageContent.indexOf('Logout [ ') != -1)
		{
			// Definite session found, get user name
			var phpbb3LogoutTextPosition = pageContent.indexOf('Logout [ ');
			var phpbb3User = pageContent.substring(phpbb3LogoutTextPosition + 9);
			phpbb3User = phpbb3User.substring(0, phpbb3User.indexOf(" ]"));

			description = "<html>phpBB3 installation on<br><font size=5>" + host + "</font><br>User: " + phpbb3User;
		}
	}
}
