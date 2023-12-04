<!-- Copyright 2019 Mark59.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 
  
  Author:  Philip Webb
  Date:    Australian Summer 2020
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<title>Metrics Overview</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<style type="text/css">
   body {  margin: 0; font-size:10pt;font-family:"Arial"}
   .a10n{font-size:10pt;font-family:"Arial"}
   .c10n{font-size:10pt;font-family:"Consolas"} 
   pre { display: block; font-family: monospace; white-space: pre; margin: 1em 2em;	}   
 
</style>
</head>
<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
  
<h1>Metrics Overview</h1>

<div><img style="max-width: 100%;" src="images/mark59_banner_thin.png"/></div> 		
 
<p>This <b>Mark59 Metrics Web Application</b> can be configured to run Groovy scripts, or commands on your Linux, Unix or Windows servers,
 to obtain performance metrics during JMeter test execution. 
Communication between JMeter and the Metrics web application is handled by the Mark59 Metrics Api artefact mark59-metrics-api.jar deployed to JMeter. 
Communication from the Metrics web application to a target server happens using:
<ul> 
<li>a SSH connection for Linux / Unix (user/password or public/private key), or</li>
<li>Windows WMIC commands or remote PowerShell scripts</li>
</ul>   
The Metrics web application contains three main data structures: Server Profiles, Commands, and Response Parsers.

<p><b>Server Profiles</b> provide the identifiers relating the JMeter Mark59 Metrics Api Java Request to the Metrics web application. 
They link a server under test to one or more Commands you want to execute on that server, or for a Groovy Server Profile one Groovy script. 
Depending on the Commands used, they may need parameter values to be set. 

<p><b>Commands</b> may be Linux/Unix Shell, WMIC, PowerShell or Groovy scripts. There are two forms of parameterization:
<ul>
<li>Groovy commands treat parameters as variables</li>
<li>All other commands use a simple form of string substitution</li>
</ul>
For example the supplied Groovy command SimpleScriptSampleCmd defines parameters parm1, parm2, parm3, parm4.  Usage in the command:
		
<pre>commandLogDebug += "passed parms : parm1=" + <b>parm1</b> + ", parm2=" + <b>parm2</b> + ",  parm3=" + <b>parm3</b></pre>
	
<p>The supplied PowerShell command WIN_Core defines parameters SECURE_KEY_ARRAY and SECURE_STRING_TXT.
The command references them using a $&#123;param&#125; convention:
	
<pre>[Byte[]] $key = @(<b>$&#123;SECURE_KEY_ARRAY&#125;</b>); 
$securePwd = '<b>$&#123;SECURE_STRING_TXT&#125;</b>' | ConvertTo-SecureString -Key $key;
</pre>

There are also some pre-defined parameters available (listed above the command panel).   

<p><b>Response Parsers</b> use Groovy to parse the response from a server-based command and return a numeric result. 
If the parsed result is not numeric it is considered invalid. 
A Groovy command does not use a response parser, as you should be able to extract the metric(s) you need using the Groovy command itself.   

<br><br>
<p><b>Notes on Key Connection Parameters..</b>  

<p>A <b>Linux SSH private/public key connection</b>. See sample command LINUX_free_m_1_1_ViaSSH, profile remoteLinuxServerViaSSH.  
The Java library Jsch used for Nix connectivity has a limitation with SSH private/public key formats, 
 so you should use the 'classic OpenSSH' format.  

<p>To convert an already existing key:      
<pre>ssh-keygen -p -f privateKeyFile -m PEM -P passphrase -N passphrase</pre>

<p>For a key without a passphrase, use a blank string. Eg:
<pre>ssh-keygen -p -f ~/.ssh/myfavsshprvkey -m PEM -P "" -N ""</pre>

<p>When creating a new key, just add the <span class="c10n">-m PEM</span> option to the ssh-keygen command. Eg:
<pre>ssh-keygen -m PEM -t rsa -f ~/.ssh/mynewfavsshkey</pre>

In Mark59, the pre-defined parameter holding the location of the private key is SSH_IDENTITY. 

<p>For a <b>Windows PowerShell connection</b> using a Credential object built using a user-defined Key,
 see sample command WIN_Core (and other commands) included in profile remoteWinServer_secureStr_key. 
 To create the necessary configuration elements, you run PowerShell locally.  Eg:
	 
<pre>[Byte[]] $key = (101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116)
$secureString = ConvertTo-SecureString 'mypassword' -AsPlainText -Force
$secureStringTxt = $secureString | ConvertFrom-SecureString -key $key
$secureStringTxt
</pre>

In this example the parameter values would be:
	
<pre>SECURE_KEY_ARRAY: 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116
SECURE_STRING_TXT: the value of $secureStringTxt in the example
</pre>
Also put your userid in the Username field (but do not enter a password).

<p>This is the preferred approach as
<ul> 
<li>you never need to enter the user's password into Mark59, and
<li>if you do not use a user-defined key, the system generates an internal key base on the computer and userid that was used. 
 This means the connection cannot be used by any other user or computer
 (pretty limiting for both the web application, and any use of a downloaded Server Profile spreadsheet).    
</ul> 

The supplied examples should get you started. 
There is much more detail available including a fully worked example of adding a Profile, a Command and its Parser,
 in the User Guide available at <a href="https://mark59.com">The Mark59 Website</a>. 

<p>Mark59 Version: 5.7</p>  
			
</div>
</body>
</html>