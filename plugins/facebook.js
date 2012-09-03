importPackage(Packages.cookie.cadger.mattslifebytes.com);

var description;
var profileImageUrl;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;

	if((host === "www.facebook.com" || host === "facebook.com") && cookies.indexOf("c_user") != -1)
	{
		if(uri.indexOf("ajax/") != -1 || uri.indexOf("ai.php") != -1)
		{
			return;
		}

		var c_userPosition = cookies.indexOf("c_user=");
		var facebookUserID = cookies.substring(c_userPosition + 7);

		if(facebookUserID.indexOf(";") != -1)
		{
			facebookUserID = facebookUserID.substring(0, facebookUserID.indexOf(";"));
		}

		var apiQueryResult = Packages.cookie.cadger.mattslifebytes.com.CookieCadgerInterface.readURL("https://graph.facebook.com/" + facebookUserID, userAgent, accept, cookies);
		var facebookProfile = JSON.parse(apiQueryResult);

		description = "<html><font size=5>Facebook</font><br>" + facebookProfile.name;
		profileImageUrl = "https://graph.facebook.com/" + facebookUserID + "/picture";
	}
}
