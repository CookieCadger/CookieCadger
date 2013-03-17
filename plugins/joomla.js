importPackage(Packages.com.cookiecadger);

var description;
var profileImageUrl;
var sessionUri;

function processRequest(host, uri, userAgent, accept, cookies)
{
	description = null;
	profileImageUrl = null;
	sessionUri = null;

	// deafult install of joomla has jpanesliders_ in cookie
	// See: https://github.com/joomla/joomla-cms/blob/master/libraries/joomla/html/sliders.php
	// See also: https://github.com/JTracker/jissues/blob/master/libraries/joomla/session/session.php
	if(cookies.indexOf("jpanesliders_") != -1)
	{
		var pageContent = Packages.com.cookiecadger.Utils.readUrl("http://" + host + uri, userAgent, accept, cookies);
		
		if(pageContent.indexOf("Joomla!") != -1){
			if(uri.indexOf("administrator") != -1){
				// administrator page
				description = "<html>Joomla! installation on<br><font size=5>" + host + "</font><br>User Role: Administrator";
				sessionUri = "/administrator";
			} else {
				description = "<html>Joomla! installation on<br><font size=5>" + host + "</font>";
			}
		}
	}
}
