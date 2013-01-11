function openwindow(fileName,windowName,attr) {
    var myRemote=window.open(fileName,windowName,attr);
    myRemote.location.href = fileName;
    if (myRemote.opener == null) myRemote.opener = window;
    return myRemote;
}

function openaida() {
    return openwindow('annotate.html','aida','toolbar=no,menubar=no,width=630,height=320,resizable=yes');
}

var server_url;
var repository;
var username;
var password;
var session;
var model;

function start(s,r,u,p,se,mo) {
    server_url = s;
    repository = r;
    username = u;
    password = p;
    session = se;
    model = mo;
    var aida = openaida();
}