importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if((host === "www.reddit.com" || host === "reddit.com") && cookies.indexOf("reddit_session=") != -1)
	{
		var pageContent = Packages.com.cookiecadger.Utils.readUrl("http://" + host, userAgent, accept, cookies);

		if(pageContent.indexOf('"logged": "') != -1)
		{
			// Definite session found, get user name
			var redditAccountPosition = pageContent.indexOf('"logged": "');
			var redditUsername = pageContent.substring(redditAccountPosition + 11);
			redditUsername = redditUsername.substring(0, redditUsername.indexOf('"'));

			description = "<html><font size=5>Reddit</font><br>" + redditUsername;
			sessionUri = "/";
		}
	}
}
