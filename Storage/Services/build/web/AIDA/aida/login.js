function openwindow(fileName,windowName,attr) {
    var myRemote=window.open(fileName,windowName,attr);
    myRemote.location.href = fileName;
    if (myRemote.opener == null) myRemote.opener = window;
    return myRemote;
}

function openaida() {
    return openwindow('annotate.html','aida','toolbar=no,menubar=no,width=630,height=320,resizable=yes');
}

var axis_url;
var server_url;
var repository;
var username;
var password;

function start(a,s,r,u,p) {
    axis_url = a;
    server_url = s;
    repository = r;
    username = u;
    password = p;
    var aida = openaida();
}