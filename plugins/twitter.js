importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	if((host === "www.twitter.com" || host === "twitter.com") && cookies.indexOf("twid=") != -1)
	{
		var twidPosition = cookies.indexOf("twid=");
		var twitterUserID = cookies.substring(twidPosition + 5);

		if(twitterUserID.indexOf(";") != -1)
		{
			twitterUserID = twitterUserID.substring(0, twitterUserID.indexOf(";"));
		}

		twitterUserID = decodeURIComponent(twitterUserID);
		twitterUserID = twitterUserID.substring(2, twitterUserID.indexOf("|"));

		var apiQueryResult = Packages.com.cookiecadger.Utils.readUrl("https://api.twitter.com/users/lookup.json?user_id=" + twitterUserID, userAgent, accept, cookies);
		var twitterProfile = JSON.parse(apiQueryResult);

		description = "<html><font size=5>Twitter</font><br>" + twitterProfile.screen_name;
		profileImageUrl = twitterProfile.profile_image_url;
		sessionUri = "/";
	}
}
