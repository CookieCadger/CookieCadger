importPackage(Packages.cookie.cadger.mattslifebytes.com);

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
		var pageContent = Packages.cookie.cadger.mattslifebytes.com.CookieCadgerInterface.readUrl("http://" + host + uri, userAgent, accept, cookies);

		if(pageContent.indexOf('My Account') != -1)
		{
			// definite session
			// can't get username it appears to be populated using javascript to the element <span id="pct_sn"></span>
			description = "<html><font size=5>GoDaddy (Domain Registrar)</font><br>" + "Unknown User";
		}
	}
}
