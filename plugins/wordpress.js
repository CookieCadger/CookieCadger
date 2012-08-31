importPackage(Packages.cookie.cadger.mattslifebytes.com);

var description;
var profileImageUrl;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;

	if(cookies.indexOf("wordpress_logged_in") != -1)
	{
		var pageContent = Packages.cookie.cadger.mattslifebytes.com.CookieCadgerInterface.readURL("http://" + host + uri, userAgent, accept, cookies);

		if(pageContent.indexOf('title="My Account"') != -1)
		{
			// Definite session found, get user name
			var wordpressMyAccountTextPosition = pageContent.indexOf('title="My Account"');
			var wordpressEndOfMyAccountTextPosition = pageContent.indexOf(", " , wordpressMyAccountTextPosition);
			var wordpressUser = pageContent.substring(wordpressEndOfMyAccountTextPosition + 2);
			wordpressUser = wordpressUser.substring(0, wordpressUser.indexOf("<"));

			description = "<html>Wordpress installation on<br><font size=5>" + host + "</font><br>User: " + wordpressUser;
		}
	}
}
