importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	// godaddy.com or godaddymobile.com
	// Note: Ironically GoDaddy mobile site uses HTTPS for login -> https://idp.godaddymobile.com
	if(host.indexOf("godaddy") != -1 && cookies.indexOf("ShopperId1") != -1)
	{
		var pageContent = Packages.com.cookiecadger.Utils.readUrl("http://" + host + uri, userAgent, accept, cookies);

		if(pageContent.indexOf('My Account') != -1)
		{
			// Definite session, but can't grab a user name.
			description = "<html><font size=5>Go Daddy</font><br>" + "Unknown User";
		}
	}
}
