
  NS4 = (document.layers) ? true : false;
  var newwindow='';

  function openwindowRef(fileName,windowName,attr) {
    var myRemote=window.open(fileName,windowName,attr);
    myRemote.location.href = fileName;
    if (myRemote.opener == null) myRemote.opener = window;
    return myRemote;
}

function openTermFinder() {
    return openwindowRef('testFinder','Term Finder','toolbar=no,menubar=no,width=320,height=320,resizable=yes');
}



function openwindow(fileName,windowName,attr) {
    var myRemote=window.open(fileName,windowName,attr);
}

  function targetWindowFrom(linkref) {
    if(!newwindow.closed && newwindow.location)
     {
      newwindow.location.href=linkref.href;
     }
    else
     {
      newwindow=openWin(linkref.href, linkref.target);
     }
    return false;
  }
  
function toggleLayer(whichLayer) {
    if (document.getElementById) {
        // this is the way the standards work
        var style2 = document.getElementById(whichLayer).style;
        style2.display = style2.display? "":"block";
    } else if (document.all) {
        // this is the way old msie versions work
        var style2 = document.all[whichLayer].style;
        style2.display = style2.display? "":"block";
    } else if (document.layers) {
        // this is the way nn4 works
        var style2 = document.layers[whichLayer].style;
        style2.display = style2.display? "":"block";
    }
}

function toggleLayer2(whichLayer) {
    if (document.getElementById) {
        // this is the way the standards work
        var style2 = document.layers['AIDfp'].document.getElementById(whichLayer).style;
        style2.display = style2.display? "":"block";
    } else if (document.all) {
        // this is the way old msie versions work
        var style2 = document.all[whichLayer].style;
        style2.display = style2.display? "":"block";
    } else if (document.layers) {
        // this is the way nn4 works
        var style2 = document.layers[whichLayer].style;
        style2.display = style2.display? "":"block";
    }
}  

  function targetPopupFrom(linkref, width, height) {
     var url = linkref.href;
     var win_name = linkref.target;

     var attrs = 'height='+height+',width='+width+',';
     attrs += 'resizable,scrollbars';

     window.open(url, win_name, attrs);

     return false;
  }

  function targetWindow(formref) {
    openWin("", formref.target);
    return true;
  }

  function openWin(url, win_name) {
    var str='';
    if(document.all) {
        var width = document.body.offsetWidth-100;
        var height = document.body.offsetHeight-50;
        str = 'height='+height+',width='+width+',';
    }
    else if (document.layers) {
        var width = window.innerWidth-100;
        var height = window.innerHeight-50;
        str = 'height='+height+',width='+width+',';
    }


    str+='toolbar,resizable,scrollbars,location,menubar,status,directories';
    var win = window.open(url, win_name,str);
    return win;
  }

  function checkEnter_org(element, event) {
    var code = 0;
    if (NS4)
      code = event.which;
    else
      code = event.keyCode;
    if (code==13) {
      if(element.form.enterKey) {
        element.form.enterKey.value = element.name;
        element.form.submit();
      }
    }
    return false;
  }

  function sbSubmit(formName, sb_cmd)
  {
    var myform = document.forms[formName];
    var myitem = myform.sb_action;
    myitem.value = sb_cmd;
    myform.submit();

    return false;
  }

