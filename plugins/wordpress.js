importPackage(Packages.cookie.cadger.mattslifebytes.com);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if(cookies.indexOf("wordpress_logged_in") != -1)
	{
		var pageContent = Packages.cookie.cadger.mattslifebytes.com.CookieCadgerInterface.readUrl("http://" + host + uri, userAgent, accept, cookies);

		if(pageContent.indexOf('title="My Account"') != -1)
		{
			// Definite session found, get user name
			var wordpressMyAccountTextPosition = pageContent.indexOf('title="My Account"');
			var wordpressEndOfMyAccountTextPosition = pageContent.indexOf(", " , wordpressMyAccountTextPosition);
			var wordpressUser = pageContent.substring(wordpressEndOfMyAccountTextPosition + 2);
			wordpressUser = wordpressUser.substring(0, wordpressUser.indexOf("<"));

			description = "<html>Wordpress installation on<br><font size=5>" + host + "</font><br>User: " + wordpressUser;
		} else {
			// another wordpress trait for the admin dashboard
			if(pageContent.indexOf("Howdy, ") != -1){
				try{
					var howdyStart = pageContent.substring(pageContent.indexOf("<p>Howdy, ") + 10, pageContent.length());
					var wordpressUser = howdyStart.substring(0, howdyStart.indexOf("</p>")).trim();
					description = "<html>Wordpress installation on<br><font size=5>" + host + "</font><br>User: " + wordpressUser;
				} catch (err){
					// maybe not actually a session
				}
			}
		}
	}
}
