importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if((host === "www.facebook.com" || host === "facebook.com" || host === "m.facebook.com") && cookies.indexOf("c_user") != -1)
	{
		var c_userPosition = cookies.indexOf("c_user=");
		var facebookUserID = cookies.substring(c_userPosition + 7);

		if(facebookUserID.indexOf(";") != -1)
		{
			facebookUserID = facebookUserID.substring(0, facebookUserID.indexOf(";"));
		}

		var apiQueryResult = Packages.com.cookiecadger.Utils.readUrl("https://graph.facebook.com/" + facebookUserID, userAgent, accept, cookies);
		var facebookProfile = JSON.parse(apiQueryResult);

		description = "<html><font size=5>Facebook</font><br>" + facebookProfile.name;
		profileImageUrl = "https://graph.facebook.com/" + facebookUserID + "/picture";
		sessionUri = "/";
	}
}
